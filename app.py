from fastapi import FastAPI, Request
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
import os
import download_open_datasets
import charts_pipeline

app = FastAPI(title="Econometrics Dashboard")

# Ensure static directory exists
os.makedirs("static", exist_ok=True)

app.mount("/static", StaticFiles(directory="static"), name="static")
# Mount root directory to serve SVGs directly for now
app.mount("/assets", StaticFiles(directory="."), name="assets")

templates = Jinja2Templates(directory="templates")

@app.get("/", response_class=HTMLResponse)
async def read_root(request: Request):
    charts = [f for f in os.listdir('.') if f.endswith('.svg')]
    return templates.TemplateResponse(request=request, name="index.html", context={"charts": charts})

@app.post("/download", response_class=HTMLResponse)
async def download_data(request: Request):
    try:
        download_open_datasets.download_datagov()
        download_open_datasets.download_fred()
        download_open_datasets.download_worldbank()
        return "<div class='alert success-alert'>✅ Datasets downloaded successfully!</div>"
    except Exception as e:
        return f"<div class='alert error-alert'>❌ Error downloading data: {e}</div>"

@app.post("/generate-charts", response_class=HTMLResponse)
async def generate_charts(request: Request):
    try:
        charts_pipeline.run()
        charts = [f for f in os.listdir('.') if f.endswith('.svg')]
        return templates.TemplateResponse(request=request, name="chart_gallery.html", context={"charts": charts})
    except Exception as e:
        return f"<div class='alert error-alert'>❌ Error generating charts: {e}</div>"

import httpx

@app.post("/backend-stats", response_class=HTMLResponse)
async def get_backend_stats(request: Request):
    query = """
    query {
      summaryStats(dataset: "wage.csv", variables: ["wage", "educ"]) {
        filename
        nobs
        stats {
          variable
          mean
          stdDev
        }
      }
    }
    """
    try:
        # Check if wage.csv exists
        if not os.path.exists("wage.csv"):
            return "<div class='alert error-alert'>❌ wage.csv not found. Run dataset generation first.</div>"
            
        async with httpx.AsyncClient() as client:
            response = await client.post("http://localhost:8080/graphql", json={"query": query})
            
        if response.status_code == 200:
            data = response.json()
            if "errors" in data:
                return f"<div class='alert error-alert'>❌ GraphQL Error: {data['errors']}</div>"
            
            stats = data["data"]["summaryStats"]
            html = f"<div class='alert success-alert'>✅ <strong>Backend Connected!</strong><br/>"
            html += f"Dataset: {stats['filename']} (N={stats['nobs']})<br/>"
            for s in stats["stats"]:
                html += f"• {s['variable']}: Mean = {s['mean']:.2f}, StdDev = {s['stdDev']:.2f}<br/>"
            html += "</div>"
            return html
        else:
            return f"<div class='alert error-alert'>❌ Backend returned {response.status_code}</div>"
    except httpx.RequestError as e:
        return f"<div class='alert error-alert'>❌ Failed to connect to Kotlin backend (is it running?): {e}</div>"
    except Exception as e:
        return f"<div class='alert error-alert'>❌ Error: {e}</div>"

import subprocess

@app.post("/launch-jupyter", response_class=HTMLResponse)
async def launch_jupyter(request: Request):
    try:
        subprocess.Popen(
            ["uv", "run", "--with", "jupyter", "--with", "kotlin-jupyter-kernel", "jupyter", "lab", "--no-browser"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
        html = "<div class='alert success-alert'>✅ <strong>Jupyter Lab Started!</strong><br/>"
        html += "<a href='http://localhost:8888' target='_blank' style='color: white; text-decoration: underline;'>Click here to open Jupyter Lab for Python & Kotlin Exploration</a></div>"
        return html
    except Exception as e:
        return f"<div class='alert error-alert'>❌ Error starting Jupyter: {e}</div>"


