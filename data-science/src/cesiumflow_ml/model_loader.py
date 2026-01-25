"""Carga única de artefactos de modelo y vectorizador para inferencia (CPU-friendly)."""
from __future__ import annotations

import logging
from pathlib import Path
from typing import Optional

import joblib

from . import config
from . import preprocessing_v2 as preprocessing

logger = logging.getLogger(__name__)


class ModelLoader:
    """Singleton ligero para cargar modelo y vectorizador una sola vez."""

    _instance: Optional["ModelLoader"] = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self) -> None:
        if getattr(self, "_initialized", False):
            return
        self.model_path: Path = Path(config.MODEL_PATH)
        self.vectorizer_path: Path = Path(config.VECTORIZER_PATH)
        self.stopwords_path: Path = Path(config.STOPWORDS_PATH)
        self.model = None
        self.vectorizer = None
        self.stopwords = preprocessing.DEFAULT_STOPWORDS
        self._initialized = True

    def _validate_paths(self) -> None:
        missing = [p for p in [self.model_path, self.vectorizer_path] if not p.exists()]
        if missing:
            raise FileNotFoundError(f"Faltan artefactos: {missing}")

    def load(self):
        """Carga modelo y vectorizador si aún no están cargados."""
        if self.model is not None and self.vectorizer is not None:
            return self.model, self.vectorizer

        self._validate_paths()

        logger.info("Cargando modelo desde %s", self.model_path)
        self.model = joblib.load(self.model_path)

        logger.info("Cargando vectorizador desde %s", self.vectorizer_path)
        self.vectorizer = joblib.load(self.vectorizer_path)

        if self.stopwords_path.exists():
            self.stopwords = preprocessing._cargar_stopwords(self.stopwords_path)
        return self.model, self.vectorizer

    def get(self):
        if self.model is None or self.vectorizer is None:
            return self.load()
        return self.model, self.vectorizer


loader = ModelLoader()

__all__ = ["ModelLoader", "loader"]
