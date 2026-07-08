import pandas as pd
import urllib.request
import os

url = "https://data.cdc.gov/api/views/hn4x-zwk7/rows.csv?accessType=DOWNLOAD"
filename = "nutrition_obesity.csv"

print(f"Downloading {url} ...")
# Download the first few MBs or read directly using pandas with nrows
try:
    df = pd.read_csv(url, nrows=2000)
    # Clean column names
    df.columns = [c.strip().lower().replace(" ", "_") for c in df.columns]
    
    # Select some useful columns for a dummy regression
    cols_to_keep = ["yearstart", "locationdesc", "class", "question", "data_value", "sample_size"]
    df = df[[c for c in cols_to_keep if c in df.columns]].dropna()
    
    df.to_csv(filename, index=False)
    print(f"Saved {len(df)} rows to {filename}")
except Exception as e:
    print(f"Error downloading: {e}")
