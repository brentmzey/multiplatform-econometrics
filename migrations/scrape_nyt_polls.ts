import { chromium } from 'playwright';
import PocketBase from 'pocketbase';
import * as readline from 'readline';

const pb = new PocketBase('https://polling-data.pockethost.io');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const question = (query: string): Promise<string> => new Promise(resolve => rl.question(query, resolve));

async function run() {
    console.log("=== New York Times Polling Scraper (2026 Midterms) ===");
    
    const email = await question('Enter your PocketHost Admin Email: ');
    const password = await question('Enter your PocketHost Admin Password: ');

    console.log("\n1. Authenticating as Admin...");
    try {
        await pb.admins.authWithPassword(email, password);
        console.log("✅ Authenticated successfully.");
    } catch (e) {
        try {
            await pb.collection('_superusers').authWithPassword(email, password);
            console.log("✅ Authenticated successfully (v0.23+).");
        } catch(e2) {
            console.error("❌ Failed to authenticate as admin.");
            rl.close();
            return;
        }
    }

    console.log("\n2. Launching Playwright to bypass NYT paywall...");
    console.log("⚠️ A visible browser will open. If you see a login wall, please log in manually with your NYT subscription!");
    
    // Launch persistent context so login session is saved for future runs
    const userDataDir = './nyt_session';
    const browser = await chromium.launchPersistentContext(userDataDir, {
        headless: false // Visible so you can log in!
    });

    const page = await browser.newPage();
    await page.goto('https://www.nytimes.com/section/polls');
    
    await question('\n👉 Please log in to NYT in the browser if prompted.\nPress ENTER here when you are fully logged in and ready to scrape...');

    console.log("\n3. Navigating to 2026 Midterms Polling...");
    // Example navigation (you can expand this to crawl the sub-pages)
    await page.goto('https://www.nytimes.com/section/polls');
    
    // Wait for content to load
    await page.waitForTimeout(3000);
    
    // Example extraction: Grab all article titles and links on the polls section
    console.log("Extracting polls...");
    const polls = await page.evaluate(() => {
        const results: any[] = [];
        const links = document.querySelectorAll('a');
        links.forEach(l => {
            if (l.href.includes('/polls/') && l.innerText.trim().length > 10) {
                results.push({
                    title: l.innerText.trim(),
                    url: l.href
                });
            }
        });
        return results;
    });
    
    console.log(`✅ Found ${polls.length} poll articles.`);
    console.log(polls.slice(0, 5));

    console.log("\n4. Pushing extracted data to PocketHost...");
    // TODO: You can map these unstructured NYT interactive articles to your exact PocketHost schema here.
    // For now, we just demonstrate the successful scrape.
    console.log("✅ Ready for DB insertion logic!");
    
    await browser.close();
    rl.close();
}

run().catch(e => {
    console.error(e);
    rl.close();
});
