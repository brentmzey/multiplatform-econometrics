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
    
    const email = process.env.POCKETHOST_ADMIN_EMAIL || await question('Enter your PocketHost Admin Email: ');
    const password = process.env.POCKETHOST_ADMIN_PASSWORD || await question('Enter your PocketHost Admin Password: ');

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

    // In-memory caches to reduce API calls and avoid 429 Rate Limits
    const geoCache = new Map<string, string>();
    const candCache = new Map<string, string>();
    const pollCache = new Map<string, string>();

    // Helper sleep function to pace requests
    const sleep = (ms: number) => new Promise(r => setTimeout(r, ms));

    console.log("\n2. Pre-fetching existing data to completely bypass rate limits...");
    try {
        const allGeos = await pb.collection('geographies').getFullList();
        allGeos.forEach(g => geoCache.set(g.name, g.id));
        
        const allCands = await pb.collection('candidates').getFullList();
        allCands.forEach(c => candCache.set(`${c.name}-${c.party}`, c.id));
        
        console.log(`✅ Cached ${geoCache.size} geographies and ${candCache.size} candidates in memory.`);
    } catch(e) {
        console.log("⚠️ Could not pre-fetch fully, relying on lazy caching.");
    }

    for (const target of targets) {
        console.log(`\n3. Fetching NYT ${target.name} Polls...`);
        const res = await fetch(target.url);
        if (!res.ok) {
            console.error(`❌ Failed to fetch ${target.name} data.`);
            continue;
        }
        
        const csvText = await res.text();
        const records = parseCSV(csvText);
        console.log(`✅ Fetched and parsed ${records.length} ${target.name} polls.`);

        console.log(`4. Pushing ${target.name} Data to PocketHost...`);
        // We will push 50 records to demonstrate since we are caching now
        const limit = Math.min(50, records.length);
        
        for (let i = 0; i < limit; i++) {
            const r = records[i];
            
            // 1. Resolve Geography
            const state = r.state || "US";
            let geoId = geoCache.get(state);
            if (!geoId) {
                const geoLevel = state === "US" ? "national" : "state";
                const newGeo = await pb.collection('geographies').create({ name: state, geo_level: geoLevel });
                geoId = newGeo.id;
                geoCache.set(state, geoId);
                await sleep(250); // backoff after a write
            }

            // 2. Resolve Candidate
            const candName = r.candidate_name || r.answer || "Generic Candidate";
            const party = r.party || "Unknown";
            const candKey = `${candName}-${party}`;
            let candId = candCache.get(candKey);
            if (!candId) {
                const newCand = await pb.collection('candidates').create({ name: candName, party: party });
                candId = newCand.id;
                candCache.set(candKey, candId);
                await sleep(250); // backoff after a write
            }

            // 3. Resolve Poll
            const pollster = r.pollster || "Unknown Pollster";
            const pollKey = `${pollster}-${r.start_date}`;
            let pollId = pollCache.get(pollKey);
            if (!pollId) {
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
                if (pollId) pollCache.set(pollKey, pollId);
                await sleep(250); // backoff after poll queries
            }

            // 4. Insert Result
            let pct = parseFloat(r.pct || "0");
            if (isNaN(pct)) pct = 0;

            await pb.collection('poll_results').create({
                poll_id: pollId as string,
                geography_id: geoId as string,
                candidate_id: candId as string,
                pct: pct
            });

            totalInserted++;
            process.stdout.write(`\rInserted ${i + 1}/${limit} ${target.name} records...`);
            
            // Sleep slightly to respect PocketHost rate limits (prevent 429)
            await sleep(250);
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
