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
    const resultCache = new Set<string>();

    // Helper sleep function to pace requests
    const sleep = (ms: number) => new Promise(r => setTimeout(r, ms));

    // Helper to safely insert into PocketBase with 429 Rate Limit exponential backoff
    async function retryCreate(collection: string, payload: any, maxRetries = 5): Promise<any> {
        for (let attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return await pb.collection(collection).create(payload, { requestKey: null });
            } catch (e: any) {
                if (e.status === 429) {
                    const waitTime = attempt * 10000; // 10s, 20s, 30s...
                    console.log(`\n⚠️ Rate limited on ${collection}. Sleeping for ${waitTime/1000} seconds...`);
                    await sleep(waitTime);
                    if (attempt === maxRetries) throw e;
                } else if (e.status === 400 && collection === 'poll_results') {
                    // Ignore relation validation failures instead of crashing
                    return null;
                } else {
                    throw e;
                }
            }
        }
    }

    console.log("\n2. Pre-fetching existing data to completely bypass rate limits...");
    try {
        const allGeos = await pb.collection('geographies').getFullList({ requestKey: null });
        allGeos.forEach(g => geoCache.set(g.name, g.id));
        
        const allCands = await pb.collection('candidates').getFullList({ requestKey: null });
        allCands.forEach(c => candCache.set(`${c.name}-${c.party}`, c.id));
        
        const allPolls = await pb.collection('polls').getFullList({ requestKey: null });
        allPolls.forEach(p => pollCache.set(`${p.pollster}-${p.start_date.split(' ')[0]}`, p.id));

        const allResults = await pb.collection('poll_results').getFullList({ requestKey: null });
        allResults.forEach(r => resultCache.add(`${r.poll_id}-${r.candidate_id}-${r.geography_id}`));

        console.log(`✅ Cached ${geoCache.size} geographies, ${candCache.size} candidates, ${pollCache.size} polls, and ${resultCache.size} results in memory.`);
    } catch(e) {
        console.log("⚠️ Could not pre-fetch fully, relying on lazy caching.", e);
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
        // We will process all records!
        const limit = records.length;
        
        let newPollsAdded = 0;

        for (let i = 0; i < limit; i++) {
            const r = records[i];
            
            // 1. Resolve Geography
            const state = r.state || "US";
            let geoId = geoCache.get(state);
            if (!geoId) {
                const geoLevel = state === "US" ? "national" : "state";
                const newGeo = await retryCreate('geographies', { name: state, geo_level: geoLevel });
                geoId = newGeo.id;
                geoCache.set(state, geoId);
                await sleep(500);
            }

            // 2. Resolve Candidate
            const candName = r.candidate_name || r.answer || "Generic Candidate";
            const party = r.party || "Unknown";
            const candKey = `${candName}-${party}`;
            let candId = candCache.get(candKey);
            if (!candId) {
                const newCand = await retryCreate('candidates', { name: candName, party: party });
                candId = newCand.id;
                candCache.set(candKey, candId);
                await sleep(500);
            }

            // 3. Resolve Poll
            const pollster = r.pollster || "Unknown Pollster";
            const startDateStr = (r.start_date || new Date().toISOString()).split('T')[0];
            const pollKey = `${pollster}-${startDateStr}`;
            let pollId = pollCache.get(pollKey);
            
            if (!pollId) {
                let pSize = parseInt(r.sample_size || "0");
                if (isNaN(pSize)) pSize = 0;

                const newPoll = await retryCreate('polls', {
                    pollster: pollster,
                    start_date: new Date(r.start_date || Date.now()).toISOString(),
                    end_date: new Date(r.end_date || Date.now()).toISOString(),
                    sample_size: pSize,
                    population: r.population || "rv"
                });
                pollId = newPoll.id;
                pollCache.set(pollKey, pollId);
                await sleep(500);
            }

            // 4. Insert Result
            // We use a deduplication key to ensure we don't insert the same candidate twice for the same poll
            const resultKey = `${pollId}-${candId}-${geoId}`;
            if (!resultCache.has(resultKey)) {
                let pct = parseFloat(r.pct || "0");
                if (isNaN(pct)) pct = 0;

                try {
                    await retryCreate('poll_results', {
                        poll_id: pollId,
                        geography_id: geoId,
                        candidate_id: candId,
                        pct: pct
                    });

                    resultCache.add(resultKey);
                    totalInserted++;
                    newPollsAdded++;
                    await sleep(50);
                } catch (e: any) {
                    console.error(`\n❌ Failed to insert result for poll ${pollId}, cand ${candId}, geo ${geoId}.`);
                    console.error("Payload rejected:", JSON.stringify(e?.response || e, null, 2));
                    // We don't crash, we just skip and continue
                }
            }
            
            if (i % 100 === 0 || i === limit - 1) {
                process.stdout.write(`\rProcessed ${i + 1}/${limit} ${target.name} records... (Added ${newPollsAdded} new results)`);
            }
        }
        console.log(`\n✅ Finished batch for ${target.name}. Added ${newPollsAdded} new records.`);
    }

    console.log(`\n🎉 Grand Total: Inserted ${totalInserted} NYT polling records across all races!`);
    rl.close();
}

run().catch(e => {
    console.error(e);
    rl.close();
});
