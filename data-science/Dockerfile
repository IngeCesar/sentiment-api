# =========================================================
# STAGE 1: BUILDER & DEVELOPMENT
# =========================================================
FROM python:3.14.2-slim AS builder

ARG DEBIAN_FRONTEND=noninteractive
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# 1. System Dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgomp1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 2. Virtual Environment
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 3. Python Dependencies & NLTK Data
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Create directory and download NLTK data to a shared location
RUN mkdir -p /usr/local/share/nltk_data && \
    python -m nltk.downloader -d /usr/local/share/nltk_data stopwords

# 4. Security & Permissions
RUN useradd -m -u 1001 ml-service
# Grant permissions to user for app, venv, AND the new nltk_data folder
RUN chown -R ml-service:ml-service /app /opt/venv /usr/local/share/nltk_data

# Healthcheck pointing to our custom health endpoint
HEALTHCHECK --interval=20s --timeout=5s --start-period=10s --retries=3 \
  CMD curl --fail http://localhost:5000/health || exit 1

USER ml-service
EXPOSE 5000

CMD ["uvicorn", "ds-api.main:app", "--host", "0.0.0.0", "--port", "5000", "--reload"]

# =========================================================
# STAGE 2: PRODUCTION
# =========================================================
FROM python:3.14.2-slim AS production

ARG DEBIAN_FRONTEND=noninteractive
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1001 ml-service

# Copy Virtual Environment
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# --- CRITICAL: Copy NLTK Data from Builder ---
COPY --from=builder /usr/local/share/nltk_data /usr/local/share/nltk_data

COPY . .

# Final Permissions
RUN chown -R ml-service:ml-service /app /usr/local/share/nltk_data

# Healthcheck pointing to our custom health endpoint
HEALTHCHECK --interval=20s --timeout=5s --start-period=10s --retries=3 \
  CMD curl --fail http://localhost:5000/health || exit 1

USER ml-service
EXPOSE 5000

CMD ["uvicorn", "ds-api.main:app", "--host", "0.0.0.0", "--port", "5000"]
