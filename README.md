# Econometric & Causal Inference Suite

Welcome to the Econometric Causal Suite! This project provides a robust, multi-language pipeline (Kotlin and Python) for executing advanced econometric regressions, isolating causal effects, and rendering gorgeous statistical visualizations.

## 📚 Central Documentation Hub

All deep-dive documentation and architecture overviews have been localized to the `docs/` folder for easy reference.

*   **[Core Suite Overview & Architecture](docs/Econometrics_Suite_Documentation.md)**
    *   Read this first. It explains the datasets used, contains Mermaid architecture diagrams of the pipeline, and shows the exact Kotlin regression code alongside embedded statistical charts.
*   **[Deep Dive: Instrumental Variables & 2SLS](docs/IV_2SLS_Deep_Dive.md)**
    *   A mathematical and programmatic deep dive. If you want to understand Omitted Variable Bias (Endogeneity) and exactly how the 2-Stage Least Squares (2SLS) algorithm mathematically strips out bias to find *pure causation*, read this.

## 🚀 Quick Start Guide

### 1. Download the Datasets
Before running the regression engines, pull down the public datasets (including data directly from Data.gov):
```bash
uv run --with pandas python3 download_datagov.py
uv run --with linearmodels --with pandas python3 generate_datasets.py
```

### 2. Run the Causal Engines
Run the pipelines to calculate and compare Naive OLS estimates against Unbiased IV-2SLS estimates across the datasets.
**Kotlin Pipeline (Fast JVM with Mordant Tables):**
```bash
gradle run -q
```
**Python Pipeline (Polars & LinearModels):**
```bash
uv run causal_pipeline.py
```

### 3. Generate Visualizations
We generate statistical SVGs to visualize the data natively:
```bash
uv run --with pandas --with seaborn --with matplotlib python3 charts_pipeline.py
```

---

## What's Next? (Suggestions to expand the suite)
Here are a few ways we could continue building this project out:

1. **API Integration (VA Mobile Health / Data.gov)**: We can build native Kotlin API clients (using Ktor) to stream live healthcare data continuously, rather than static CSV downloads.
2. **Advanced Modeling**: Implement *Difference-in-Differences (DiD)* or *Regression Discontinuity Design (RDD)* in Kotlin.
3. **Web Dashboard**: Use Kotlin Multiplatform or Spring Boot to serve these gorgeous lets-plot charts and Mordant tables to a live web dashboard. 
