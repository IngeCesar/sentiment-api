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
CREATE INDEX IF NOT EXISTS idx_sentiment_created_at ON sentiment_records(created_at);

-- -------------------------------------------
-- VISTAS PARA ANALÍTICA (DASHBOARD) - VERSIÓN CORREGIDA Y ROBUSTA
-- -------------------------------------------

-- 1. Pie Chart View: Distribución global con protección contra nulos
-- Se asegura de que la columna se llame 'sentiment' para coincidir con Java
CREATE OR REPLACE VIEW view_sentiment_distribution AS
SELECT
    prediction AS sentiment,
    COUNT(*) AS count
FROM sentiment_records
WHERE prediction IS NOT NULL
  AND prediction != 'ERROR'
  AND prediction != ''
GROUP BY prediction;

-- 2. Bar Chart View: Top palabras clave con limpieza de formato
-- Soluciona el bug de arrays mixtos y añade compatibilidad estricta con Docker
CREATE OR REPLACE VIEW view_top_keywords AS
SELECT
    word AS keyword,
    COUNT(*) AS count
FROM (
    -- CAMBIO CRÍTICO AQUÍ: Se añadió "::text" después de keywords
    -- Esto convierte el Array a Texto explícitamente antes de procesarlo
    SELECT TRIM(BOTH ' "''{}[]' FROM unnest(string_to_array(translate(keywords::text, '[]', '{}'), ','))) AS word
    FROM sentiment_records
    ) AS subquery
WHERE word IS NOT NULL AND length(word) > 3 -- Filtro para omitir conectores cortos
GROUP BY word
ORDER BY count DESC
    LIMIT 10; -- Aumentado a 10 para dar más riqueza al gráfico
