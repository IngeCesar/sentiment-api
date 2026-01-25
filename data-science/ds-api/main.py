import os
import sys
import uvicorn
from datetime import datetime
from typing import List, Optional
from fastapi import FastAPI, HTTPException, Response, status
from pydantic import BaseModel

# --- INTEGRACIÓN CON NUEVO PREDICTOR DE PRODUCCIÓN ---
# Agregamos la carpeta 'src' al path para importar módulos
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_PATH = os.path.join(BASE_DIR, "..", "src")
sys.path.append(SRC_PATH)

try:
    # Importamos el SentimentPredictor validado en producción
    from cesiumflow_ml.predictor import SentimentPredictor
except ImportError as e:
    print(f"❌ Error al importar SentimentPredictor: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

# ==========================================
# 2. APP CONFIGURATION
# ==========================================
app = FastAPI(
    title="SentimentAPI - CesiumFlow",
    version="2.0-ProductionModel",
    description="API de clasificación de sentimientos con modelo validado (83.33% accuracy)"
)

# Contexto global para el predictor
ml_context = {"predictor": None, "status": "initializing"}

@app.on_event("startup")
async def startup_event():
    """Inicializar el predictor al arrancar la aplicación."""
    try:
        print("🔄 Inicializando SentimentPredictor...")
        ml_context["predictor"] = SentimentPredictor()
        ml_context["status"] = "ready"
        print("✅ Motor de ML cargado correctamente (Modelo de Producción v2.0)")
        print("   - Accuracy: 83.33%")
        print("   - F1-Macro: 0.8344")
        print("   - Tiempo inferencia: ~3.88ms/predicción")
    except Exception as e:
        ml_context["status"] = "failed"
        print(f"❌ Error al cargar el modelo: {e}")
        import traceback
        traceback.print_exc()

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
    """
    Endpoint principal de predicción de sentimientos.
    
    Mantiene compatibilidad total con la API anterior.
    Ahora usa el modelo de producción validado (83.33% accuracy).
    """
    if ml_context["status"] != "ready":
        raise HTTPException(
            status_code=503,
            detail="Model not ready. Service is initializing or failed to load."
        )

    try:
        # Obtener predictor
        predictor = ml_context["predictor"]
        
        # Realizar predicción (devuelve PredictionResult)
        resultado = predictor.predict(request.text)
        
        # Convertir a formato de respuesta de la API
        # Limitar keywords según top_n_keywords
        top_n = request.top_n_keywords or 5
        keywords = resultado.keywords[:top_n] if resultado.keywords else []
        
        return SentimentResponse(
            prediction=resultado.prediction,
            probability=round(resultado.probability, 2),
            keywords=keywords,
            timestamp=resultado.timestamp
        )
        
    except ValueError as e:
        # Errores de validación (texto vacío, None, etc.)
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        # Errores internos del modelo
        print(f"🔥 Error en predicción: {e}")
        import traceback
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Inference error: {str(e)}"
        )

@app.get("/health")
def health():
    """Health check endpoint."""
    return {
        "status": "UP" if ml_context["status"] == "ready" else "DOWN",
        "version": "2.0-ProductionModel",
        "model_ready": ml_context["status"] == "ready"
    }

if __name__ == "__main__":
    # En la nube, Railway inyecta la variable PORT.
    # En local, esa variable no existe, así que usamos 5000 por defecto.
    port = int(os.environ.get("PORT", 5000))

    print(f"🚀 Iniciando servidor en el puerto: {port}")
    uvicorn.run(app, host="0.0.0.0", port=port)
