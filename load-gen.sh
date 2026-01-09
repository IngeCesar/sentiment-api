#!/usr/bin/env bash

# ==========================================
# CESIUMFLOW STRESS TESTER (PARALLEL)
# Orquestador de carga sintética concurrente
# ==========================================

# Punto de entrada del microservicio Core
API_URL="http://localhost:8080/api/v1/sentiment"

# Definición de paleta ANSI para feedback visual en terminal
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# --- DATASETS DE PRUEBA (Muestras aleatorias para el motor de IA) ---
POSITIVOS=(
    "El servicio fue excelente y muy rápido"
    "Me encanta este producto, lo recomiendo totalmente"
    "La atención al cliente fue maravillosa y amable"
    "Entrega puntual y en perfectas condiciones"
    "Superó mis expectativas, volveré a comprar"
    "Una experiencia de usuario fantástica"
    "Calidad precio inmejorable"
    "Estoy muy feliz con el resultado"
)

NEGATIVOS=(
    "El pedido llegó tarde y en mal estado"
    "Pésima atención, el personal fue grosero"
    "No funciona como dicen, es una estafa"
    "Muy decepcionado con la calidad del material"
    "Es demasiado lento y se traba constantemente"
    "No responden a mis correos, terrible soporte"
    "Dinero tirado a la basura"
    "La peor experiencia de compra de mi vida"
)

NEUTROS=(
    "El producto es normal, cumple su función"
    "Llegó el día acordado pero la caja estaba sucia"
    "No estoy seguro si me gusta el color"
    "Es aceptable por el precio que tiene"
    "Necesito probarlo más tiempo para opinar"
    "La instalación fue regular, ni fácil ni difícil"
)

# Captura de argumento o valor por defecto (Parámetro posicional $1)
CANTIDAD=${1:-50}

echo -e "${YELLOW}🔥 INICIANDO STRESS TEST: Lanzando $CANTIDAD peticiones concurrentes...${NC}"
echo "Target: $API_URL"
echo "---------------------------------------------------"

for ((i = 1; i <= CANTIDAD; i++)); do
    # 1. Selección aleatoria de categoría para simular tráfico real
    TIPO=$((RANDOM % 3))

    if [ $TIPO -eq 0 ]; then
        SIZE=${#POSITIVOS[@]}
        IDX=$((RANDOM % SIZE))
        TEXTO="${POSITIVOS[$IDX]}"
        LABEL="[POS]"
        COLOR=$GREEN
    elif [ $TIPO -eq 1 ]; then
        SIZE=${#NEGATIVOS[@]}
        IDX=$((RANDOM % SIZE))
        TEXTO="${NEGATIVOS[$IDX]}"
        LABEL="[NEG]"
        COLOR=$RED
    else
        SIZE=${#NEUTROS[@]}
        IDX=$((RANDOM % SIZE))
        TEXTO="${NEUTROS[$IDX]}"
        LABEL="[NEU]"
        COLOR=$BLUE
    fi

    # 2. DISPARO ASÍNCRONO (FIRE & FORGET)
    # El '&' delega el curl a un proceso de fondo para no bloquear el loop
    curl -s -o /dev/null -X POST \
        -H "Content-Type: application/json" \
        -d "{\"text\": \"$TEXTO\"}" \
        "$API_URL" &

    # 3. Log de telemetría (Reporta el intento de envío, no el resultado)
    echo -e "${COLOR}🚀 ($i/$CANTIDAD) Disparado: $LABEL \"$TEXTO\"${NC}"

done

echo "---------------------------------------------------"
echo -e "${YELLOW}⏳ Esperando a que el servidor procese las respuestas en segundo plano...${NC}"

# Bloqueo de flujo hasta que todos los subprocesos curl finalicen
wait

echo -e "${GREEN}✅ Todas las peticiones han finalizado.${NC}"
