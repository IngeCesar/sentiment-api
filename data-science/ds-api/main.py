import os
import joblib
import re
import numpy as np
import uvicorn
import nltk
import sys
from datetime import datetime
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from nltk.corpus import stopwords

# ==========================================
# 1. SERVER SETUP & INITIALIZATION
# ==========================================
app = FastAPI(title="SentimentAPI - Team 55", version="0.1 alpha")

# --- PATH RESOLUTION ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_FILENAME = "sentiment_model_alpha.joblib"
MODEL_PATH = os.path.join(BASE_DIR, "..", "models", MODEL_FILENAME)
MODEL_PATH = os.path.normpath(MODEL_PATH)

print(f"📍 API Directory: {BASE_DIR}")

# --- RESOURCE LOADING ---

# A. Stopwords (Visual Filter - Extended)
try:
    nltk.download('stopwords', quiet=True)
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
    print("✅ NLTK Stopwords + Custom Blacklist loaded.")

except Exception as e:
    print(f"⚠️ NLTK Error: {e}. Visual filter disabled.")
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
            coef = clf.coef_[class_idx][col_idx]
            if coef > 0:
                candidates[word] = coef

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
    text: str

class SentimentResponse(BaseModel):
    prediction: str
    probability: float
    keywords: list[str]
    timestamp: str

@app.get("/")
def home():
    status = "Online" if model_pipeline else "Offline"
    return {"service": "SentimentAPI", "status": status, "port": 5000}

@app.post("/predict", response_model=SentimentResponse)
def predict_endpoint(request: ReviewRequest):
    if not model_pipeline:
        raise HTTPException(status_code=500, detail="Model unavailable.")
    if not request.text or len(request.text.strip()) < 2:
        raise HTTPException(status_code=400, detail="Text too short.")

    cleaned_text = clean_text_for_prediction(request.text)
    if not cleaned_text: cleaned_text = request.text.lower()

    pred_label = model_pipeline.predict([cleaned_text])[0]

    probs = model_pipeline.predict_proba([cleaned_text])[0]
    prob_score = float(np.max(probs))

    clean_kws = extract_keywords_clean(cleaned_text, model_pipeline)
    current_time = datetime.now().isoformat()

    return {
        "prediction": pred_label,
        "probability": round(prob_score, 2),
        "keywords": clean_kws,
        "timestamp": current_time
    }

if __name__ == "__main__":
    print("🚀 Starting Server on Port 5000...")
    uvicorn.run(app, host="0.0.0.0", port=5000)
