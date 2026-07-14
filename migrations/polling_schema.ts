import PocketBase from 'pocketbase';
import * as readline from 'readline';

const pb = new PocketBase('https://econometrics-broker.pockethost.io');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const question = (query: string): Promise<string> => new Promise((resolve) => rl.question(query, resolve));

async function runMigration() {
    console.log("=== Polling Data Schema Migration ===");
    const email = await question("Enter your PocketHost Admin Email: ");
    const password = await question("Enter your PocketHost Admin Password: ");
    rl.close();

    console.log("\n1. Authenticating as Admin...");
    try {
        await pb.admins.authWithPassword(email, password);
        console.log("✅ Authenticated successfully.");
    } catch (e) {
        console.error("❌ Migration Failed: Failed to authenticate as admin.");
        return;
    }

    // Function to create collection and return its ID
    async function createOrGetCollection(config: any) {
        try {
            const col = await pb.collections.create(config);
            console.log(`✅ '${config.name}' collection created.`);
            return col.id;
        } catch (e: any) {
            try {
                const existing = await pb.collections.getOne(config.name);
                console.log(`⚠️ '${config.name}' collection already exists.`);
                return existing.id;
            } catch (innerE) {
                console.error(`❌ Failed to create or get '${config.name}':`, e.message);
                throw e;
            }
        }
    }

    // Creating geographies
    console.log("\n2. Creating 'geographies' collection...");
    const geoId = await createOrGetCollection({
        name: 'geographies',
        type: 'base',
        schema: [
            { name: 'geo_level', type: 'select', options: { maxSelect: 1, values: ["national", "state", "district", "local"] }, required: true },
            { name: 'name', type: 'text', required: true }
        ]
    });

    // Creating candidates
    console.log("\n3. Creating 'candidates' collection...");
    const candidateId = await createOrGetCollection({
        name: 'candidates',
        type: 'base',
        schema: [
            { name: 'name', type: 'text', required: true },
            { name: 'party', type: 'text' }
        ]
    });

    // Creating polls
    console.log("\n4. Creating 'polls' collection...");
    const pollId = await createOrGetCollection({
        name: 'polls',
        type: 'base',
        schema: [
            { name: 'pollster', type: 'text', required: true },
            { name: 'start_date', type: 'date', required: true },
            { name: 'end_date', type: 'date', required: true },
            { name: 'sample_size', type: 'number' },
            { name: 'population', type: 'text' }, // RV, LV, A
            { name: 'methodology', type: 'text' }
        ]
    });

    // Creating poll_results (xref table)
    console.log("\n5. Creating 'poll_results' xref collection...");
    await createOrGetCollection({
        name: 'poll_results',
        type: 'base',
        schema: [
            { name: 'poll_id', type: 'relation', required: true, options: { collectionId: pollId, cascadeDelete: true } },
            { name: 'geography_id', type: 'relation', required: true, options: { collectionId: geoId, cascadeDelete: false } },
            { name: 'candidate_id', type: 'relation', required: true, options: { collectionId: candidateId, cascadeDelete: false } },
            { name: 'pct', type: 'number', required: true }
        ]
    });

    console.log("\nMigration completed successfully!");
}

runMigration();
