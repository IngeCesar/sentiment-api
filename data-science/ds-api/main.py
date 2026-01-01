import os
import joblib
import re
import numpy as np
import uvicorn
import nltk
import sys
from datetime import datetime
from fastapi import FastAPI, HTTPException, Response, status
from pydantic import BaseModel
from nltk.corpus import stopwords

# --- NLTK PATH CONFIGURATION (Fix for Docker) ---
# Explicitly tell NLTK where the data was downloaded in the Dockerfile
nltk.data.path.append('/usr/local/share/nltk_data')

# ==========================================
# 1. SERVER SETUP & INITIALIZATION
# ==========================================
app = FastAPI(title="SentimentAPI - Team 55", version="0.1 alpha")

# --- PATH RESOLUTION ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_FILENAME = "sentiment_model_alpha.joblib"
# Points to ../models/ relative to the script location
MODEL_PATH = os.path.join(BASE_DIR, "..", "models", MODEL_FILENAME)
MODEL_PATH = os.path.normpath(MODEL_PATH)

print(f"📍 API Directory: {BASE_DIR}")

# --- RESOURCE LOADING ---

# A. Stopwords (Visual Filter - Extended)
try:
    # This will now look specifically in /usr/local/share/nltk_data
    base_stopwords = set(stopwords.words('spanish'))

    # Custom Blacklist: Words that are mathematically useful but visually noise
    custom_stopwords = {
        'dio', 'da', 'dan', 'dado',       # Verb dar
        'fue', 'fui', 'sido', 'es',       # Verb ser
        'hizo', 'hace', 'hacen',          # Verb hacer
        'tengo', 'tiene', 'tienen',       # Verb tener
        'puedo', 'puede',                 # Verb poder
        'voy', 'va', 'vamos',             # Verb ir
        'aqui', 'ahora', 'hoy',           # Adverbs
        'producto', 'compra', 'amazon'    # Context words
    }

    BLACKLIST_VISUAL = base_stopwords.union(custom_stopwords)
    print("✅ NLTK Stopwords + Custom Blacklist loaded from explicit path.")

except Exception as e:
    print(f"⚠️ NLTK Error: {e}. Attempting emergency load...")
    BLACKLIST_VISUAL = set()

# B. Model Loading
try:
    model_pipeline = joblib.load(MODEL_PATH)
    print(f"✅ Pipeline loaded successfully.")
except FileNotFoundError:
    print(f"❌ CRITICAL ERROR: Model not found at {MODEL_PATH}")
    model_pipeline = None

# ==========================================
# 2. CORE LOGIC (Safety & Integrity)
# ==========================================

def clean_text_for_prediction(text):
    """
    Cleaning V2.1: Lowercase + Letters + Spanish Chars (ñ, ü).
    """
    if not isinstance(text, str): return ""
    text = text.lower()
    # Updated Regex to include 'ü' and 'ñ'
    text = re.sub(r'[^a-záéíóúñü\s]', '', text)
    text = " ".join(text.split())
    return text

# --- STARTUP INTEGRITY CHECK ---
def run_integrity_tests():
    test_phrase = "El producto está dañado y es una vergüenza"
    expected = "el producto está dañado y es una vergüenza"
    result = clean_text_for_prediction(test_phrase)

    if result != expected:
        print("\n🚨 INTEGRITY CHECK FAILED 🚨")
        print(f"Expected: '{expected}' | Got: '{result}'")
        sys.exit(1) # Fail Fast
    else:
        print("✅ Integrity Check Passed: Spanish characters preserved.")

run_integrity_tests()

# ==========================================
# 3. HELPER FUNCTIONS
# ==========================================

def extract_keywords_clean(clean_text, pipeline, top_n=3):
    if pipeline is None: return []
    try:
        vectorizer = pipeline.named_steps['tfidf']
        clf = pipeline.named_steps['clf']

        prediction = pipeline.predict([clean_text])[0]
        class_idx = list(clf.classes_).index(prediction)

        tfidf_vector = vectorizer.transform([clean_text])
        feature_names = vectorizer.get_feature_names_out()

        candidates = {}
        for col_idx in tfidf_vector.nonzero()[1]:
            word = feature_names[col_idx]
            # Assumes binary classification or access to coef_[class_idx]
            # Note: For multi-class, coef_ has shape [n_classes, n_features]
            if hasattr(clf, 'coef_'):
                coef = clf.coef_[class_idx][col_idx]
                if coef > 0:
                    candidates[word] = coef
            else:
                # Fallback if coef_ is not available (e.g., Random Forest)
                candidates[word] = tfidf_vector[0, col_idx]

        sorted_candidates = sorted(candidates.items(), key=lambda item: item[1], reverse=True)

        final_keywords = []
        for word, score in sorted_candidates:
            if word not in BLACKLIST_VISUAL:
                final_keywords.append(word)
            if len(final_keywords) == top_n:
                break
        return final_keywords
    except Exception as e:
        print(f"⚠️ Keyword Error: {e}")
        return []

# ==========================================
# 4. API ENDPOINTS
# ==========================================

class ReviewRequest(BaseModel):
    """Schema for incoming sentiment analysis requests."""
    text: str

class SentimentResponse(BaseModel):
    """Schema for outgoing sentiment analysis results."""
    prediction: str
    probability: float
    keywords: list[str]
    timestamp: str

# --- USER ENDPOINT (Landing) ---
@app.get("/")
def home():
    """
    User-friendly entry point to verify the container is responding.
    Provides basic organization and product metadata.
    """
    return {
        "organization": "CesiumFlow",
        "product": "SentimentAPI",
        "team": "Team 55",
        "version": "0.1-alpha",
        "documentation": "/docs"
    }

# --- SYSTEM ENDPOINT (Health Check for Actuator) ---
@app.get("/health")
def health_check(response: Response):
    """
    Technical endpoint for Spring Boot Actuator and Docker Engine.
    Returns 200 OK if the model is ready, 503 Service Unavailable otherwise.
    """
    if model_pipeline is not None:
        return {
            "status": "UP",
            "model_loaded": True,
            "engine": "FastAPI",
            "uptime_check": "passed"
        }
    else:
        # 🚨 Set 503 status so the Java WebClient/Actuator detects the failure immediately
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE
        return {
            "status": "DOWN",
            "model_loaded": False,
            "error": "ML model pipeline not found or failed to load",
            "uptime_check": "failed"
        }

@app.post("/predict", response_model=SentimentResponse)
def predict_sentiment(request: ReviewRequest):
    """
    Main Prediction Endpoint.
    Simplification: Removed APIRouter and Logging library for directness.
    """

    # 1. Availability Check
    if not model_pipeline:
        print("🚨 Error: Model not loaded.")
        raise HTTPException(status_code=503, detail="Model is offline.")

    # 2. Validation
    if not request.text or len(request.text.strip()) < 2:
        print(f"⚠️ Warning: Received invalid text: {request.text}")
        raise HTTPException(status_code=400, detail="Text is too short.")

    try:
        # 3. Processing & Inference
        cleaned_text = clean_text_for_prediction(request.text)
        if not cleaned_text:
            cleaned_text = request.text.lower()

        prediction_label = model_pipeline.predict([cleaned_text])[0]
        probabilities = model_pipeline.predict_proba([cleaned_text])[0]
        confidence_score = float(np.max(probabilities))

        keywords = extract_keywords_clean(cleaned_text, model_pipeline)

        # Simple print for Docker logs
        print(f"✅ Prediction Success: {prediction_label} ({confidence_score:.2f})")

        return {
            "prediction": str(prediction_label),
            "probability": round(confidence_score, 2),
            "keywords": keywords,
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        print(f"🔥 Unexpected Error: {str(e)}")
        raise HTTPException(status_code=500, detail="Inference failed.")

if __name__ == "__main__":
    print("🚀 Starting Server on Port 5000...")
    uvicorn.run(app, host="0.0.0.0", port=5000)
