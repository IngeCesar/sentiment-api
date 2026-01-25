# Plan de Modelado - Clasificador de Sentimientos (Fase B)

## Información del Proyecto
- **Dataset:** Amazon Reviews Multi (Español)
- **Objetivo:** Clasificación de sentimientos (3 clases: Positivo, Negativo, Neutro)
- **Fase anterior:** Fase A - Decisiones de Preprocesamiento ✅
- **Fecha de inicio Fase B:** 23 de enero de 2026

---

## 1. Contexto y Objetivo

### 1.1 Objetivo del Modelo
- Clasificar reseñas de Amazon en: **Positivo**, **Negativo**, **Neutro**
- Proporcionar probabilidad de la predicción
- Extraer palabras clave que justifican la predicción

### 1.2 Contrato de API (Referencia)

> **Nota:** La implementación del contrato se realiza en la PARTE 5 del notebook,
> después de tener el modelo final. Se documenta aquí como referencia.

**Entrada:**
```
texto: string (reseña del usuario)
```

**Salida:**
```json
{
    "prediction": "Positivo|Negativo|Neutro",
    "probability": 0.98,
    "keywords": ["palabra1", "palabra2", ...],
    "timestamp": "2026-01-23T10:30:00"
}
```

### 1.3 Restricciones Técnicas

| Restricción | Descripción |
|-------------|-------------|
| `predict_proba()` | Modelo debe soportar probabilidades |
| Mapeo inverso | Vectorizador debe permitir extraer keywords |
| Artefactos | Guardar: modelo, vectorizador, stopwords |

### 1.4 Preguntas Pendientes

- [ ] ¿Despliegue en misma máquina que Java o servidor separado?
- [ ] ¿Cantidad de keywords en respuesta? (propuesta: 3-5)

---

## 2. Arquitectura de Datos

### 2.1 Datasets Disponibles

| Dataset | Registros | Uso |
|---------|-----------|-----|
| `df_dev` | 205,000 | Entrenamiento + Validación (K-Fold CV) |
| `df_test` | 5,000 | Evaluación final (UNA VEZ) |

### 2.2 Distribución de Clases

| Clase | Porcentaje | Registros (aprox) |
|-------|------------|-------------------|
| Negativo | 40% | 82,000 |
| Neutro | 20% | 41,000 |
| Positivo | 40% | 82,000 |

> ⚠️ **Hallazgo del EDA:** Clase NEUTRO es minoritaria y difícil de clasificar
> ✅ **Verificado en EDA:** df_dev=205,000 y df_test=5,000 mantienen proporciones 40/20/40 (IR 2:1) con estratificación consistente entre ambos conjuntos.

### 2.3 Estrategia de Validación

| Aspecto | Decisión |
|---------|----------|
| **Método** | Stratified K-Fold Cross-Validation (5 folds) |
| **Razón** | Mantiene proporción de clases en cada fold |
| **df_test** | Se usa UNA SOLA VEZ al final para métricas oficiales |

### 2.4 Referencias

- Análisis de datos: `EDA_sentiment.ipynb` (Sección 2)

---

## 3. Pipeline de Preprocesamiento

### 3.1 Resumen de Decisiones (Fase A)

| # | Elemento | Decisión | Referencia |
|---|----------|----------|------------|
| 3.1 | Números | Transformar patrones significativos, eliminar resto | DECISIONES 3.1 |
| 3.2 | Puntuación repetida | Tokens granulares por intensidad | DECISIONES 3.2 |
| 3.3 | Caracteres especiales | Tokenizar € y %, eliminar resto | DECISIONES 3.3 |
| 3.4 | Errores ortográficos | No hacer nada (preservar) | DECISIONES 3.4 |
| 3.5 | Mayúsculas | Normalización selectiva (preservar MAYÚSCULAS) | DECISIONES 3.5 |
| 3.6 | Mixed case | Cubierto por 3.5 | DECISIONES 3.6 |
| 3.7 | Emojis | Eliminar | DECISIONES 3.7 |
| 3.8 | text_length | Probar en experimentación | DECISIONES 3.8 |
| 3.9 | Stopwords | Lista custom (403 eliminar, 118 preservar) | DECISIONES 3.9 |

> 📄 **Documento completo:** `DECISIONES_PREPROCESAMIENTO.md`  
> 📓 **Análisis detallado:** `EDA_sentiment.ipynb` (Sección 3)

### 3.2 Funciones de Preprocesamiento

| # | Función | Descripción | Orden |
|---|---------|-------------|-------|
| 0 | `limpiar_ruido()` | Elimina URLs, correos, menciones, hashtags, y caracteres `<>` | 0° |
| 1 | `eliminar_emojis()` | Elimina todos los emojis | 1° |
| 2 | `tokenizar_caracteres_especiales()` | número+€/$ → `<PRECIO>`, número+% → `<PORCENTAJE>` | 2° |
| 3 | `tokenizar_patrones_numericos()` | Ver detalle abajo ↓ | 3° |
| 4 | `tokenizar_puntuacion_repetida()` | !! → `<EXCL_2>`, etc. | 4° |
| 5 | `eliminar_caracteres_restantes()` | Limpia caracteres no deseados (excepto `<>`) | 5° |
| 6 | `normalizar_mayusculas()` | Preserva MAYÚSCULAS, normaliza resto | 6° |
| 7 | `preprocesar_texto()` | **Función maestra** - ejecuta todas en orden | — |

> ⚠️ **IMPORTANTE:** El orden es crítico. El paso 0 elimina `<>` ANTES de tokenizar para evitar conflictos con tokens especiales.

> 📝 **Nota sobre Stopwords:** Las stopwords NO se eliminan en preprocesamiento. Se configuran en TF-IDF como `stop_words=list(STOPWORDS_ELIMINAR)`. Las 118 palabras preservadas se incluyen automáticamente porque NO están en esa lista.

#### Detalle de `tokenizar_patrones_numericos()`

| Subpaso | Patrón | Acción | Ejemplo |
|---------|--------|--------|---------|
| 3.1 | `duracion` | TRANSFORMAR a texto | "2 días" → "dos días" |
| 3.2 | `numero_veces` | TRANSFORMAR a texto | "3 veces" → "tres veces" |
| 3.3 | `cantidad` | TRANSFORMAR a texto | "5 unidades" → "cinco unidades" |
| 3.4 | `porcentaje` | TRANSFORMAR a texto | "100%" → "cien por ciento" |
| 3.5 | `ordinal_typo` | TRANSFORMAR a texto | "3er" → "tercer" |
| 3.6 | `estrellas` | TRANSFORMAR a texto | "5 estrellas" → "cinco estrellas" |
| 3.7 | `duracion_typo` | PRESERVAR | "24h" se mantiene |
| 3.8 | `cero_absoluto` | PRESERVAR | "cero problemas" se mantiene |
| 3.9 | `numero_aislado` | ELIMINAR | Números sin contexto → "" |

> ⚠️ **Orden interno crítico:** Las transformaciones a texto (3.1-3.6) se ejecutan ANTES de eliminar números aislados (3.9), protegiendo los números con valor semántico.

### 3.3 Tokens Especiales Generados

| Categoría | Tokens |
|-----------|--------|
| Numéricos | `<PRECIO>` |
| Puntuación | `<EXCL_2>`, `<EXCL_3>`, `<EXCL_MULT>`, `<INTER_2>`, `<INTER_3>`, `<INTER_MULT>`, `<SUSP_3>`, `<SUSP_MULT>`, `<MIXTO>` |
| Caracteres | `<PORCENTAJE>` |

**Total:** 11 tokens especiales

### 3.4 Configuración TF-IDF para Tokens

```python
# Token pattern que captura palabras y tokens especiales
token_pattern = r'(?u)\b\w+\b|<[A-Z_0-9]+>'

# Stopwords a eliminar (403 palabras)
stop_words = list(STOPWORDS_ELIMINAR_FINAL)
```

---

## 4. Estrategia de Modelado

### 4.1 Enfoque General

```
PARTE 2: Pipeline       →  Preprocesar df_dev y df_test
    ↓
PARTE 3: Baseline       →  Regresión Logística + TF-IDF inicial
    ↓                      Evaluar con K-Fold CV
    ↓                      Medir tiempo de entrenamiento
    ↓
PARTE 4: Experimentación →  Optimizar hiperparámetros TF-IDF
    ↓                      [Opcional] Probar otros modelos
    ↓
PARTE 5: Modelo Final   →  Entrenar en df_dev completo
                           Evaluar en df_test (UNA VEZ)
                           Guardar artefactos
                           Implementar función de inferencia
```

### 4.2 Baseline: Regresión Logística

**¿Por qué Regresión Logística?**

| Ventaja | Relevancia para el proyecto |
|---------|------------|
| Rápido de entrenar | Permite iterar rápido en experimentación |
| Interpretable | Coeficientes → keywords |
| `predict_proba()` | Cumple contrato (probability) |
| Funciona bien con TF-IDF | Combo clásico probado |

**Configuración inicial:**

```python
LogisticRegression(
    multi_class='multinomial',
    solver='lbfgs',
    max_iter=1000,
    random_state=42
)
```

### 4.3 Configuración TF-IDF Baseline

| Parámetro | Valor Inicial | Notas |
|-----------|---------------|-------|
| `ngram_range` | (1, 2) | Unigramas + bigramas |
| `max_features` | 20,000 | Límite conservador |
| `min_df` | 3 | Mínimo 3 documentos |
| `max_df` | 0.9 | Máximo 90% de documentos |
| `lowercase` | False | Preservar MAYÚSCULAS |
| `token_pattern` | custom | Capturar tokens especiales |
| `stop_words` | lista custom | 403 stopwords |

### 4.4 Hiperparámetros a Experimentar

| Parámetro | Opciones a Probar |
|-----------|-------------------|
| `ngram_range` | (1,2), (1,3), (1,4) |
| `max_features` | 10K, 20K, 30K |
| `min_df` | 2, 3, 5 |
| `max_df` | 0.8, 0.9, 0.95 |

### 4.5 Feature Adicional: `text_length` (Experimental)

> 📄 **Referencia:** DECISIONES 3.8

| Aspecto | Detalle |
|---------|---------|
| **Feature** | `text_length` = número de palabras por reseña |
| **Cramér's V** | 0.0898 (límite entre negligible y pequeño) |
| **Validación** | Comparar F1-Macro con y sin `text_length` usando K-Fold CV |
| **Umbral de inclusión** | Incluir si ΔF1-Macro ≥ 0.5% |
| **Implementación** | Normalizar con `StandardScaler` y concatenar con matriz TF-IDF usando `scipy.sparse.hstack` |

### 4.6 Modelos Robustos (Condicional)

> **Decisión basada en tiempo de entrenamiento del baseline**

| Tiempo Baseline | Acción |
|-----------------|--------|
| < 2 minutos | Probar 2-3 modelos adicionales |
| 2-5 minutos | Probar 1 modelo adicional |
| > 5 minutos | Solo optimizar baseline |

**Candidatos:**

1. **LinearSVC** - Similar a LogReg pero puede ser más rápido
2. **SGDClassifier** - Escalable para datos grandes

---

## 5. Métricas de Evaluación

### 5.1 Métrica Principal

- **F1-Macro:** Promedio de F1 por clase (no sesgado por desbalance)

### 5.2 Métricas Secundarias

| Métrica | Propósito |
|---------|-----------|
| Accuracy | Visión general |
| F1 por clase | Especialmente NEUTRO |
| Confusion Matrix | Patrones de error |

### 5.3 Análisis Especial: Clase NEUTRO

- Monitorear recall de clase NEUTRO
- Analizar confusiones NEUTRO ↔ POSITIVO y NEUTRO ↔ NEGATIVO
- Identificar patrones de error para mejora futura

---

## 6. Artefactos a Generar

### 6.1 Archivos de Modelo

| Archivo | Contenido |
|---------|-----------|
| `modelo_sentiment.joblib` | Modelo entrenado |
| `tfidf_vectorizer.joblib` | Vectorizador TF-IDF fitted |
| `stopwords_eliminar.txt` | Lista de 403 stopwords |
| `config_modelo.json` | Hiperparámetros finales |

### 6.2 Clase de Inferencia

```python
class SentimentPredictor:
    def __init__(self, model_path, vectorizer_path):
        """Carga modelo y vectorizador"""
        ...
    
    def preprocesar(self, texto: str) -> str:
        """Aplica pipeline de preprocesamiento"""
        ...
    
    def predict(self, texto: str) -> dict:
        """
        Retorna: {
            "prediction": str,
            "probability": float,
            "keywords": List[str],
            "timestamp": str
        }
        """
        ...
    
    def extraer_keywords(self, texto: str, prediccion: str, top_n: int = 5) -> List[str]:
        """Extrae palabras que más contribuyen a la predicción"""
        ...
```

---

## 7. Checklist de Entregables

### PARTE 2: Pipeline de Preprocesamiento

- [ ] Todas las funciones implementadas
- [ ] Función maestra `preprocesar_texto()`
- [ ] Pipeline aplicado a df_dev y df_test
- [ ] Validación con ejemplos

### PARTE 3: Baseline

- [ ] TF-IDF configurado
- [ ] Regresión Logística entrenada
- [ ] Métricas con K-Fold CV
- [ ] Tiempo de entrenamiento medido
- [ ] Decisión: ¿cuántos modelos probar?

### PARTE 4: Experimentación

- [ ] Grid search de hiperparámetros TF-IDF
- [ ] Mejor configuración identificada
- [ ] [Opcional] Modelos adicionales evaluados

### PARTE 5: Modelo Final

- [ ] Modelo entrenado en df_dev completo
- [ ] Evaluación en df_test (métricas oficiales)
- [ ] Artefactos guardados
- [ ] Clase SentimentPredictor funcional
- [ ] Tests de inferencia

---

## 8. Referencias

| Documento | Contenido |
|-----------|-----------|
| `PLAN_PREPROCESAMIENTO.md` | Plan original Fase A |
| `DECISIONES_PREPROCESAMIENTO.md` | Todas las decisiones tomadas |
| `EDA_sentiment.ipynb` | Análisis exploratorio completo |
| `ngramas_discriminativos_multi_n.csv` | N-gramas discriminativos |

---

## 9. Notebook de Implementación

**Archivo:** `modelo_sentiment.ipynb`

### Estructura del Notebook

```
PARTE 1: CONFIGURACIÓN
├── 1.1 Imports y paths
└── 1.2 Cargar datos (df_dev, df_test)

PARTE 2: PIPELINE DE PREPROCESAMIENTO
├── 2.1 Funciones individuales
├── 2.2 Función maestra: preprocesar_texto()
├── 2.3 Aplicar a datasets
└── 2.4 Validación con ejemplos

PARTE 3: BASELINE
├── 3.1 TF-IDF configuración inicial
├── 3.2 Regresión Logística
├── 3.3 K-Fold CV (5 folds)
├── 3.4 Métricas y análisis
└── 3.5 DECISIÓN: ¿Tiempo permite más modelos?

PARTE 4: EXPERIMENTACIÓN
├── 4.1 Grid de hiperparámetros TF-IDF
├── 4.2 Búsqueda con CV
├── 4.3 Mejor configuración
└── 4.4 [Opcional] Otros modelos

PARTE 5: MODELO FINAL
├── 5.1 Entrenar en df_dev completo
├── 5.2 Evaluar en df_test
├── 5.3 Guardar artefactos
└── 5.4 Función de inferencia (cumple contrato)
```

---

## Historial de Actualizaciones

| Fecha | Cambio |
|-------|--------|
| 2026-01-23 | Documento inicial - Fase B |

---

*Este documento sirve como guía para la implementación del modelo de clasificación de sentimientos.*
