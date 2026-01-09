-- Initial Schema for CesiumFlow Sentiment Analysis
-- Uso de UUID para garantizar escalabilidad en sistemas distribuidos
CREATE TABLE IF NOT EXISTS sentiment_records (
    -- Identificador único universal para evitar colisiones de datos
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_text TEXT NOT NULL,
    prediction VARCHAR(50) NOT NULL,
    probability DOUBLE PRECISION NOT NULL,
    -- Arreglo nativo para manejo eficiente de palabras clave
    keywords TEXT[],
    
    -- TIMESTAMPTZ y DEFAULT NOW() para máxima robustez
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Índice opcional para optimizar búsquedas y filtrado por fecha
CREATE INDEX idx_sentiment_created_at ON sentiment_records(created_at);

-- -------------------------------------------
-- VISTAS PARA ANALÍTICA (DASHBOARD)
-- -------------------------------------------

-- 1. Pie Chart View: Agregación para distribución global de sentimientos
CREATE OR REPLACE VIEW view_sentiment_distribution AS
SELECT
    prediction as sentiment,
    COUNT(*) as count
FROM sentiment_records
GROUP BY prediction;

-- 2. Bar Chart View: Extracción de las 3 palabras clave más frecuentes
CREATE OR REPLACE VIEW view_top_keywords AS
SELECT
    TRIM(word) as keyword,
    COUNT(*) as count
FROM sentiment_records,
    unnest(keywords) as word -- Descompone el array en filas para conteo
WHERE length(word) > 3 -- Filtro básico para omitir conectores y ruido
GROUP BY keyword
ORDER BY count DESC
LIMIT 3;