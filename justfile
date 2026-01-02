# ==============================================================================
# CESIUMFLOW PROJECT MANAGEMENT - SQUAD 55
# ==============================================================================
project_name := "cesiumflow"
compose_base := "docker-compose.yml"

# UI Colors
clr_ready := '\033[0;32m'
clr_info  := '\033[0;34m'
clr_warn  := '\033[0;33m'
clr_reset := '\033[0m'

# --- CORE ---

# Show this help menu with all available commands
[no-cd]
help:
    @printf "{{clr_info}}Available commands for {{project_name}}:{{clr_reset}}\n"
    @just --list --list-heading ""

# --- DEVELOPMENT ENVIRONMENT (Ports: 80, 8080, 5000) ---

# Start development environment with Hot Reload enabled
[no-cd]
dev:
    @printf "{{clr_info}}🚀 Starting DEVELOPMENT environment...{{clr_reset}}\n"
    @printf "{{clr_info}}Web: http://localhost:80 | API: http://localhost:8080 | Engine: http://localhost:5000{{clr_reset}}\n"
    @docker compose -p {{project_name}}-dev up -d --build

# Stop and remove all development containers
[no-cd]
down:
    @printf "{{clr_warn}}🛑 Stopping DEVELOPMENT containers...{{clr_reset}}\n"
    @docker compose -p {{project_name}}-dev down

# Show status of running development containers
[no-cd]
ps:
    @printf "{{clr_info}}Container Status - DEVELOPMENT:{{clr_reset}}\n"
    @docker compose -p {{project_name}}-dev ps

# Deep clean: Remove dev containers, volumes, and local images
[no-cd]
clean:
    @printf "{{clr_warn}}🧹 Performing deep clean (Dev)...{{clr_reset}}\n"
    @docker compose -p {{project_name}}-dev down --rmi local -v --remove-orphans
    @printf "{{clr_ready}}Cleanup complete.{{clr_reset}}\n"

# --- PRODUCTION ENVIRONMENT (Ports: 80, 8080, 5000) ---

# Start production-ready environment (optimized images)
[no-cd]
prod:
    @printf "{{clr_ready}}🌐 Starting PRODUCTION environment...{{clr_reset}}\n"
    @printf "{{clr_ready}}Web: http://localhost | API: http://localhost:8080 | Engine: http://localhost:5000{{clr_ready}}\n"
    @docker compose -f {{compose_base}} -p {{project_name}}-prod up -d --build

# Stop and remove all production containers
[no-cd]
down-prod:
    @printf "{{clr_warn}}🛑 Stopping PRODUCTION containers...{{clr_reset}}\n"
    @docker compose -f {{compose_base}} -p {{project_name}}-prod down

# Show status of running production containers
[no-cd]
ps-prod:
    @printf "{{clr_ready}}Container Status - PRODUCTION:{{clr_reset}}\n"
    @docker compose -f {{compose_base}} -p {{project_name}}-prod ps

# --- UTILITIES ---

# Follow real-time logs from development containers
[no-cd]
logs:
    @docker compose -p {{project_name}}-dev logs -f

# Emergency: Hard reset all Docker resources related to this project
[no-cd]
reset-docker:
    @printf "{{clr_warn}}🔥 DANGER: Hard resetting all {{project_name}} resources...{{clr_reset}}\n"
    @docker ps -a --format '{{{{.Names}}}}' | grep "{{project_name}}" | xargs -r docker stop > /dev/null 2>&1 || true
    @docker ps -a --format '{{{{.Names}}}}' | grep "{{project_name}}" | xargs -r docker rm > /dev/null 2>&1 || true
    @docker network prune -f > /dev/null 2>&1
    @printf "{{clr_ready}}✨ Docker system is fresh.{{clr_reset}}\n"


# --- TESTING ---

[no-cd]
test-int:
    @echo "🧪 Running Integration Tests..."
    set -a && . ./.env && set +a && \
    ./core-service/mvnw -f core-service/pom.xml \
           -Dtest=SentimentIntegrationTest \
           test
