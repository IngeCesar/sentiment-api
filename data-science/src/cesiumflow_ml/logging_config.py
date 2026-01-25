"""Configuracion de logging (sin PII, orientado a produccion CPU-friendly)."""
from __future__ import annotations

import logging
import os
from logging import Logger
from typing import Optional

from . import config


def setup_logging(level: Optional[str] = None, fmt: Optional[str] = None) -> None:
    """Configura logging global.

    - level: permite override (por defecto usa config.LOG_LEVEL)
    - fmt: formato de log (por defecto usa config.LOG_FORMAT)
    """
    logging.basicConfig(
        level=(level or config.LOG_LEVEL),
        format=(fmt or config.LOG_FORMAT),
    )


def get_logger(name: str) -> Logger:
    if not logging.getLogger().handlers:
        setup_logging()
    return logging.getLogger(name)


__all__ = ["setup_logging", "get_logger"]
