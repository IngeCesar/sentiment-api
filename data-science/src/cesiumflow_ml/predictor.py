"""Clase principal de inferencia para el clasificador de sentimientos."""
from __future__ import annotations

import logging
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Dict, Any

import numpy as np

from . import config
from .model_loader import loader
from . import preprocessing_v2 as preprocessing
from .keyword_extractor import extraer_keywords

logger = logging.getLogger(__name__)

MAPEO_CLASES = {
    "negativo": "Negativo",
    "neutro": "Neutro",
    "positivo": "Positivo",
}


def _timestamp_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _validar_texto(texto: Any) -> str:
    if texto is None:
        raise ValueError("'texto' no puede ser None")
    if not isinstance(texto, str):
        raise TypeError("'texto' debe ser str")
    if not texto.strip():
        raise ValueError("'texto' no puede estar vacio")
    return texto


@dataclass
class PredictionResult:
    prediction: str
    probability: float
    keywords: list[str]
    timestamp: str

    def to_dict(self) -> Dict[str, Any]:
        return {
            "prediction": self.prediction,
            "probability": self.probability,
            "keywords": self.keywords,
            "timestamp": self.timestamp,
        }


class SentimentPredictor:
    def __init__(self) -> None:
        self.model, self.vectorizer = loader.get()

    def predict(self, texto: str) -> PredictionResult:
        texto_raw = _validar_texto(texto)

        texto_prep = preprocessing.preprocesar_texto(texto_raw)
        if not texto_prep:
            raise ValueError("El preprocesamiento produjo texto vacio")

        X = self.vectorizer.transform([texto_prep])
        proba = self.model.predict_proba(X)[0]
        pred_raw = self.model.predict(X)[0]

        pred_api = MAPEO_CLASES.get(pred_raw, pred_raw.capitalize())
        prob_val = round(float(np.max(proba)), config.PROB_DECIMALS)

        kws = extraer_keywords(
            texto_prep=texto_prep,
            prediccion=pred_raw,
            model=self.model,
            vectorizer=self.vectorizer,
            top_n=config.KEYWORDS_TOP_N,
        )

        ts = _timestamp_iso()
        logger.info("prediction=%s prob=%.3f", pred_api, prob_val)

        return PredictionResult(
            prediction=pred_api,
            probability=prob_val,
            keywords=kws,
            timestamp=ts,
        )


__all__ = ["SentimentPredictor", "PredictionResult", "MAPEO_CLASES"]
