import linearmodels.datasets.mroz as mroz
import linearmodels.datasets.wage as wage
import linearmodels.datasets.birthweight as bw

# 1. Mroz: Female Labor Supply
df_mroz = mroz.load()
df_mroz.to_csv("mroz.csv", index=False)
print("Saved mroz.csv")

# 2. Wage
df_wage = wage.load()
df_wage.to_csv("wage.csv", index=False)
print("Saved wage.csv")

# 3. Birthweight
df_bw = bw.load()
df_bw.to_csv("birthweight.csv", index=False)
print("Saved birthweight.csv")
