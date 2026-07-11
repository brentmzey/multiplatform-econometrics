import PocketBase from 'pocketbase';
import * as readline from 'readline';

const pb = new PocketBase('https://econometrics-broker.pockethost.io');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const question = (query: string): Promise<string> => new Promise(resolve => rl.question(query, resolve));

async function runMigration() {
    console.log("=== PocketBase Setup Migration ===");
    const email = await question("Enter your PocketHost Admin Email: ");
    const password = await question("Enter your PocketHost Admin Password: ");
    
    try {
        console.log("\n1. Authenticating as Admin...");
        await pb.admins.authWithPassword(email, password);
        console.log("✅ Admin authentication successful.");
        
        console.log("\n2. Creating test user for the App (admin@demo.com)...");
        try {
            await pb.collection('users').create({
                email: 'admin@demo.com',
                password: 'password1234',
                passwordConfirm: 'password1234',
                name: 'Demo Admin'
            });
            console.log("✅ Test user created.");
        } catch (e: any) {
            console.log("⚠️ Test user might already exist. Skipping.");
        }

        console.log("\n3. Creating 'datasets' collection...");
        try {
            // Note: PocketBase admin API uses the /api/collections endpoint to create schemas
            await pb.collections.create({
                name: 'datasets',
                type: 'base',
                schema: [
                    { name: 'name', type: 'text', required: true },
                    { name: 'headers', type: 'json', required: true },
                    { name: 'rows', type: 'json', required: true }
                ],
                // API Rules: Only logged-in users can view or list datasets
                listRule: '@request.auth.id != ""',
                viewRule: '@request.auth.id != ""'
            });
            console.log("✅ 'datasets' collection created.");
        } catch (e: any) {
            console.log("⚠️ 'datasets' collection might already exist. Proceeding.");
        }

        console.log("\n4. Seeding Demo Data (David Card's Returns to Education)...");
        const headers = ["lwage", "educ", "exper", "black", "smsa", "nearc4"];
        const rows = [
            [5.5, 12, 16, 1, 1, 1],
            [6.1, 16, 9, 0, 1, 1],
            [5.8, 14, 12, 0, 1, 1],
            [6.5, 18, 5, 0, 1, 1],
            [5.2, 10, 20, 1, 0, 0],
            [5.9, 16, 8, 0, 0, 1],
            [6.2, 14, 14, 0, 1, 0],
            [5.6, 12, 15, 1, 1, 1],
            [6.0, 16, 10, 0, 1, 1],
            [5.4, 12, 18, 1, 0, 0]
        ];

        try {
            await pb.collection('datasets').create({
                name: 'Card Education Sample',
                headers: headers,
                rows: rows
            });
            console.log("✅ Seed data inserted successfully.");
        } catch (e: any) {
            console.log("❌ Failed to insert seed data:", e.message);
        }

        console.log("\n🎉 PocketBase Migration Complete! You can now log in via the Compose App.");
    } catch (e: any) {
        console.error("❌ Migration Failed:", e.message);
    } finally {
        rl.close();
    }
}

runMigration();
