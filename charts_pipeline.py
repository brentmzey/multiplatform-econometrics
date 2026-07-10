import pandas as pd
import seaborn as sns
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import os
import numpy as np
from typing import Optional

def run() -> None:
    print("Generating Data.gov Nutrition/Obesity Visualizations in Python...")
    
    if os.path.exists("nutrition_obesity.csv"):
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
    else:
        print("nutrition_obesity.csv not found.")

    if os.path.exists("fred_macro.csv"):
        print("Generating FRED Macroeconomic Visualizations...")
        df_fred = pd.read_csv("fred_macro.csv")
        df_fred['DATE'] = pd.to_datetime(df_fred['DATE'])
        
        plt.figure(figsize=(12, 6))
        
        # Plot UNRATE on left y-axis
        ax1 = sns.lineplot(data=df_fred, x="DATE", y="UNRATE", color="blue", label="Unemployment Rate (%)")
        ax1.set_ylabel("Unemployment Rate (%)", color="blue")
        
        # Plot GDP on right y-axis
        ax2 = plt.twinx()
        sns.lineplot(data=df_fred, x="DATE", y="GDP", color="red", ax=ax2, label="GDP (Billions $)")
        ax2.set_ylabel("GDP", color="red")
        
        plt.title("FRED: Unemployment Rate vs GDP over Time")
        plt.tight_layout()
        plt.savefig("fred_macro_python.svg")
        print("Saved fred_macro_python.svg")

    if os.path.exists("worldbank_gdp.csv"):
        print("Generating World Bank Visualizations...")
        df_wb = pd.read_csv("worldbank_gdp.csv")
        
        plt.figure(figsize=(10, 6))
        sns.lineplot(data=df_wb, x="year", y="NY.GDP.PCAP.CD", hue="country", marker="o")
        plt.title("World Bank: GDP per Capita")
        plt.xlabel("Year")
        plt.ylabel("GDP per Capita (Current US$)")
        plt.grid(True)
        plt.tight_layout()
        plt.savefig("worldbank_gdp_python.svg")
        print("Saved worldbank_gdp_python.svg")

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
