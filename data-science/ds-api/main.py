import os
import sys
import uvicorn
from datetime import datetime
from typing import List, Optional
from fastapi import FastAPI, HTTPException, Response, status
from pydantic import BaseModel

# --- IMPORTACIÓN DIRECTA DEL ARCHIVO ORIGINAL ---
# Agregamos la carpeta 'src' al path para que Python vea a 'sentiment_api.py'
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_PATH = os.path.join(BASE_DIR, "..", "src")
sys.path.append(SRC_PATH)

try:
    # Importamos las funciones con los nombres EXACTOS del archivo original
    from sentiment_api import predecir_sentimiento, cargar_modelo
except ImportError as e:
    print(f"❌ Error al importar sentiment_api.py: {e}")
    sys.exit(1)

# ==========================================
# 2. APP CONFIGURATION
# ==========================================
app = FastAPI(title="SentimentAPI - CesiumFlow", version="0.1-OriginalLogic")

MODELS_DIR = os.path.join(BASE_DIR, "..", "models")
MODEL_PATH = os.path.join(MODELS_DIR, "sentiment_model.joblib")
VECT_PATH = os.path.join(MODELS_DIR, "tfidf_vectorizer.joblib")

ml_context = {"model": None, "vectorizer": None, "status": "initializing"}

@app.on_event("startup")
async def startup_event():
    try:
        # Usamos la función cargar_modelo del archivo original
        model, vectorizer = cargar_modelo(MODEL_PATH, VECT_PATH)
        ml_context["model"] = model
        ml_context["vectorizer"] = vectorizer
        ml_context["status"] = "ready"
        print(f"✅ Motor cargado usando lógica original.")
    except Exception as e:
        ml_context["status"] = "failed"
        print(f"❌ Error: {e}")

# ==========================================
# 3. CONTRATO DE API (DTOs)
# ==========================================
class ReviewRequest(BaseModel):
    text: str
    top_n_keywords: Optional[int] = 5

class SentimentResponse(BaseModel):
    prediction: str
    probability: float
    keywords: List[str]
    timestamp: str

# ==========================================
# 4. ENDPOINTS
# ==========================================
@app.post("/predict", response_model=SentimentResponse)
def predict_endpoint(request: ReviewRequest):
    if ml_context["status"] != "ready":
        raise HTTPException(status_code=503, detail="Model not ready")

    try:
        # LLAMADA A LA LÓGICA ORIGINAL
        # 'predecir_sentimiento' es la función del archivo sentiment_api.py
        resultado = predecir_sentimiento(
            texto=request.text,
            modelo=ml_context["model"],
            vectorizador=ml_context["vectorizer"],
            top_n_keywords=request.top_n_keywords or 5
        )

        return {
            "prediction": resultado['prediction'],
            "probability": round(resultado['probability'], 2),
            "keywords": resultado['keywords'],
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        print(f"🔥 Error en lógica original: {e}")
        raise HTTPException(status_code=500, detail="Inference Error")

@app.get("/health")
def health():
    return {"status": "UP" if ml_context["status"] == "ready" else "DOWN"}

if __name__ == "__main__":
    # En la nube, Railway inyecta la variable PORT.
    # En local, esa variable no existe, así que usamos 5000 por defecto.
    port = int(os.environ.get("PORT", 5000))

    print(f"🚀 Iniciando servidor en el puerto: {port}")
    uvicorn.run(app, host="0.0.0.0", port=port)
