from fastapi.testclient import TestClient
from app import app
import os
import pytest

client = TestClient(app)

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
    assert "Econometrics Explorer" in response.text

def test_generate_charts():
    # If the CSVs exist, this should work
    response = client.post("/generate-charts")
    assert response.status_code == 200
    if "nutrition_obesity.csv" in os.listdir('.'):
        assert "nutrition_obesity_python" in response.text or "svg" in response.text
