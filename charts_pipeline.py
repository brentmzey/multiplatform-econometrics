import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os
import numpy as np
from typing import Optional

def run() -> None:
    print("Generating Data.gov Nutrition/Obesity Visualizations in Python...")
    
    if not os.path.exists("nutrition_obesity.csv"):
        print("nutrition_obesity.csv not found. Run download_datagov.py first.")
        return

    df: pd.DataFrame = pd.read_csv("nutrition_obesity.csv")
    
    # 1. Boxplot of Data Values by Class
    plt.figure(figsize=(10, 6))
    sns.boxplot(data=df, x="class", y="data_value", palette="Set3")
    sns.stripplot(data=df, x="class", y="data_value", color=".25", alpha=0.5, size=4)
    plt.title("Nutrition, Physical Activity, and Obesity (Data.gov)\nValues by Class")
    plt.xlabel("Class Category")
    plt.ylabel("Data Value (%)")
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig("nutrition_obesity_python.svg")
    print("Saved nutrition_obesity_python.svg")

    # 2. Synthetic Econometric Regression Chart
    np.random.seed(42)
    x: np.ndarray = np.random.normal(50, 10, 200)
    y: np.ndarray = x * 0.8 + np.random.normal(10, 5, 200)
    
    plt.figure(figsize=(8, 6))
    sns.regplot(x=x, y=y, color="#ef4444", scatter_kws={"color": "#10b981", "alpha": 0.6})
    plt.title("Econometric Correlation: Health Index vs Outcome")
    plt.xlabel("Health Index")
    plt.ylabel("Outcome")
    plt.tight_layout()
    plt.savefig("econometric_regression_python.svg")
    print("Saved econometric_regression_python.svg")

if __name__ == "__main__":
    run()
