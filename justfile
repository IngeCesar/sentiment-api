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
    @printf "\n{{clr_info}}============================================================{{clr_reset}}\n"
    @printf "{{clr_info}}   🚀 MANIFIESTO DE AUTOMATIZACIÓN - {{project_name}} (SQUAD 55){{clr_reset}}\n"
    @printf "{{clr_info}}============================================================{{clr_reset}}\n"

    @printf "\n{{clr_ready}}💻  ENTORNO DE DESARROLLO (Localhost){{clr_reset}}\n"
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just dev" "Levanta el entorno con Hot-Reload y Volúmenes."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just down" "Detiene los servicios de desarrollo."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just clean" "Borra contenedores, volúmenes e imágenes locales (Reset total)."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just logs" "Muestra logs en tiempo real (Tail)."

    @printf "\n{{clr_warn}}☁️   ENTORNO DE PRODUCCIÓN (ej:OCI Server / Rama Main){{clr_reset}}\n"
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just deploy" "Git Pull + Build Optimizado + Deploy (Sin downtime)."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just down-prod" "Apaga Producción de forma segura (Conserva Datos)."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just logs-prod" "Ver logs del entorno productivo."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just restart-prod" "Reinicia los servicios (Soft Reset)."

    @printf "\n{{clr_warn}}☢️   ZONA DE PELIGRO / DEMO TOOLS{{clr_reset}}\n"
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just flush-db-prod" "Borra SOLO la Base de Datos y la reinicia limpia (Para Demos)."
    @printf "  {{clr_info}}%-20s{{clr_reset}} %s\n" "just nuke-prod" "💀 DESTRUYE TODO en Producción (Volúmenes incluidos)."

    @printf "\n{{clr_info}}============================================================{{clr_reset}}\n"

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

# [no-cd]
# test-int:
# @echo "🧪 Ejecutando suite de Integración..."
# # Verificación de pre-requisitos: Asegura la integridad del contrato de entorno (.env).
# @if [ ! -f .env ]; then echo "❌ Error crítico: Configuración (.env) inexistente"; exit 1; fi
# set -a && . ./.env && set +a && \
# ./core-service/mvnw -f core-service/pom.xml \
# -Dtest=SentimentIntegrationTest \
# test

# --- OPERACIONES DE PRODUCCIÓN---

[no-cd]
deploy:
    @printf "{{clr_ready}}🚀 PROD: Desplegando CesiumFlow (Rama Main)...{{clr_reset}}\n"
    git pull origin develop ## main en prod real
    # '-f docker-compose.yml' usa la config base y activa target: production
    @docker compose -f docker-compose.yml -p {{project_name}}-prod up -d --build --remove-orphans
    @docker image prune -f
    @printf "{{clr_ready}}✅ Sistema operando en Producción.{{clr_reset}}\n"

[no-cd]
down-prod:
    @printf "{{clr_warn}}🛑 Deteniendo servicios de PROD (Conservando Datos)...{{clr_reset}}\n"
    @docker compose -f docker-compose.yml -p {{project_name}}-prod down
    @printf "{{clr_ready}}✅ Sistema apagado.{{clr_reset}}\n"

[no-cd]
restart-prod:
    @printf "{{clr_warn}}🔄 Reiniciando servicios de PROD...{{clr_reset}}\n"
    @docker compose -f docker-compose.yml -p {{project_name}}-prod down
    @docker compose -f docker-compose.yml -p {{project_name}}-prod up -d
    @printf "{{clr_ready}}✅ Servicios reiniciados.{{clr_reset}}\n"

[no-cd]
flush-db-prod:
    @printf "{{clr_warn}}🚽 FLUSH: Borrando SOLO los datos de la DB en PROD...{{clr_reset}}\n"
    # 1. Para DB y Core (para evitar errores de conexión en Java)
    @docker compose -f docker-compose.yml -p {{project_name}}-prod stop sentiment-db core-service

    # 2. Elimina el contenedor de la DB para soltar el volumen
    @docker compose -f docker-compose.yml -p {{project_name}}-prod rm -f sentiment-db

    # 3. Borra el volumen físico (Aquí es donde se borran los datos)
    @docker volume rm {{project_name}}-prod_postgres-data || echo "⚠️ El volumen ya estaba borrado o no existe."

    # 4. Levanta la DB (Se recrea limpia y ejecuta init.sql)
    @docker compose -f docker-compose.yml -p {{project_name}}-prod up -d sentiment-db

    @printf "⏳ Esperando a que la DB inicie..."
    @sleep 5
    @docker compose -f docker-compose.yml -p {{project_name}}-prod up -d core-service
    @printf "\n{{clr_ready}}✨ Base de datos reseteada a estado inicial.{{clr_reset}}\n"

[no-cd]
nuke-prod:
    @printf "{{clr_warn}}☢️  ATENCIÓN: Destruyendo PROD y BORRANDO DATOS (Volúmenes)...{{clr_reset}}\n"
    @docker compose -f docker-compose.yml -p {{project_name}}-prod down -v --remove-orphans
    @docker system prune -f
    @printf "{{clr_ready}}💀 Entorno eliminado. Ejecuta 'just deploy' para reinstalar.{{clr_reset}}\n"

[no-cd]
logs-prod:
    @docker compose -p {{project_name}}-prod logs -f --tail=100
