# Econometric & Causal Inference Suite

Welcome to the Econometric Causal Suite! This project helps you find the true "cause and effect" in data, stripping out hidden biases using advanced statistics. We use two programming languages to do this: **Kotlin** and **Python**.

Don't worry if you aren't a programmer! This guide is written so that anyone can follow along and run the analysis on their own computer.

---

## 🛠️ Step 0: What You Need (Prerequisites)

Before you begin, make sure your computer has the following installed. If you don't have them, you can click the links to install them:
1. **Python** (for running the Python scripts) - [Download Python](https://www.python.org/downloads/)
2. **uv** (a fast Python tool manager) - Open your terminal and run: `curl -LsSf https://astral.sh/uv/install.sh | sh`
3. **Java (JDK 21 or newer)** (for running Kotlin) - [Download Java](https://adoptium.net/)
4. **Gradle** (for building the Kotlin code) - [Download Gradle](https://gradle.org/install/)

Once you have these installed, open your Terminal (Mac/Linux) or Command Prompt (Windows), and make sure you are inside this project's folder.

---

## 🚀 Step-by-Step Guide

### Step 1: Download the Data
Our programs need data to analyze! We will download healthcare data directly from Data.gov, macroeconomic data from FRED (St. Louis Fed), indicator data from the World Bank, and stock market data from Yahoo Finance.

In your terminal, copy and paste these two commands, pressing **Enter** after each one:
```bash
uv run --with pandas --with pandas-datareader --with yfinance --with lxml python3 download_open_datasets.py
```
```bash
uv run --with linearmodels --with pandas python3 generate_datasets.py
```
*What this does:* It safely downloads CSV (spreadsheet) files like `nutrition_obesity.csv` and `wage.csv` into your folder.

### Step 2: Create Visual Charts (Graphs)
Let's create some beautiful graphs so we can see the data visually!
```bash
uv run --with pandas --with seaborn --with matplotlib python3 charts_pipeline.py
```
*What this does:* It creates picture files (`.svg` format) that you can open in your web browser to see the data trends.

### Step 3: Run the Python Analysis Engine
Now we will let the computer find the cause-and-effect relationships using Python.
```bash
uv run causal_pipeline.py
```
*What this does:* It reads the spreadsheets and prints out a neat table showing how much different factors (like education) actually cause an outcome (like wages).

### Step 4: Run the Kotlin Analysis Engine
We also built a lightning-fast version of the same analysis in Kotlin. Let's run it and see the results!
```bash
gradle run -q
```
*What this does:* It performs the same cause-and-effect math but uses the Java/Kotlin engine, and it will also generate some interactive HTML charts (like `nutrition_obesity_kotlin.html`) that you can double-click and open in your web browser!

---

## 📚 Want to Learn More?

If you want to understand the math, the code, or the theory behind "causal inference," we have easy-to-read guides in the `docs/` folder:

*   **[Core Suite Overview & Architecture](docs/Econometrics_Suite_Documentation.md)** - Read this first! It explains what data we are looking at and shows you how the pipeline works.
*   **[Deep Dive: Instrumental Variables & 2SLS](docs/IV_2SLS_Deep_Dive.md)** - If you want to know *how* we strip out bias (Endogeneity) to find pure causation, this explains the "2-Stage Least Squares" math in plain English.

---

## 🌐 Web Dashboard (New!)

We built a beautiful, interactive web dashboard using **FastAPI**, **HTMX**, and **Alpine.js** so you can download datasets and view the generated charts right from your browser!

### How to start the dashboard:
In your terminal, run the easy startup script:
```bash
./start_dashboard.sh
```

Once it starts, open your web browser and go to:
**[http://127.0.0.1:8000](http://127.0.0.1:8000)**

From there, you can click **Download Datasets** and **Generate Charts** to see the macroeconomic and healthcare data in action.
## 🔬 Jupyter Notebook Exploration (New!)

Want to explore the data dynamically with Python? We included an `exploration.ipynb` file!
Simply launch Jupyter or open the file in VS Code to interactively load the datasets using Pandas and run dynamic Causal Inference regressions (OLS/IV) on the fly!

---

## 📱 Kotlin Multiplatform Mobile & Desktop UI (New!)

We’ve configured **Compose Multiplatform**! The project now supports compiling the UI and backend logic out to native **Desktop (macOS, Windows, Linux)**, **iOS**, and **Android** targets.

To run the Desktop native app UI:
```bash
./start_desktop.sh
```

---

## 📦 How to Download and Install Releases

Whenever a new version of this project is ready, our GitHub Actions pipeline automatically builds all the native apps for **Desktop (macOS/Windows/Linux), Android (APK), iOS, and Web**, along with the Python Dashboard.

### Downloading the Latest Release:
1. Go to the **Releases** section on the right side of this GitHub repository.
2. Click on the latest version tag (e.g., `v1.0.0`).
3. Under the **Assets** dropdown, download the `econometrics-suite-release.zip` file.

### Installation Instructions:

**Python Web Dashboard (Local Server)**
1. Extract the downloaded `.zip` folder.
2. Inside the `python` folder, you will find `app.py` and the data generating scripts.
3. Simply run `./start_dashboard.sh` (or `uv run uvicorn app:app --reload` manually).
4. Open your browser to `http://127.0.0.1:8000`.

**Desktop App (Kotlin Multiplatform JVM)**
1. Inside the `kotlin` folder, locate the extracted `build/libs` directory.
2. Run the executable `.jar` file by double-clicking it, or by running `java -jar <filename>.jar` in your terminal. This will launch the native desktop Compose application!

**Android App**
1. Inside the `kotlin/build/outputs/apk/debug/` folder, find the `.apk` file.
2. Transfer this file to your Android phone or an Android emulator.
3. Open it on the phone to install the Econometrics Mobile App. (You may need to allow "Install from Unknown Sources" in your settings).
