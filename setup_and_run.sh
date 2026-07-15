#!/bin/bash
set -e

echo "=========================================================="
echo "    🚀 Econometrics Pipeline Setup & Execution Script"
echo "=========================================================="

export POCKETHOST_ADMIN_EMAIL="${POCKETHOST_ADMIN_EMAIL:-brentmzey4795@gmail.com}"
export POCKETHOST_ADMIN_PASSWORD="${POCKETHOST_ADMIN_PASSWORD:-MHD@hyt0arm8dvf5awc}"

echo ""
echo "1️⃣  Installing dependencies for PocketHost Migration scripts..."
cd migrations
bun install
cd ..

echo ""
echo "2️⃣  Enforcing strict Relational Schema on PocketHost..."
cd migrations
bun run pocketbase_schema.ts
cd ..

echo ""
echo "3️⃣  Hydrating PocketHost with NYT Polling Data..."
echo "(This script uses exponential backoff to respect Free Tier limits)"
cd migrations
bun run scrape_nyt_polls.ts
cd ..

echo ""
echo "4️⃣  Running Kotlin Econometric Math Test Suite (2SLS & Hausman)..."
./gradlew test

echo ""
echo "5️⃣  Launching Compose Desktop Dashboard..."
./start_desktop.sh
