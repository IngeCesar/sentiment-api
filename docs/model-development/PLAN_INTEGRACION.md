# 📋 Plan de Integración al Repositorio sentiment-api

**Fecha:** 25 de enero de 2026  
**Repositorio:** https://github.com/cesiumflow/sentiment-api  
**Rama actual:** develop (default)

---

## 🔍 Análisis del Repositorio Existente

### Estructura General
```
sentiment-api/
├── frontend/          # Vue 3 + Vite (SPA)
├── core-service/      # Java Spring Boot (Orquestador)
├── data-science/      # Python FastAPI (Motor ML) ⭐ AQUÍ INTEGRAMOS
├── db/                # PostgreSQL schemas
├── .github/           # CI/CD workflows
└── docs/              # Documentación
```

### Ramas Disponibles
- **develop** (actual) - Rama de desarrollo activa
- **main** - Producción estable
- **feature/cloud-deploy** - Deploy en la nube

### Estado del Servicio `data-science/`

#### Estructura Actual
```
data-science/
├── Dockerfile
├── requirements.txt           # Dependencias básicas
├── ds-api/
│   └── main.py               # FastAPI server (puerto 5000)
├── models/
│   ├── sentiment_model.joblib        # Modelo antiguo
│   └── tfidf_vectorizer.joblib       # Vectorizador antiguo
└── src/
    ├── sentiment_api.py              # Lógica de inferencia antigua
    └── cesiumflow_ml/
        ├── __init__.py
        ├── inference.py              # Engine de inferencia
        └── preprocessing.py          # Limpieza simple de texto
```

#### Código Existente - Características

**preprocessing.py actual:**
- ✅ Limpieza básica (lowercase, URLs, menciones)
- ❌ NO tiene tokenización de patrones numéricos
- ❌ NO tiene tokenización de puntuación repetida
- ❌ NO tiene normalización de mayúsculas
- ❌ NO tiene manejo de ordinales
- ❌ Pipeline de 1 paso vs nuestro pipeline de 7 pasos

**inference.py actual:**
- ✅ Carga de modelos con joblib
- ✅ Predicción con probabilidades
- ✅ Extracción de keywords básica
- ❌ Stopwords hardcodeadas (solo 10 palabras)
- ❌ Sin archivo stopwords_eliminar.txt

**sentiment_api.py actual:**
- ✅ Funciones: limpiar_texto(), extraer_keywords(), predecir_sentimiento()
- ⚠️ Preprocesamiento simplificado
- ⚠️ Predicción sanitiza corchetes [POS] → POS

---

## 🎯 Objetivo de la Integración

**Reemplazar el modelo antiguo con nuestro modelo validado de producción:**
- Modelo LogisticRegression (C=0.5, balanced)
- Accuracy: 83.33% (vs. desconocido del anterior)
- Pipeline de preprocesamiento completo (7 pasos)
- 516 stopwords customizadas
- Validado con 25/25 tests end-to-end

---

## 🚨 Puntos Críticos - NO ROMPER

### 1. Contrato de API (main.py)
El endpoint `/predict` debe mantener la misma estructura:

**Request:**
```json
{
  "text": "string",
  "top_n_keywords": 5  // opcional
}
```

**Response:**
```json
{
  "prediction": "Positivo",  // NO [POS], debe ser capitalizado
  "probability": 0.97,
  "keywords": ["excelente", "calidad", ...],
  "timestamp": "2026-01-25T15:39:08.886444+00:00"
}
```

### 2. Puerto y Configuración
- Puerto: `5000` (o variable de entorno `PORT`)
- Health endpoint: `/health`
- FastAPI con uvicorn

### 3. Nombres de Archivos de Modelos
```
models/
├── sentiment_model.joblib        # ⚠️ Nombre actual
└── tfidf_vectorizer.joblib       # ⚠️ Nombre actual
```

**Nuestros nombres:**
```
├── modelo_sentiment_final.joblib
├── tfidf_vectorizer_final.joblib
└── stopwords_eliminar.txt        # ⚠️ NUEVO archivo
```

---

## 📝 Plan de Integración Paso a Paso

### FASE 1: Preparación y Backup ✅

#### 1.1 Crear rama de trabajo
```bash
cd "c:\Users\LENOVO\OneDrive\Cursos\Portafolio\No country\Hackaton ONE\sentiment-api"
git checkout develop
git pull origin develop
git checkout -b feature/production-model-integration
```

#### 1.2 Backup del código existente
```bash
# Copiar versión actual a carpeta de backup
mkdir data-science-backup
Copy-Item -Path data-science/* -Destination data-science-backup/ -Recurse
```

---

### FASE 2: Integración de Archivos 🔄

#### 2.1 Copiar Artefactos del Modelo

**Acción:**
```powershell
# Copiar modelos con RENOMBRE para mantener compatibilidad
Copy-Item -Path "../project/modelo_sentiment_final.joblib" `
          -Destination "data-science/models/sentiment_model.joblib" -Force

Copy-Item -Path "../project/tfidf_vectorizer_final.joblib" `
          -Destination "data-science/models/tfidf_vectorizer.joblib" -Force

# Copiar archivo nuevo de stopwords
Copy-Item -Path "../project/stopwords_eliminar.txt" `
          -Destination "data-science/models/stopwords_eliminar.txt"
```

**⚠️ IMPORTANTE:** Renombramos los archivos para que coincidan con los nombres esperados por el código existente.

#### 2.2 Integrar Código de Preprocesamiento

**Opción A: Reemplazar completamente** (RECOMENDADO)
```powershell
# Backup del archivo antiguo
Copy-Item -Path "data-science/src/cesiumflow_ml/preprocessing.py" `
          -Destination "data-science/src/cesiumflow_ml/preprocessing.py.bak"

# Copiar nuestro preprocessing.py
Copy-Item -Path "../project/src/preprocessing.py" `
          -Destination "data-science/src/cesiumflow_ml/preprocessing.py" -Force
```

**Opción B: Mantener ambos** (CONSERVADOR)
```powershell
# Agregar nuestro módulo como archivo separado
Copy-Item -Path "../project/src/preprocessing.py" `
          -Destination "data-science/src/cesiumflow_ml/preprocessing_v2.py"
```

#### 2.3 Integrar Predictor

```powershell
# Copiar otros módulos necesarios
Copy-Item -Path "../project/src/config.py" `
          -Destination "data-science/src/cesiumflow_ml/config.py"

Copy-Item -Path "../project/src/keyword_extractor.py" `
          -Destination "data-science/src/cesiumflow_ml/keyword_extractor.py"

Copy-Item -Path "../project/src/model_loader.py" `
          -Destination "data-science/src/cesiumflow_ml/model_loader.py"

Copy-Item -Path "../project/src/predictor.py" `
          -Destination "data-science/src/cesiumflow_ml/predictor.py"
```

#### 2.4 Actualizar Requirements

**Archivo:** `data-science/requirements.txt`

**Cambios necesarios:**
```diff
  pandas>=2.0.3
  numpy>=1.24.4
- scikit-learn>=1.3.2
+ scikit-learn==1.8.0
  joblib>=1.3.2

  fastapi==0.127.1
  uvicorn==0.40.0
  pydantic==2.12.5

  requests==2.32.5
  regex==2025.11.3
+ python-dotenv>=1.0.0
```

---

### FASE 3: Adaptar main.py para Usar Nuestro Código 🔧

**Archivo:** `data-science/ds-api/main.py`

**Estrategia:** Modificar el main.py para usar `SentimentPredictor` de nuestro código.

**Cambios clave:**

```python
# ANTES (usa sentiment_api.py antiguo)
from sentiment_api import predecir_sentimiento, cargar_modelo

# DESPUÉS (usa nuestro predictor)
from cesiumflow_ml.predictor import SentimentPredictor
```

**Adaptación del endpoint `/predict`:**

```python
# Inicializar predictor en startup
@app.on_event("startup")
async def startup_event():
    try:
        ml_context["predictor"] = SentimentPredictor()
        ml_context["status"] = "ready"
        print("✅ Motor cargado con SentimentPredictor v2")
    except Exception as e:
        ml_context["status"] = "failed"
        print(f"❌ Error: {e}")

# Endpoint /predict adaptado
@app.post("/predict", response_model=SentimentResponse)
def predict_endpoint(request: ReviewRequest):
    if ml_context["status"] != "ready":
        raise HTTPException(status_code=503, detail="Model not ready")

    try:
        predictor = ml_context["predictor"]
        resultado = predictor.predict(request.text)
        
        # Convertir PredictionResult a dict
        result_dict = resultado.to_dict()
        
        return {
            "prediction": result_dict['prediction'],
            "probability": round(result_dict['probability'], 2),
            "keywords": result_dict['keywords'][:request.top_n_keywords],
            "timestamp": result_dict['timestamp']
        }
    except Exception as e:
        print(f"🔥 Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
```

---

### FASE 4: Actualizar Configuración 📝

#### 4.1 Modificar config.py

**Archivo:** `data-science/src/cesiumflow_ml/config.py`

```python
# Rutas adaptadas al repositorio
BASE_DIR = Path(__file__).parent.parent.parent  # data-science/
MODELS_DIR = BASE_DIR / "models"

MODELO_PATH = MODELS_DIR / "sentiment_model.joblib"
VECTORIZADOR_PATH = MODELS_DIR / "tfidf_vectorizer.joblib"
STOPWORDS_PATH = MODELS_DIR / "stopwords_eliminar.txt"
```

#### 4.2 Verificar Dockerfile

**Archivo:** `data-science/Dockerfile`

Asegurarse de que copia todos los archivos necesarios:
```dockerfile
# Copiar modelos
COPY models/ /app/models/

# Copiar código fuente
COPY src/ /app/src/
COPY ds-api/ /app/ds-api/
```

---

### FASE 5: Testing Local 🧪

#### 5.1 Test Manual (sin Docker)

```powershell
cd "data-science"

# Activar entorno virtual
python -m venv .venv
.venv\Scripts\Activate.ps1

# Instalar dependencias
pip install -r requirements.txt

# Ejecutar servidor
python ds-api/main.py
```

**Verificar:**
- ✅ Servidor inicia en puerto 5000
- ✅ `/health` responde {"status": "UP"}
- ✅ Modelo carga correctamente
- ✅ `/predict` funciona con texto de prueba

#### 5.2 Test con Docker

```powershell
# Construir imagen
docker build -t sentiment-engine:test data-science/

# Ejecutar contenedor
docker run -p 5000:5000 sentiment-engine:test

# Probar endpoint
curl -X POST http://localhost:5000/predict `
     -H "Content-Type: application/json" `
     -d '{"text": "Excelente producto, muy recomendado"}'
```

**Respuesta esperada:**
```json
{
  "prediction": "Positivo",
  "probability": 0.98,
  "keywords": ["excelente", "recomendado", "producto"],
  "timestamp": "2026-01-25T..."
}
```

#### 5.3 Test con docker-compose completo

```powershell
# Desde la raíz del repo
cd "c:\Users\LENOVO\OneDrive\Cursos\Portafolio\No country\Hackaton ONE\sentiment-api"

# Levantar stack completo
docker compose up -d --build

# Verificar que todos los servicios están UP
docker compose ps
```

---

### FASE 6: Integración de Tests 🧪

#### 6.1 Copiar Tests

```powershell
# Crear carpeta de tests si no existe
mkdir data-science/tests -Force

# Copiar tests
Copy-Item -Path "../project/tests/test_integration.py" `
          -Destination "data-science/tests/"

Copy-Item -Path "../project/tests/test_end_to_end.py" `
          -Destination "data-science/tests/"

Copy-Item -Path "../project/tests/__init__.py" `
          -Destination "data-science/tests/"
```

#### 6.2 Actualizar requirements.txt para tests

```diff
+ pytest>=9.0.2
+ pytest-asyncio>=0.24.0
```

#### 6.3 Ejecutar Tests

```powershell
cd data-science
pytest tests/ -v
```

**Resultado esperado:** 25/25 tests pasados

---

### FASE 7: Documentación 📚

#### 7.1 Actualizar README del data-science

```powershell
# Copiar documentación
Copy-Item -Path "../project/README_PRODUCCION.md" `
          -Destination "data-science/README_PRODUCCION.md"
```

#### 7.2 Agregar documentación técnica

```powershell
mkdir data-science/docs -Force

Copy-Item -Path "../project/INFORME_VALIDACION.md" `
          -Destination "data-science/docs/"

Copy-Item -Path "../project/DECISIONES_PREPROCESAMIENTO.md" `
          -Destination "data-science/docs/"
```

---

### FASE 8: Commit y Push 🚀

#### 8.1 Git Add

```bash
cd "sentiment-api"
git status

# Agregar cambios
git add data-science/
git add -u  # Agregar archivos modificados
```

#### 8.2 Commit con mensaje descriptivo

```bash
git commit -m "feat(data-science): upgrade to production-ready sentiment model

- Replace old model with validated LogisticRegression (83.33% accuracy)
- Implement 7-step preprocessing pipeline (vs 1-step basic cleaning)
- Add 516 custom stopwords (vs 10 hardcoded)
- Integrate SentimentPredictor with PredictionResult dataclass
- Add comprehensive tests (25/25 passed)
- Update scikit-learn to 1.8.0
- Maintain API contract compatibility (/predict endpoint)

Performance improvements:
- Inference time: 3.88ms/prediction
- F1-Macro: 0.8344
- Consistent predictions (100% reproducible)

Breaking changes: NONE
- API contract unchanged
- Model artifact names preserved
- Docker configuration compatible
"
```

#### 8.3 Push y Pull Request

```bash
# Push a rama feature
git push origin feature/production-model-integration

# Crear Pull Request en GitHub:
# develop ← feature/production-model-integration
```

---

## ✅ Checklist de Integración

### Pre-integración
- [x] Repositorio clonado
- [x] Ramas analizadas (develop, main, feature/cloud-deploy)
- [x] Estructura actual entendida
- [x] Código existente revisado
- [ ] Plan aprobado por el usuario

### Integración
- [ ] Rama `feature/production-model-integration` creada
- [ ] Backup del código antiguo realizado
- [ ] Modelos copiados y renombrados
- [ ] stopwords_eliminar.txt agregado
- [ ] preprocessing.py reemplazado
- [ ] Módulos adicionales copiados (config, predictor, etc.)
- [ ] requirements.txt actualizado
- [ ] main.py adaptado para usar SentimentPredictor
- [ ] config.py rutas corregidas

### Testing
- [ ] Test manual sin Docker (servidor local)
- [ ] Test endpoint /predict con curl
- [ ] Test con Docker build individual
- [ ] Test con docker-compose completo
- [ ] Tests unitarios ejecutados (25/25)
- [ ] Performance validada (< 10ms)

### Documentación
- [ ] README_PRODUCCION.md copiado
- [ ] INFORME_VALIDACION.md agregado
- [ ] Comentarios en código actualizados

### Git
- [ ] Cambios staged correctamente
- [ ] Commit con mensaje descriptivo
- [ ] Push a rama feature
- [ ] Pull Request creado en GitHub
- [ ] CI/CD pasa (si existe)

### Validación Final
- [ ] Frontend conecta correctamente
- [ ] Core-service recibe respuestas válidas
- [ ] No hay breaking changes
- [ ] Performance aceptable en stack completo
- [ ] Logs sin errores

---

## 🔄 Plan de Rollback (si algo sale mal)

### Si falla en desarrollo local:
```powershell
# Restaurar código antiguo
Remove-Item -Path "data-science/src" -Recurse -Force
Copy-Item -Path "data-science-backup/src" -Destination "data-science/" -Recurse
```

### Si falla después del push:
```bash
# Revertir commit
git reset --hard HEAD~1
git push origin feature/production-model-integration --force

# O crear commit de reversión
git revert HEAD
git push origin feature/production-model-integration
```

### Si falla en producción (después de merge):
```bash
# Revertir merge en develop
git checkout develop
git revert -m 1 <merge-commit-hash>
git push origin develop
```

---

## 📊 Comparativa: Antes vs Después

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Accuracy** | Desconocido | 83.33% |
| **F1-Macro** | Desconocido | 0.8344 |
| **Preprocessing** | 1 paso (limpieza básica) | 7 pasos (pipeline completo) |
| **Stopwords** | 10 hardcodeadas | 516 customizadas en archivo |
| **Tokenización** | No | Sí (números, fechas, emojis, puntuación) |
| **Tests** | No documentados | 25 tests (100% coverage) |
| **Inference time** | Desconocido | 3.88ms |
| **Consistencia** | No validada | 100% reproducible |
| **Documentación** | Básica | Completa (README + Informe) |

---

## 🎯 Siguiente Paso Recomendado

**Ejecutar FASE 1: Crear rama y hacer backup**

```bash
cd "c:\Users\LENOVO\OneDrive\Cursos\Portafolio\No country\Hackaton ONE\sentiment-api"
git checkout develop
git pull origin develop
git checkout -b feature/production-model-integration
mkdir data-science-backup
Copy-Item -Path data-science/* -Destination data-science-backup/ -Recurse
```

---

**¿Proceder con la integración?** (Sí/No)
