import PocketBase from 'pocketbase';
import * as readline from 'readline';

const pb = new PocketBase('https://polling-data.pockethost.io');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const question = (query: string): Promise<string> => new Promise(resolve => rl.question(query, resolve));

async function ensureCollection(name: string, fieldsDef: any[]) {
    try {
        const existing = await pb.collections.getOne(name);
        console.log(`✅ Collection '${name}' already exists.`);
        return existing;
    } catch (e) {
        console.log(`⚠️ Collection '${name}' not found. Creating...`);
        // Use 'fields' instead of 'schema' for PocketBase v0.23+
        const newCollection = await pb.collections.create({
            name: name,
            type: 'base',
            fields: fieldsDef,
            listRule: "",
            viewRule: "",
            createRule: "@request.auth.id != ''",
            updateRule: "@request.auth.id != ''",
            deleteRule: "@request.auth.id != ''",
        });
        console.log(`✅ Created collection '${name}'.`);
        return newCollection;
    }
}

async function run() {
    console.log("=== PocketBase Idempotent Schema Migration ===");
    const email = process.env.POCKETHOST_ADMIN_EMAIL || await question('Enter your PocketHost Admin Email: ');
    const password = process.env.POCKETHOST_ADMIN_PASSWORD || await question('Enter your PocketHost Admin Password: ');

    console.log("\n1. Authenticating as Admin...");
    try {
        await pb.admins.authWithPassword(email, password);
        console.log("✅ Authenticated successfully.");
    } catch(e) {
        console.error("❌ Failed to authenticate as admin.");
        rl.close();
        return;
    }

    console.log("\n2. Enforcing Relational Data Integrity in PocketBase...");

    // 1. Geographies
    const geoCollection = await ensureCollection("geographies", [
        { name: "name", type: "text", required: true },
        { name: "geo_level", type: "text", required: true }
    ]);

    // 2. Candidates
    const candCollection = await ensureCollection("candidates", [
        { name: "name", type: "text", required: true },
        { name: "party", type: "text", required: true }
    ]);

    // 3. Polls (Temporal Facts)
    const pollCollection = await ensureCollection("polls", [
        { name: "pollster", type: "text", required: true },
        { name: "start_date", type: "date", required: true },
        { name: "end_date", type: "date", required: true },
        { name: "sample_size", type: "number", required: false },
        { name: "population", type: "text", required: false }
    ]);

    // 4. Poll Results (Central Fact Table with Strict Relational Integrity)
    await ensureCollection("poll_results", [
        { name: "poll_id", type: "relation", required: true, options: { collectionId: pollCollection.id, cascadeDelete: true } },
        { name: "geography_id", type: "relation", required: true, options: { collectionId: geoCollection.id, cascadeDelete: true } },
        { name: "candidate_id", type: "relation", required: true, options: { collectionId: candCollection.id, cascadeDelete: true } },
        { name: "pct", type: "number", required: true }
    ]);

    console.log("\n🎉 Idempotent Schema Enforcement Complete!");
    rl.close();
}

run().catch(e => {
    console.error(e);
    rl.close();
});
