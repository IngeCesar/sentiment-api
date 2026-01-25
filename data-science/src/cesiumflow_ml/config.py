"""Configuracion centralizada para inferencia en produccion (CPU-friendly)."""
from __future__ import annotations

import os
from pathlib import Path
from typing import Tuple, Dict, Any

# ---------------------------------------------------------------------------
# Paths base
# ---------------------------------------------------------------------------
# BASE_DIR apunta a src/cesiumflow_ml, necesitamos subir 2 niveles a data-science/
BASE_DIR = Path(__file__).resolve().parent.parent.parent
ARTIFACTS_DIR = BASE_DIR / "models"

MODEL_VERSION = "1.0.0"

# Permite override via variables de entorno (rutas absolutas o relativas)
def _env_path(var_name: str, default: Path) -> Path:
    candidate = os.getenv(var_name)
    if candidate:
        return Path(candidate).expanduser().resolve()
    return default

MODEL_PATH = _env_path("MODEL_PATH", ARTIFACTS_DIR / "sentiment_model.joblib")
VECTORIZER_PATH = _env_path("VECTORIZER_PATH", ARTIFACTS_DIR / "tfidf_vectorizer.joblib")
STOPWORDS_PATH = _env_path("STOPWORDS_PATH", ARTIFACTS_DIR / "stopwords_eliminar.txt")

# ---------------------------------------------------------------------------
# TF-IDF config (segun PARTE 11.3.6)
# ---------------------------------------------------------------------------
TFIDF_CONFIG: Dict[str, Any] = {
    "max_features": 10_000,
    "ngram_range": (1, 2),
    "min_df": 3,
    "max_df": 0.9,
    "lowercase": False,
    "token_pattern": r"(?u)\\b\\w+\\b|<[A-Z_0-9]+>",
    "stop_words": None,  # se carga externamente desde STOPWORDS_PATH
}

# ---------------------------------------------------------------------------
# Modelo: Logistic Regression (PARTE 11.3.6)
# ---------------------------------------------------------------------------
MODEL_CONFIG: Dict[str, Any] = {
    "C": 0.5,
    "class_weight": "balanced",
    "multi_class": "multinomial",
    "solver": "lbfgs",
    "max_iter": 1000,
    "random_state": 42,
}

# ---------------------------------------------------------------------------
# Inferencia
# ---------------------------------------------------------------------------
KEYWORDS_TOP_N: int = int(os.getenv("KEYWORDS_TOP_N", "5"))
PROB_DECIMALS: int = int(os.getenv("PROB_DECIMALS", "2"))

# ---------------------------------------------------------------------------
# Logging (basico; loggers configurados en logging_config.py)
# ---------------------------------------------------------------------------
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
LOG_FORMAT = os.getenv(
    "LOG_FORMAT",
    "%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)

__all__ = [
    "BASE_DIR",
    "ARTIFACTS_DIR",
    "MODEL_VERSION",
    "MODEL_PATH",
    "VECTORIZER_PATH",
    "STOPWORDS_PATH",
    "TFIDF_CONFIG",
    "MODEL_CONFIG",
    "KEYWORDS_TOP_N",
    "PROB_DECIMALS",
    "LOG_LEVEL",
    "LOG_FORMAT",
]
