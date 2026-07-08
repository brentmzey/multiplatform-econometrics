import pandas as pd
import linearmodels.datasets.mroz as mroz
import linearmodels.datasets.wage as wage
import linearmodels.datasets.birthweight as bw

def generate_datasets() -> None:
    # 1. Mroz: Female Labor Supply
    df_mroz: pd.DataFrame = mroz.load()
    df_mroz.to_csv("mroz.csv", index=False)
    print("Saved mroz.csv")

    # 2. Wage
    df_wage: pd.DataFrame = wage.load()
    df_wage.to_csv("wage.csv", index=False)
    print("Saved wage.csv")

    # 3. Birthweight
    df_bw: pd.DataFrame = bw.load()
    df_bw.to_csv("birthweight.csv", index=False)
    print("Saved birthweight.csv")

if __name__ == "__main__":
    generate_datasets()
