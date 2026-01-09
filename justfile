# ==============================================================================
# CESIUMFLOW ORCHESTRATION - SQUAD 55
# Estandarización de flujos operativos para garantizar paridad de entornos.
# ==============================================================================
project_name := "cesiumflow"
compose_base := "docker-compose.yml"
compose_dev  := "-f docker-compose.yml -f docker-compose.override.yml"

# UI: Trazabilidad visual en terminal
clr_ready := '\033[0;32m'
clr_info  := '\033[0;34m'
clr_warn  := '\033[0;33m'
clr_reset := '\033[0m'

# --- GESTIÓN DE CICLO DE VIDA ---

[no-cd]
help:
    @printf "{{clr_info}}Manifiesto de automatización para {{project_name}}:{{clr_reset}}\n"
    @just --list --list-heading ""

[no-cd]
dev:
    @printf "{{clr_info}}🚀 Inicializando entorno de DESARROLLO (Modo Override)...{{clr_reset}}\n"
    # Orquestación con sincronización de volúmenes para optimizar el ciclo de feedback.
    @docker compose {{compose_dev}} -p {{project_name}}-dev up -d --build

[no-cd]
down:
    @printf "{{clr_warn}}🛑 Cierre de servicios de desarrollo...{{clr_reset}}\n"
    @docker compose {{compose_dev}} -p {{project_name}}-dev down

[no-cd]
clean:
    @printf "{{clr_warn}}🧹 Purga de infraestructura (Imágenes locales + Volúmenes)...{{clr_reset}}\n"
    # Garantiza un estado de persistencia 'limpio' al eliminar volúmenes asociados.
    @docker compose {{compose_dev}} -p {{project_name}}-dev down --rmi local -v --remove-orphans
    @printf "{{clr_ready}}✨ Infraestructura y base de datos saneadas.{{clr_reset}}\n"

# --- TELEMETRÍA Y RECUPERACIÓN ---

[no-cd]
logs:
    @docker compose -p {{project_name}}-dev logs -f

[no-cd]
reset-docker:
    @printf "{{clr_warn}}🔥 HARD RESET: Depuración total de recursos {{project_name}}...{{clr_reset}}\n"
    # Estrategia de remediación ante colisiones de red o estados inconsistentes de Docker.
    @docker ps -a --format '{{{{.Names}}}}' | grep "cesium-" | xargs -r docker stop > /dev/null 2>&1 || true
    @docker ps -a --format '{{{{.Names}}}}' | grep "cesium-" | xargs -r docker rm > /dev/null 2>&1 || true
    @docker volume ls -q | grep "{{project_name}}" | xargs -r docker volume rm > /dev/null 2>&1 || true
    @docker network prune -f > /dev/null 2>&1
    @printf "{{clr_ready}}✨ Entorno de ejecución purificado.{{clr_reset}}\n"

# --- CALIDAD Y VALIDACIÓN ---

[no-cd]
test-int:
    @echo "🧪 Ejecutando suite de Integración..."
    # Verificación de pre-requisitos: Asegura la integridad del contrato de entorno (.env).
    @if [ ! -f .env ]; then echo "❌ Error crítico: Configuración (.env) inexistente"; exit 1; fi
    set -a && . ./.env && set +a && \
    ./core-service/mvnw -f core-service/pom.xml \
            -Dtest=SentimentIntegrationTest \
            test