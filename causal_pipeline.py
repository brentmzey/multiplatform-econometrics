# /// script
# requires-python = ">=3.11"
# dependencies = [
#     "polars>=0.20.0",
#     "pyarrow>=15.0.0",
#     "linearmodels>=5.4",
#     "rich>=13.7.0",
# ]
# ///
import polars as pl
import pandas as pd
from linearmodels.iv import IV2SLS
from rich.console import Console
from rich.panel import Panel
from rich.table import Table
from rich import box
import os
from typing import List, Dict, Optional, Any

def run() -> None:
    console: Console = Console()
    console.print(Panel.fit("[bold cyan]Python Econometric Engine[/bold cyan]\n[dim]Library: Polars + LinearModels | Estimation: OLS and IV 2SLS[/dim]", border_style="cyan"))

    datasets: List[Dict[str, Any]] = [
        {"name": "Return to Education (Card)", "file": "card.csv", "y": "lwage", "endog": "educ", "iv": "nearc4", "exog": ["exper", "black", "smsa"]},
        {"name": "Female Wage (Mroz)", "file": "mroz.csv", "y": "wage", "endog": None, "iv": None, "exog": ["educ", "exper", "age", "kidslt6"]},
        {"name": "Men's Wage (Wage)", "file": "wage.csv", "y": "lwage", "endog": None, "iv": None, "exog": ["educ", "exper", "tenure", "black"]},
        {"name": "Birthweight (BW)", "file": "birthweight.csv", "y": "bwght", "endog": None, "iv": None, "exog": ["cigs", "faminc", "parity", "male"]},
    ]

    for ds in datasets:
        if not os.path.exists(str(ds["file"])):
            console.print(f"[yellow]Skipping {ds['name']}, {ds['file']} not found.[/yellow]")
            continue

        with console.status(f"[bold green]Reading {ds['file']}...", spinner="dots"):
            cols: List[str] = [str(ds["y"])] + list(ds["exog"])
            if ds["endog"]:
                cols.append(str(ds["endog"]))
            if ds["iv"]:
                cols.append(str(ds["iv"]))
            
            # Read and drop nulls
            df: pl.DataFrame = pl.read_csv(str(ds["file"]), null_values=["."])
            df = df.rename({c: c.strip() for c in df.columns})
            df = df.select([pl.col(c) for c in cols]).drop_nulls()
            df = df.with_columns(pl.lit(1.0).alias("const"))
            pdf: pd.DataFrame = df.to_pandas()
            n_rows: int = len(pdf)

        console.print(f"\n[green]✔[/green] Loaded [bold]{n_rows}[/bold] complete observations for [bold]{ds['name']}[/bold].")

        y: pd.Series = pdf[str(ds["y"])]
        controls: List[str] = ["const"] + list(ds["exog"])

        table: Table = Table(title=f"Results: {ds['name']}", box=box.ROUNDED, header_style="bold cyan", border_style="dim")
        table.add_column("Estimator", style="bold")
        table.add_column("Variable", justify="right")
        table.add_column("Estimate (β)", justify="right")
        table.add_column("Std. Error", justify="right")
        table.add_column("t-Stat", justify="right")
        table.add_column("p-value", justify="right")

        target_var: str = str(ds["endog"]) if ds["endog"] else str(ds["exog"][0])

        # 1. OLS
        ols_exog: pd.DataFrame = pdf[controls + ([str(ds["endog"])] if ds["endog"] else [])]
        ols_model: Any = IV2SLS(dependent=y, exog=ols_exog, endog=None, instruments=None).fit(cov_type="robust")
        
        table.add_row(
            "OLS", 
            target_var,
            f"{ols_model.params[target_var]:.4f}", 
            f"{ols_model.std_errors[target_var]:.4f}", 
            f"{ols_model.tstats[target_var]:.2f}", 
            f"{ols_model.pvalues[target_var]:.4f}"
        )

        # 2. IV 2SLS if applicable
        if ds["endog"] and ds["iv"]:
            iv_model: Any = IV2SLS(dependent=y, exog=pdf[controls], endog=pdf[[str(ds["endog"])]], instruments=pdf[[str(ds["iv"])]]).fit(cov_type="robust")
            table.add_row(
                "[green]IV 2SLS[/green]", 
                target_var,
                f"[bold green]{iv_model.params[str(ds['endog'])]:.4f}[/bold green]", 
                f"{iv_model.std_errors[str(ds['endog'])]:.4f}", 
                f"{iv_model.tstats[str(ds['endog'])]:.2f}", 
                f"{iv_model.pvalues[str(ds['endog'])]:.4f}"
            )

        console.print(table)

    console.print("[dim]✔ Python pipeline completed successfully.[/dim]\n")

if __name__ == "__main__":
    run()
