import pandas as pd
import pandas_datareader.data as web
import datetime
import os
from typing import List

def download_datagov() -> None:
    url: str = "https://data.cdc.gov/api/views/hn4x-zwk7/rows.csv?accessType=DOWNLOAD"
    filename: str = "nutrition_obesity.csv"
    print(f"Downloading {url} ...")
    try:
        df: pd.DataFrame = pd.read_csv(url, nrows=2000)
        df.columns = [str(c).strip().lower().replace(" ", "_") for c in df.columns]
        cols_to_keep: List[str] = ["yearstart", "locationdesc", "class", "question", "data_value", "sample_size"]
        df = df[[c for c in cols_to_keep if c in df.columns]].dropna()
        df.to_csv(filename, index=False)
        print(f"Saved {len(df)} rows to {filename}")
    except Exception as e:
        print(f"Error downloading Data.gov: {e}")

def download_fred() -> None:
    print("Downloading FRED (St. Louis Fed) macroeconomic data...")
    start = datetime.datetime(2000, 1, 1)
    end = datetime.datetime.today()
    try:
        # GDP: Gross Domestic Product, UNRATE: Unemployment Rate, CPIAUCSL: Consumer Price Index
        df_fred = web.DataReader(['GDP', 'UNRATE', 'CPIAUCSL'], 'fred', start, end)
        df_fred.reset_index(inplace=True)
        # Drop rows where all economic indicators are NaN
        df_fred.dropna(subset=['GDP', 'UNRATE', 'CPIAUCSL'], how='all', inplace=True)
        filename = "fred_macro.csv"
        df_fred.to_csv(filename, index=False)
        print(f"Saved FRED data to {filename}")
    except Exception as e:
        print(f"Error downloading FRED data: {e}")

def download_worldbank() -> None:
    from pandas_datareader import wb
    print("Downloading World Bank indicator data (GDP per capita for US, CN, DE)...")
    try:
        # NY.GDP.PCAP.CD = GDP per capita (current US$)
        df_wb = wb.download(indicator='NY.GDP.PCAP.CD', country=['US', 'CN', 'DE'], start=2000, end=datetime.datetime.today().year)
        df_wb.reset_index(inplace=True)
        filename = "worldbank_gdp.csv"
        df_wb.to_csv(filename, index=False)
        print(f"Saved World Bank data to {filename}")
    except Exception as e:
        print(f"Error downloading World Bank data: {e}")

def download_yfinance() -> None:
    import yfinance as yf
    print("Downloading Yahoo Finance data (S&P 500 and Tech Stocks)...")
    try:
        # Download S&P 500 (SPY), Apple, Microsoft
        tickers = ["SPY", "AAPL", "MSFT"]
        df_yf = yf.download(tickers, start="2015-01-01", end=datetime.datetime.today().strftime('%Y-%m-%d'))
        # Flatten multi-level columns if they exist
        if isinstance(df_yf.columns, pd.MultiIndex):
            df_yf.columns = ['_'.join(col).strip() for col in df_yf.columns.values]
        df_yf.reset_index(inplace=True)
        filename = "yfinance_stocks.csv"
        df_yf.to_csv(filename, index=False)
        print(f"Saved Yahoo Finance data to {filename}")
    except Exception as e:
        print(f"Error downloading Yahoo Finance data: {e}")

if __name__ == "__main__":
    download_datagov()
    download_fred()
    download_worldbank()
    download_yfinance()
