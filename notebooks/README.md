# 📓 Notebooks de Desarrollo del Modelo

Esta carpeta contiene los notebooks Jupyter utilizados en el proceso de desarrollo, entrenamiento y validación del modelo de clasificación de sentimientos.

---

## 📂 Contenido

### 01_EDA_sentiment.ipynb
**Análisis Exploratorio de Datos (EDA)**

- 📊 Exploración del dataset de reseñas
- 📈 Visualización de distribución de sentimientos
- 🔍 Análisis de patrones en el texto
- 📉 Identificación de desbalance de clases
- 🧹 Análisis de calidad de datos

**Salidas principales:**
- Estadísticas descriptivas del dataset
- Gráficos de distribución
- Análisis de longitud de textos
- Identificación de palabras más frecuentes

---

### 02_modelo_sentiment.ipynb
**Entrenamiento y Evaluación del Modelo**

- 🧪 Experimentación con diferentes algoritmos
- ⚙️ Tuning de hiperparámetros
- 📊 Evaluación de métricas (Accuracy, F1, Precision, Recall)
- 🔬 Validación cruzada
- 💾 Exportación de artefactos finales

**Modelo final seleccionado:**
- **Algoritmo:** LogisticRegression
- **Hiperparámetros:** C=0.5, class_weight='balanced'
- **Vectorizador:** TfidfVectorizer (ngram_range=(1,2), max_features=1500)
- **Accuracy:** 83.33%
- **F1-Macro:** 0.8344

**Artefactos generados:**
- `modelo_sentiment_final.joblib`
- `tfidf_vectorizer_final.joblib`
- `stopwords_eliminar.txt`

---

### 03_produccion_sentiment_plan.ipynb
**Planificación y Validación para Producción**

- 🔧 Diseño del pipeline de preprocesamiento
- ✅ Validación end-to-end del modelo
- 🧪 Tests de casos edge
- 📦 Preparación para integración con API
- 📝 Documentación de decisiones técnicas

**Validaciones realizadas:**
- 25 tests end-to-end (100% pasados)
- Casos normales (positivo, negativo, neutro)
- Casos edge (textos muy cortos/largos, caracteres especiales)
- Manejo de errores
- Consistencia de predicciones

---

## 🚀 Cómo Usar Estos Notebooks

### Prerequisitos
```bash
# Instalar dependencias
pip install jupyter pandas numpy scikit-learn matplotlib seaborn

# Activar entorno virtual (recomendado)
python -m venv .venv
.venv\Scripts\activate  # Windows
source .venv/bin/activate  # Linux/Mac
```

### Ejecutar Notebooks
```bash
# Iniciar Jupyter
jupyter notebook

# O usar Jupyter Lab
jupyter lab
```

### Orden Recomendado
1. **01_EDA_sentiment.ipynb** - Entender los datos
2. **02_modelo_sentiment.ipynb** - Ver proceso de entrenamiento
3. **03_produccion_sentiment_plan.ipynb** - Validación para producción

---

## 📊 Datasets Utilizados

Los notebooks esperan encontrar los siguientes archivos de datos:
- `reseñas_train.csv` - Dataset de entrenamiento
- `reseñas_test.csv` - Dataset de prueba
- `stopwords_eliminar.txt` - Stopwords customizadas

**Nota:** Los datasets no se incluyen en el repositorio por razones de tamaño y privacidad.

---

## 📝 Documentación Relacionada

Para más detalles sobre el proceso de desarrollo del modelo, consulta:
- [DECISIONES_PREPROCESAMIENTO.md](../docs/model-development/DECISIONES_PREPROCESAMIENTO.md)
- [PLAN_MODELADO.md](../docs/model-development/PLAN_MODELADO.md)
- [INFORME_VALIDACION.md](../docs/model-development/INFORME_VALIDACION.md)

---

## ⚠️ Notas Importantes

### Reproducibilidad
- Los notebooks fueron ejecutados con Python 3.14.2
- Se recomienda usar el mismo entorno para garantizar reproducibilidad
- Las semillas aleatorias están fijadas cuando es posible

### Tamaño de Archivos
- `01_EDA_sentiment.ipynb` (~4 MB con outputs)
- `02_modelo_sentiment.ipynb` (~843 KB con outputs)
- `03_produccion_sentiment_plan.ipynb` (~80 KB)

### Versionamiento
Los notebooks incluyen outputs de ejecución para facilitar la revisión sin necesidad de re-ejecutar. Si deseas limpiar los outputs:

```bash
# Limpiar outputs de un notebook
jupyter nbconvert --clear-output --inplace notebook.ipynb

# Limpiar todos los notebooks
jupyter nbconvert --clear-output --inplace *.ipynb
```

---

## 🔄 Actualización del Modelo

Si necesitas re-entrenar o actualizar el modelo:

1. Ejecutar `01_EDA_sentiment.ipynb` para análisis de nuevos datos
2. Ejecutar `02_modelo_sentiment.ipynb` para entrenar nuevo modelo
3. Ejecutar `03_produccion_sentiment_plan.ipynb` para validar
4. Copiar artefactos a `data-science/models/`:
   ```bash
   cp modelo_sentiment_final.joblib ../data-science/models/sentiment_model.joblib
   cp tfidf_vectorizer_final.joblib ../data-science/models/tfidf_vectorizer.joblib
   cp stopwords_eliminar.txt ../data-science/models/
   ```
5. Ejecutar tests de integración
6. Actualizar documentación

---

## 📞 Contacto

Para preguntas sobre el proceso de desarrollo del modelo:
- Revisar documentación en `/docs/model-development/`
- Consultar con el equipo de Data Science

---

**Última actualización:** Enero 25, 2026  
**Versión del modelo:** 2.0 (Production-Ready)
