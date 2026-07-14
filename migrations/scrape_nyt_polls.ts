import PocketBase from 'pocketbase';
import * as readline from 'readline';

const pb = new PocketBase('https://polling-data.pockethost.io');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const question = (query: string): Promise<string> => new Promise(resolve => rl.question(query, resolve));

// Very simple CSV parser for trusted data
function parseCSV(text: string) {
    const lines = text.split('\n').filter(l => l.trim().length > 0);
    const headers = lines[0].split(',').map(h => h.replace(/"/g, '').trim());
    
    const results = [];
    for (let i = 1; i < lines.length; i++) {
        // Handle commas inside quotes
        const row = lines[i].split(/,(?=(?:(?:[^"]*"){2})*[^"]*$)/).map(v => v.replace(/"/g, '').trim());
        const obj: any = {};
        headers.forEach((h, idx) => {
            obj[h] = row[idx];
        });
        results.push(obj);
    }
    return results;
}

async function run() {
    console.log("=== New York Times 2026 Midterms Scraper (No Paywall!) ===");
    console.log("Did you know NYT publishes their raw interactive data to public CSVs? We can grab it directly!\n");
    
    const email = await question('Enter your PocketHost Admin Email: ');
    const password = await question('Enter your PocketHost Admin Password: ');

    console.log("\n1. Authenticating as Admin...");
    try {
        await pb.collection('_superusers').authWithPassword(email, password);
        console.log("✅ Authenticated successfully.");
    } catch(e2) {
        console.error("❌ Failed to authenticate as admin.");
        rl.close();
        return;
    }

    const targets = [
        { name: "House", url: "https://www.nytimes.com/newsgraphics/polls/house.csv" },
        { name: "Senate", url: "https://www.nytimes.com/newsgraphics/polls/senate.csv" },
        { name: "Governor", url: "https://www.nytimes.com/newsgraphics/polls/governor.csv" }
    ];

    let totalInserted = 0;

    for (const target of targets) {
        console.log(`\n2. Fetching NYT ${target.name} Polls...`);
        const res = await fetch(target.url);
        if (!res.ok) {
            console.error(`❌ Failed to fetch ${target.name} data.`);
            continue;
        }
        
        const csvText = await res.text();
        const records = parseCSV(csvText);
        console.log(`✅ Fetched and parsed ${records.length} ${target.name} polls.`);

        console.log(`3. Pushing ${target.name} Data to PocketHost...`);
        // For demonstration, inserting first 20 records of each type
        const limit = Math.min(20, records.length);
        
        for (let i = 0; i < limit; i++) {
            const r = records[i];
            
            // Resolve Geography
            const state = r.state || "US";
            let geoId = "";
            try {
                const existingGeo = await pb.collection('geographies').getFirstListItem(`name="${state}"`);
                geoId = existingGeo.id;
            } catch {
                const geoLevel = state === "US" ? "national" : "state";
                const newGeo = await pb.collection('geographies').create({ name: state, geo_level: geoLevel });
                geoId = newGeo.id;
            }

            // Resolve Candidate
            const candName = r.candidate_name || r.answer || "Generic Candidate";
            const party = r.party || "Unknown";
            let candId = "";
            try {
                const existingCand = await pb.collection('candidates').getFirstListItem(`name="${candName}"`);
                candId = existingCand.id;
            } catch {
                const newCand = await pb.collection('candidates').create({ name: candName, party: party });
                candId = newCand.id;
            }

            // Resolve Poll
            const pollster = r.pollster || "Unknown Pollster";
            let pollId = "";
            try {
                const existingPoll = await pb.collection('polls').getFirstListItem(`pollster="${pollster}" && start_date="${r.start_date}"`);
                pollId = existingPoll.id;
            } catch {
                let pSize = parseInt(r.sample_size || "0");
                if (isNaN(pSize)) pSize = 0;

                const newPoll = await pb.collection('polls').create({
                    pollster: pollster,
                    start_date: new Date(r.start_date || Date.now()).toISOString(),
                    end_date: new Date(r.end_date || Date.now()).toISOString(),
                    sample_size: pSize,
                    population: r.population || "rv"
                });
                pollId = newPoll.id;
            }

            // Insert Result
            let pct = parseFloat(r.pct || "0");
            if (isNaN(pct)) pct = 0;

            await pb.collection('poll_results').create({
                poll_id: pollId,
                geography_id: geoId,
                candidate_id: candId,
                pct: pct
            });

            totalInserted++;
            process.stdout.write(`\rInserted ${i + 1}/${limit} ${target.name} records...`);
        }
        console.log(`\n✅ Finished batch for ${target.name}.`);
    }

    console.log(`\n🎉 Grand Total: Inserted ${totalInserted} NYT polling records across all races!`);
    rl.close();
}

run().catch(e => {
    console.error(e);
    rl.close();
});
