"""Extraccion de keywords basada en coeficientes del modelo (LogReg)."""
from __future__ import annotations

from typing import List
import numpy as np


def extraer_keywords(texto_prep: str, prediccion: str, model, vectorizer, top_n: int = 5) -> List[str]:
    """
    Retorna las palabras del texto preprocesado con mayor contribucion para la clase predicha.
    - texto_prep: texto ya preprocesado (tokens separados por espacios)
    - prediccion: etiqueta predicha (ej: "negativo", "neutro", "positivo")
    - model: instancia de LogisticRegression entrenada
    - vectorizer: TfidfVectorizer entrenado
    - top_n: numero de palabras a retornar
    """
    if not texto_prep:
        return []

    vocab = vectorizer.get_feature_names_out()
    vocab_index = {t: i for i, t in enumerate(vocab)}

    try:
        class_idx = list(model.classes_).index(prediccion)
    except ValueError:
        return []

    coefs = model.coef_[class_idx]
    palabras = set(texto_prep.split())

    contribuciones = []
    for palabra in palabras:
        idx = vocab_index.get(palabra)
        if idx is not None:
            contribuciones.append((palabra, coefs[idx]))

    contribuciones.sort(key=lambda x: x[1], reverse=True)
    return [p for p, _ in contribuciones[:top_n]]


__all__ = ["extraer_keywords"]
