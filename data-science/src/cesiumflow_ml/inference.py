import joblib
import numpy as np
import os
from typing import Dict, List, Tuple, Any
from .preprocessing import clean_text

# Balanced list: Remove pure visual noise (articles/prepositions)
# but KEEP contrastive words like 'pero', 'aunque', 'no' as they
# provide essential context for neutral or ambiguous sentiments.
INTERNAL_STOPWORDS = {
    'el', 'la', 'los', 'las', 'un', 'una', 'unos', 'unas',
    'de', 'del', 'al', 'lo', 'que', 'con', 'por', 'para', 'y', 'o'
}

def load_artifacts(model_path: str, vectorizer_path: str) -> Tuple[Any, Any]:
    """
    Loads serialized .joblib artifacts from the filesystem.
    """
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Model artifact not found at: {model_path}")

    if not os.path.exists(vectorizer_path):
        raise FileNotFoundError(f"Vectorizer artifact not found at: {vectorizer_path}")

    model = joblib.load(model_path)
    vectorizer = joblib.load(vectorizer_path)
    return model, vectorizer

def extract_keywords(
    clean_input: str,
    model: Any,
    vectorizer: Any,
    top_n: int = 5
) -> List[str]:
    """
    Extracts impactful keywords based on the model's coefficients.
    Calculates impact as: Coefficient Weight * TF-IDF Score.
    Filters out pure visual noise while keeping semantic shifters like 'pero'.
    """
    # Transform text to vector space
    tfidf_matrix = vectorizer.transform([clean_input])

    # Get the predicted class to look at the correct coefficients
    prediction_label = model.predict(tfidf_matrix)[0]

    # Locate the index of the predicted class
    class_idx = np.where(model.classes_ == prediction_label)[0][0]

    # Get model coefficients for that specific class
    coefficients = model.coef_[class_idx]
    feature_names = vectorizer.get_feature_names_out()

    # Identify non-zero terms in the input text
    dense_vector = tfidf_matrix.toarray()[0]
    present_indices = np.where(dense_vector > 0)[0]

    # Calculate contribution score
    contributions = []
    for idx in present_indices:
        word = feature_names[idx]

        # QUALITY FILTER:
        # Skip if word is in the visual noise list
        if word in INTERNAL_STOPWORDS:
            continue

        # Calculate impact score
        score = abs(coefficients[idx] * dense_vector[idx])
        contributions.append((word, score))

    # Sort by score descending
    contributions.sort(key=lambda x: x[1], reverse=True)

    return [word for word, score in contributions[:top_n]]

def predict_sentiment(
    text: str,
    model: Any,
    vectorizer: Any,
    top_n_keywords: int = 5
) -> Dict[str, Any]:
    """
    Orchestrates the full prediction pipeline: Cleaning -> Inference -> Keywords.
    """
    try:
        # 1. Preprocessing
        cleaned_text = clean_text(text)
        tfidf_vector = vectorizer.transform([cleaned_text])

        # 2. Prediction
        prediction_label = model.predict(tfidf_vector)[0]

        # 3. Confidence Calculation
        probabilities = model.predict_proba(tfidf_vector)[0]
        class_idx = np.where(model.classes_ == prediction_label)[0][0]
        confidence_score = float(probabilities[class_idx])

        # 4. Feature Extraction (Keywords)
        keywords = extract_keywords(cleaned_text, model, vectorizer, top_n_keywords)

        return {
            "prediction": prediction_label,
            "probability": confidence_score,
            "keywords": keywords
        }

    except Exception as e:
        print(f"Inference Engine Error: {e}")
        # Fail-safe response matching the API contract
        return {
            "prediction": "Error",
            "probability": 0.0,
            "keywords": []
        }
