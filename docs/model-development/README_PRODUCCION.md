# 🚀 Clasificador de Sentimientos - Documentación de Producción

## 📋 Descripción

Sistema de clasificación de sentimientos en español para reseñas de productos. Utiliza **Logistic Regression** con vectorización **TF-IDF** y un pipeline de preprocesamiento de 7 pasos optimizado para análisis de opiniones.

### **Características Principales**
- 🎯 **3 clases**: Negativo, Neutro, Positivo
- 📊 **Accuracy**: 83.33% (validado en reseñas controladas)
- ⚡ **Rápido**: ~3-4ms por predicción
- 🔧 **CPU-friendly**: No requiere GPU
- 📝 **Keywords**: Extracción automática de palabras clave

---

## 📁 Estructura del Proyecto

```
project/
├── src/                                    # Código de producción
│   ├── __init__.py                         # Exporta SentimentPredictor
│   ├── preprocessing.py                    # Pipeline de 7 pasos
│   ├── model_loader.py                     # Singleton para cargar artefactos
│   ├── predictor.py                        # Clase principal de inferencia
│   ├── keyword_extractor.py                # Extracción de keywords
│   ├── config.py                           # Configuración centralizada
│   ├── api_schema.py                       # Schemas Pydantic (opcional)
│   └── logging_config.py                   # Configuración de logs
│
├── tests/                                  # Tests de integración
│   ├── __init__.py
│   └── test_integration.py                 # 4 tests (estructura, error, performance)
│
├── modelo_sentiment_final.joblib           # 🎯 Modelo entrenado (235.39 KB)
├── tfidf_vectorizer_final.joblib           # 🎯 Vectorizador TF-IDF (378.00 KB)
├── stopwords_eliminar.txt                  # 🎯 Lista de stopwords (516 palabras)
│
├── modelo_sentiment.ipynb                  # Notebook de desarrollo/entrenamiento
├── produccion_sentiment_plan.ipynb         # Plan de producción (documentación)
│
├── test_produccion_controladas.py          # Script de validación con 60 reseñas
├── validar_pipeline.py                     # Script de validación rápida
│
└── README_PRODUCCION.md                    # 📖 Esta documentación
```

---

## 🔧 Instalación

### **1. Requisitos del Sistema**
- Python 3.10 o superior
- 1 GB de RAM mínimo
- ~10 MB de espacio en disco

### **2. Instalar Dependencias**

```bash
pip install scikit-learn==1.8.0 joblib numpy
```

**Dependencias opcionales** (para tests):
```bash
pip install pytest
```

### **3. Verificar Instalación**

```bash
# Ejecutar tests de integración
python -m pytest tests/test_integration.py -v

# O validación rápida
python validar_pipeline.py
```

---

## 🚀 Uso Rápido

### **Ejemplo Básico**

```python
from src import SentimentPredictor

# Inicializar predictor (carga modelo automáticamente)
predictor = SentimentPredictor()

# Predecir sentimiento
texto = "Excelente producto, superó mis expectativas!!!"
result = predictor.predict(texto)

print(f"Predicción: {result.prediction}")      # "Positivo"
print(f"Probabilidad: {result.probability}")   # 0.98
print(f"Keywords: {result.keywords}")          # ['excelente', 'superó', ...]
print(f"Timestamp: {result.timestamp}")        # ISO 8601 UTC
```

### **Ejemplo con Múltiples Reseñas**

```python
from src import SentimentPredictor

predictor = SentimentPredictor()

reseñas = [
    "El producto llegó roto, muy mala calidad",
    "Cumple su función, nada especial",
    "¡Increíble! Mejor compra del año"
]

for texto in reseñas:
    result = predictor.predict(texto)
    print(f"{result.prediction:10s} ({result.probability:.0%}) - {texto[:40]}...")
```

**Salida:**
```
Negativo   (97%) - El producto llegó roto, muy mala calid...
Neutro     (61%) - Cumple su función, nada especial...
Positivo   (73%) - ¡Increíble! Mejor compra del año...
```

### **Manejo de Errores**

```python
from src import SentimentPredictor

predictor = SentimentPredictor()

# Textos inválidos lanzan ValueError
try:
    result = predictor.predict("")  # Texto vacío
except ValueError as e:
    print(f"Error: {e}")  # "'texto' no puede estar vacio"

try:
    result = predictor.predict(None)  # None
except ValueError as e:
    print(f"Error: {e}")  # "'texto' no puede ser None"
```

### **Formato de Respuesta**

```python
result = predictor.predict("Texto de ejemplo")

# Atributos disponibles
result.prediction   # str: "Negativo" | "Neutro" | "Positivo"
result.probability  # float: 0.0 a 1.0 (redondeado a 2 decimales)
result.keywords     # list[str]: Top 5 palabras clave
result.timestamp    # str: ISO 8601 UTC (ej: "2026-01-25T15:14:15.594167+00:00")

# Convertir a diccionario
data = result.to_dict()
# {
#     "prediction": "Positivo",
#     "probability": 0.98,
#     "keywords": ["excelente", "superó", "expectativas", "producto"],
#     "timestamp": "2026-01-25T15:14:15.594167+00:00"
# }
```

---

## 🎯 Artefactos del Modelo

### **modelo_sentiment_final.joblib** (235.39 KB)
**Modelo**: Logistic Regression
- **Hiperparámetros**:
  - `C=0.5` (regularización)
  - `class_weight='balanced'` (balanceo de clases)
  - `solver='lbfgs'`
  - `multi_class='multinomial'`
  - `max_iter=1000`
- **Clases**: `['negativo', 'neutro', 'positivo']`
- **Entrenamiento**: 194,273 muestras limpias (Amazon Reviews Multi - español)

### **tfidf_vectorizer_final.joblib** (378.00 KB)
**Vectorizador**: TfidfVectorizer
- **Configuración**:
  - `max_features=10,000` (vocabulario de 10K términos)
  - `ngram_range=(1, 2)` (unigramas + bigramas)
  - `min_df=3` (mínimo 3 documentos)
  - `max_df=0.9` (máximo 90% de documentos)
  - `lowercase=False` (preserva mayúsculas ALL-CAPS)
  - `token_pattern=r'<[A-Z_0-9]+>|\b\w\w+\b'` (palabras + tokens especiales)
- **Stopwords**: Cargadas desde `stopwords_eliminar.txt`

### **stopwords_eliminar.txt** (516 palabras)
Lista de palabras vacías eliminadas durante vectorización:
- Obtenida de spaCy español
- Excluye palabras con valor discriminativo (ej: "no", "sin", "nunca")
- Codificación UTF-8

---

## 🔄 Pipeline de Preprocesamiento

El modelo aplica **7 pasos secuenciales** antes de vectorizar:

### **1. limpiar_ruido()**
Elimina elementos sin valor semántico:
- URLs (`https://...`, `www.ejemplo.com`)
- Emails (`usuario@dominio.com`)
- Menciones (`@usuario`)
- Hashtags (`#tema`)
- Caracteres `<` y `>` del texto original

**Ejemplo:**
```python
Entrada:  "Visita www.ejemplo.com @usuario #oferta gran producto"
Salida:   "gran producto"
```

### **2. eliminar_emojis()**
Elimina todos los emojis Unicode.

**Ejemplo:**
```python
Entrada:  "Me encanta 😍🎉 este producto 👍"
Salida:   "Me encanta este producto"
```

### **3. tokenizar_caracteres_especiales()**
Convierte precios y porcentajes a tokens especiales:
- `15.99€` → `<PRECIO>`
- `$29.99` → `<PRECIO>`
- `50%` → `<PORCENTAJE>`

**Ejemplo:**
```python
Entrada:  "Cuesta 29.99€ con 20% descuento"
Salida:   "Cuesta <PRECIO> con <PORCENTAJE> descuento"
```

### **4. tokenizar_patrones_numericos()**
Convierte números contextuales a texto, elimina números aislados:
- `3 días` → `tres dias`
- `5 estrellas` → `cinco estrellas`
- `2 veces` → `dos veces`
- `1er` → `primer`
- Números aislados → (eliminados)

**Ejemplo:**
```python
Entrada:  "Llegó en 2 días, 5 estrellas, compré 3 veces"
Salida:   "Llegó en dos dias cinco estrellas compré tres veces"
```

### **5. tokenizar_puntuacion_repetida()**
Convierte puntuación repetida a tokens de intensidad:
- `!!!` → `<EXCL_3>`
- `!!!!` → `<EXCL_MULT>`
- `???` → `<INTER_3>`
- `...` → `<SUSP_3>`
- `!?!?` → `<MIXTO>`

**Ejemplo:**
```python
Entrada:  "Excelente!!! ¿Por qué??? Genial..."
Salida:   "Excelente <EXCL_3> Por qué <INTER_3> Genial <SUSP_3>"
```

### **6. eliminar_caracteres_restantes()**
Elimina caracteres especiales sin valor:
- `( ) - / + * = # & ^ ~ \ | [ ] { } @ _`
- **Preserva**: Tokens especiales `<TOKEN>`

**Ejemplo:**
```python
Entrada:  "Calidad (premium) [5/5] @marca"
Salida:   "Calidad premium marca"
```

### **7. normalizar_mayusculas()**
Normaliza mayúsculas preservando énfasis:
- Palabras ALL-CAPS (≥2 chars) → **PRESERVADAS** (indican énfasis emocional)
- Tokens especiales → Preservados tal cual
- Resto → minúsculas

**Ejemplo:**
```python
Entrada:  "EXCELENTE Producto Normal <PRECIO>"
Salida:   "EXCELENTE producto normal <PRECIO>"
```

### **Pipeline Completo - Ejemplo**

```python
Entrada:  "¡¡¡INCREÍBLE!!! Compré este producto en www.tienda.com por 29.99€ 
           con 20% descuento 😍 Llegó en 2 días. @vendedor ⭐⭐⭐⭐⭐ 5 estrellas"

Después de preprocesar_texto():
Salida:   "INCREIBLE <EXCL_3> Compré este producto en por <PRECIO> con <PORCENTAJE> 
           descuento Llegó en dos dias cinco estrellas"
```

---

## 📊 Métricas de Desempeño

### **Validación con Reseñas Controladas** (60 reseñas, 20 por clase)

| Métrica | Producción | Objetivo | Estado |
|---------|-----------|----------|--------|
| **Accuracy** | 83.33% | 83.33% | ✅ Cumple |
| **F1-Macro** | 0.8344 | 0.8318 | ✅ Superior |
| **F1-Negativo** | 0.9231 | 0.9268 | ⚠️ -0.37% |
| **F1-Neutro** | 0.7895 | 0.7778 | ✅ +1.17% |
| **F1-Positivo** | 0.7907 | 0.7907 | ✅ Cumple |

### **Matriz de Confusión**

```
              Predicho
             Neg  Neu  Pos
Real: Neg     18    1    1     (90% recall)
      Neu      0   15    5     (75% recall)
      Pos      1    2   17     (85% recall)
```

### **Aciertos por Clase**
- **Negativo**: 18/20 (90%)
- **Neutro**: 15/20 (75%) - Clase más difícil
- **Positivo**: 17/20 (85%)

### **Performance**
- ⚡ **Tiempo de inferencia**: 3-4 ms por predicción
- 💾 **Memoria**: ~1 MB en RAM (modelo + vectorizador)
- 🔋 **CPU**: 1 core suficiente

---

## 🧪 Ejecución de Tests

### **Tests de Integración**

```bash
# Ejecutar todos los tests
python -m pytest tests/test_integration.py -v

# Salida esperada:
# test_prediccion_estructura_y_rangos PASSED
# test_probabilidad_es_maxima_del_vector PASSED
# test_texto_vacio_lanza_error PASSED
# test_tiempo_inferencia_rapido PASSED
# ========================= 4 passed in 0.90s ==========================
```

### **Validación con Reseñas Controladas**

```bash
python test_produccion_controladas.py

# Valida 60 reseñas diseñadas manualmente
# Compara métricas vs. modelo entrenado
```

### **Validación Rápida**

```bash
python validar_pipeline.py

# Prueba 5 casos representativos
# Muestra predicciones + keywords + probabilidades
```

---

## ⚙️ Configuración Avanzada

### **Variables de Entorno**

```bash
# Rutas personalizadas para artefactos
export MODEL_PATH="/ruta/custom/modelo.joblib"
export VECTORIZER_PATH="/ruta/custom/vectorizer.joblib"
export STOPWORDS_PATH="/ruta/custom/stopwords.txt"

# Configuración de inferencia
export KEYWORDS_TOP_N=10        # Top N keywords (default: 5)
export PROB_DECIMALS=3          # Decimales de probabilidad (default: 2)

# Logging
export LOG_LEVEL=DEBUG          # DEBUG, INFO, WARNING, ERROR
```

### **Configuración en Código**

```python
from src import config

# Sobrescribir configuración
config.KEYWORDS_TOP_N = 10
config.PROB_DECIMALS = 3

# Usar predictor con config personalizada
from src import SentimentPredictor
predictor = SentimentPredictor()
```

---

## 🐛 Troubleshooting

### **Error: "No module named 'src'"**
```bash
# Asegúrate de ejecutar desde el directorio raíz del proyecto
cd /ruta/al/project
python -c "from src import SentimentPredictor"
```

### **Error: "Faltan artefactos de modelo/vectorizer"**
```bash
# Verifica que los 3 archivos existan:
ls -lh modelo_sentiment_final.joblib
ls -lh tfidf_vectorizer_final.joblib
ls -lh stopwords_eliminar.txt

# Si faltan, regenerarlos desde modelo_sentiment.ipynb
# Ejecutar celdas 185-186 del notebook
```

### **Rendimiento lento (>500ms por predicción)**
```python
# Verificar que el modelo se carga una sola vez
from src import SentimentPredictor

# ❌ MAL: Crea nueva instancia cada vez
for texto in textos:
    predictor = SentimentPredictor()  # Recarga modelo (lento)
    result = predictor.predict(texto)

# ✅ BIEN: Reutiliza la misma instancia
predictor = SentimentPredictor()  # Carga modelo una vez
for texto in textos:
    result = predictor.predict(texto)  # Rápido (~3ms)
```

### **Métricas diferentes al validar**
- Las pequeñas variaciones (<1%) son normales entre ejecuciones
- Verificar que preprocessing.py coincida con el notebook
- Confirmar que stopwords_eliminar.txt tenga 516 palabras

---

## 📚 Recursos Adicionales

### **Notebooks de Referencia**
- **modelo_sentiment.ipynb**: Desarrollo completo del modelo (PARTE 1-11)
- **produccion_sentiment_plan.ipynb**: Plan de producción detallado

### **Documentación Técnica**
- **PLAN_MODELADO.md**: Decisiones de diseño del modelo
- **PLAN_PREPROCESAMIENTO.md**: Decisiones del pipeline de preprocesamiento
- **DECISIONES_PREPROCESAMIENTO.md**: Justificación de cada paso

### **Archivos de Análisis**
- **EDA_sentiment.ipynb**: Análisis exploratorio de datos
- **ngramas_discriminativos_multi_n.csv**: N-gramas más discriminativos

---

## 🔮 Próximos Pasos (Opcional)

### **1. API REST con FastAPI**
```python
# app.py (ejemplo)
from fastapi import FastAPI
from src import SentimentPredictor
from src.api_schema import SentimentRequest, SentimentResponse

app = FastAPI()
predictor = SentimentPredictor()

@app.post("/predict", response_model=SentimentResponse)
def predict(request: SentimentRequest):
    result = predictor.predict(request.texto)
    return SentimentResponse(
        prediction=result.prediction,
        probability=result.probability,
        keywords=result.keywords,
        timestamp=result.timestamp
    )
```

### **2. Dockerización**
```dockerfile
FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```

### **3. Monitoreo y Logging**
- Implementar logging de predicciones
- Métricas de performance (latencia, throughput)
- Dashboard con Prometheus + Grafana

---

## 📄 Licencia

Este proyecto fue desarrollado como parte del **Hackathon ONE - No Country**.

---

## 👥 Contacto

Para consultas o issues, contactar al equipo de desarrollo.

**Última actualización**: Enero 25, 2026
**Versión del modelo**: 1.0.0
