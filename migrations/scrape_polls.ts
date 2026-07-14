import PocketBase from 'pocketbase';
import * as readline from 'readline';

const pb = new PocketBase('https://polling-data.pockethost.io');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const question = (query: string): Promise<string> => new Promise((resolve) => rl.question(query, resolve));

// Very lightweight CSV parser for the specific 538 format
function parseCSV(text: string) {
    const lines = text.split('\n').filter(l => l.trim().length > 0);
    const headers = lines[0].split(',').map(h => h.trim());
    const data = [];
    for (let i = 1; i < lines.length; i++) {
        // Handle quotes in CSV
        const row = lines[i].match(/(".*?"|[^",\s]+)(?=\s*,|\s*$)/g) || [];
        const cleanRow = row.map(cell => cell.replace(/^"|"$/g, '').trim());
        const obj: any = {};
        headers.forEach((h, idx) => {
            obj[h] = cleanRow[idx] || "";
        });
        data.push(obj);
    }
    return data;
}

async function runScraper() {
    console.log("=== FiveThirtyEight Presidential Polling Scraper ===");
    const email = await question("Enter your PocketHost Admin Email: ");
    const password = await question("Enter your PocketHost Admin Password: ");
    rl.close();

    console.log("\n1. Authenticating as Admin...");
    try {
        await pb.admins.authWithPassword(email, password);
        console.log("✅ Authenticated successfully.");
    } catch (e) {
        console.error("❌ Failed to authenticate as admin.");
        return;
    }

    console.log("\n2. Reading Presidential Polling Data from local CSV...");
    const fs = require('fs');
    const path = require('path');
    const filePath = path.join(process.cwd(), 'president_polls.csv');
    if (!fs.existsSync(filePath)) {
        console.error("❌ Failed to find 'president_polls.csv' locally.");
        console.error("Cloudflare blocks automated downloads. Please manually download the CSV from:");
        console.error("👉 https://projects.fivethirtyeight.com/polls/data/president_polls.csv");
        console.error("Place it in this 'migrations' folder and run this script again.");
        return;
    }
    const csvText = fs.readFileSync(filePath, 'utf-8');
    console.log(`✅ Read ${(csvText.length / 1024 / 1024).toFixed(2)} MB of polling data from local file.`);

    console.log("\n3. Parsing CSV Data...");
    const rows = parseCSV(csvText);
    console.log(`✅ Parsed ${rows.length} poll rows.`);

    // Local caches to avoid spamming the DB with duplicate creations
    const geoCache = new Map<string, string>();
    const candidateCache = new Map<string, string>();
    const pollCache = new Map<string, string>();

    console.log("\n4. Pushing Data to PocketHost (Batching up to 500 records for demonstration)...");
    
    // Process a subset to avoid hour-long script for now, user can change limit.
    const limit = Math.min(rows.length, 500); 
    
    for (let i = 0; i < limit; i++) {
        const row = rows[i];
        
        // --- 1. Geography ---
        const stateName = row.state ? row.state : "National";
        const geoLevel = stateName === "National" ? "national" : "state";
        const geoKey = `${geoLevel}-${stateName}`;
        
        let geoId = geoCache.get(geoKey);
        if (!geoId) {
            try {
                // Try to find first
                const existingGeo = await pb.collection('geographies').getFirstListItem(`geo_level="${geoLevel}" && name="${stateName}"`);
                geoId = existingGeo.id;
            } catch {
                const newGeo = await pb.collection('geographies').create({ geo_level: geoLevel, name: stateName });
                geoId = newGeo.id;
            }
            geoCache.set(geoKey, geoId);
        }

        // --- 2. Candidate ---
        const candidateName = row.answer;
        const candidateParty = row.candidate_party || "IND";
        if (!candidateName) continue; // skip empty
        
        let candidateId = candidateCache.get(candidateName);
        if (!candidateId) {
            try {
                const existingCand = await pb.collection('candidates').getFirstListItem(`name="${candidateName}"`);
                candidateId = existingCand.id;
            } catch {
                const newCand = await pb.collection('candidates').create({ name: candidateName, party: candidateParty });
                candidateId = newCand.id;
            }
            candidateCache.set(candidateName, candidateId);
        }

        // --- 3. Poll ---
        // 538 provides a unique poll_id
        const pollster = row.pollster || "Unknown";
        const startDate = row.start_date ? new Date(row.start_date).toISOString() : new Date().toISOString();
        const endDate = row.end_date ? new Date(row.end_date).toISOString() : new Date().toISOString();
        const sampleSize = parseInt(row.sample_size) || 0;
        const population = row.population || "A";
        const pollKey = `${pollster}-${startDate}-${endDate}-${sampleSize}`;
        
        let pollId = pollCache.get(pollKey);
        if (!pollId) {
            try {
                const newPoll = await pb.collection('polls').create({ 
                    pollster, 
                    start_date: startDate, 
                    end_date: endDate, 
                    sample_size: sampleSize, 
                    population 
                });
                pollId = newPoll.id;
                pollCache.set(pollKey, pollId);
            } catch(e: any) {
                console.error("Failed to insert poll:", e.response?.data || e.message);
                continue; 
            }
        }

        // --- 4. Poll Result (X-Ref) ---
        const pct = parseFloat(row.pct) || 0.0;
        try {
            await pb.collection('poll_results').create({
                poll_id: pollId,
                geography_id: geoId,
                candidate_id: candidateId,
                pct: pct
            });
        } catch(e: any) {
            console.error("Failed to insert poll result:", e.response?.data || e.message);
        }
        
        if (i % 50 === 0) {
            process.stdout.write(`\rProcessed ${i}/${limit} records...`);
        }
    }

    console.log(`\n\n✅ Successfully pushed ${limit} normalized polling cross-references to PocketHost!`);
    console.log("Note: To ingest all 10,000+ historical rows, edit limit in script.");
}

runScraper();
