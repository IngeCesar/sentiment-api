# =========================================================
# STAGE 1: BUILDER & DEVELOPMENT
# =========================================================
# This stage installs dependencies and serves as the target for local development.
FROM python:3.14.2-slim AS builder

ARG DEBIAN_FRONTEND=noninteractive
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# 1. System Dependencies (Build tools + Runtime libs for Dev)
# - build-essential: For compiling libs from source if needed.
# - libgomp1: Required for Scikit-Learn parallel computing.
# - curl: Required for the Healthcheck.
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgomp1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 2. Virtual Environment
# We install dependencies in a virtual environment to easily copy them later.
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 3. Python Dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. Security & Permissions (For Dev Runtime)
RUN useradd -m -u 1001 ml-service
# We grant permissions to the user for the app and venv directories.
RUN chown -R ml-service:ml-service /app /opt/venv

# 5. Healthcheck (For Dev Runtime)
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD curl --fail http://localhost:5000/ || exit 1

# 6. Switch User & Port
USER ml-service
EXPOSE 5000

# 7. Development Command
# This command runs when you use 'target: builder' in docker-compose.
# It enables Hot Reloading.
CMD ["uvicorn", "ds-api.main:app", "--host", "0.0.0.0", "--port", "5000", "--reload"]

# =========================================================
# STAGE 2: PRODUCTION (Optimized & Secure)
# =========================================================
# This stage creates the final lightweight image for deployment.
FROM python:3.14.2-slim AS production

ARG DEBIAN_FRONTEND=noninteractive
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# 1. Runtime Dependencies
# We install only what is needed to run the app (no compilers).
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# 2. Security: Create non-root user
RUN useradd -m -u 1001 ml-service

# 3. Copy Virtual Environment from Builder
# This brings all installed libraries without needing build tools.
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 4. Copy Application Code
COPY . .

# 5. Permissions
RUN chown -R ml-service:ml-service /app

# 6. Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD curl --fail http://localhost:5000/ || exit 1

# 7. Switch User & Port
USER ml-service
EXPOSE 5000

# 8. Production Command
# Optimized for performance (no reload).
CMD ["uvicorn", "ds-api.main:app", "--host", "0.0.0.0", "--port", "5000"]
