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
Our programs need data to analyze! We will download healthcare data directly from Data.gov and other public datasets. 

In your terminal, copy and paste these two commands, pressing **Enter** after each one:
```bash
uv run --with pandas python3 download_datagov.py
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

## 🌟 What's Next? (Future Ideas)
Here are a few ways we could expand this project:
1. **Live Data Connections**: Instead of downloading files, we could connect directly to hospital databases to get live data.
2. **Web Dashboard**: We could build a website where you can upload your own data and click a button to see the results, without needing the terminal!
