# --- ETAPA: BUILDER & DEV ---
# Gestión de dependencias y entorno de compilación.
FROM python:3.14.2-slim AS builder

ARG DEBIAN_FRONTEND=noninteractive
# Optimizaciones: Sin bytecode (.pyc), logs directos y sin caché de pip
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=off \
    PIP_DISABLE_PIP_VERSION_CHECK=on

# Instala GCC (necesario para compilar librerías de ML)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Seguridad: Usuario sin privilegios (Non-root)
RUN useradd -m -u 1001 ml-service

WORKDIR /app

# Capa de Dependencias (Separada para optimizar caché de Docker)
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY requirements.txt .
RUN pip install --upgrade pip && pip install -r requirements.txt

# Capa de Código Fuente
COPY models/ ./models/
COPY src/ ./src/
COPY ds-api/ ./ds-api/

RUN chown -R ml-service:ml-service /app /opt/venv

# Healthcheck (FastAPI)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl --fail http://localhost:5000/health || exit 1

USER ml-service
EXPOSE 5000

# Entrypoint dinámico para acelerar el ciclo de desarrollo (hot-reload).
CMD ["uvicorn", "ds-api.main:app", "--host", "0.0.0.0", "--port", "5000", "--reload"]

# --- ETAPA: PRODUCCIÓN (SLIM) ---
# Runtime optimizado: Sin compiladores, imagen ligera.
FROM python:3.14.2-slim AS production

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PATH="/opt/venv/bin:$PATH"

WORKDIR /app

RUN useradd -m -u 1001 ml-service

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Transferencia de artefacto: Exclusión de herramientas de compilación y cache.
# Nota: Usamos --chown aquí para optimizar el tamaño de la capa final.
COPY --from=builder --chown=ml-service:ml-service /opt/venv /opt/venv
COPY --from=builder --chown=ml-service:ml-service /app/models ./models
COPY --from=builder --chown=ml-service:ml-service /app/src ./src
COPY --from=builder --chown=ml-service:ml-service /app/ds-api ./ds-api

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl --fail http://localhost:5000/health || exit 1

USER ml-service
EXPOSE 5000

# Entrypoint Prod (Optimizado)
ENTRYPOINT ["uvicorn", "ds-api.main:app", "--host", "0.0.0.0", "--port", "5000"]
