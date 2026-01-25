"""Test rápido de la API FastAPI"""
import requests
import json

API_URL = "http://localhost:5000"

print("=" * 70)
print("TEST DE LA API FASTAPI")
print("=" * 70)

# Test 1: Health check
print("\n[1/4] Health Check...")
response = requests.get(f"{API_URL}/health")
print(f"  Status: {response.status_code}")
print(f"  Response: {response.json()}")

# Test 2: Predicción positiva
print("\n[2/4] Test Predicción Positiva...")
data = {"text": "Excelente producto, muy recomendado", "top_n_keywords": 5}
response = requests.post(f"{API_URL}/predict", json=data)
print(f"  Status: {response.status_code}")
result = response.json()
print(f"  Predicción: {result['prediction']}")
print(f"  Probabilidad: {result['probability']}")
print(f"  Keywords: {result['keywords']}")

# Test 3: Predicción negativa
print("\n[3/4] Test Predicción Negativa...")
data = {"text": "Pésimo servicio, muy decepcionado"}
response = requests.post(f"{API_URL}/predict", json=data)
print(f"  Status: {response.status_code}")
result = response.json()
print(f"  Predicción: {result['prediction']}")
print(f"  Probabilidad: {result['probability']}")
print(f"  Keywords: {result['keywords']}")

# Test 4: Predicción neutra
print("\n[4/4] Test Predicción Neutra...")
data = {"text": "Producto normal, cumple su función"}
response = requests.post(f"{API_URL}/predict", json=data)
print(f"  Status: {response.status_code}")
result = response.json()
print(f"  Predicción: {result['prediction']}")
print(f"  Probabilidad: {result['probability']}")
print(f"  Keywords: {result['keywords']}")

print("\n" + "=" * 70)
print("✅ TODOS LOS TESTS COMPLETADOS")
print("=" * 70)
