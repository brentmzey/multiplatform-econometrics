#!/bin/bash
echo "Starting FastAPI Web Dashboard on port 8000..."
uv run --with httpx --with fastapi --with uvicorn --with jinja2 --with python-multipart --with pandas --with pandas-datareader --with yfinance --with lxml --with seaborn --with matplotlib --with jupyter --with kotlin-jupyter-kernel uvicorn app:app --reload
