# Plan de Preprocesamiento - Clasificador de Sentimientos

## Información del Proyecto
- **Dataset:** Amazon Reviews Multi (Español)
- **Objetivo:** Clasificación de sentimientos (3 clases: negativo, neutro, positivo)
- **Modelo:** Clásico con TF-IDF
- **Fecha de creación:** 23 de enero de 2026

---

## Estructura del Plan

El plan se divide en dos fases:
- **Fase A:** Decisiones de preprocesamiento (análisis → evidencia → decisión)
- **Fase B:** Baseline y experimentación (configuración TF-IDF)

---

## FASE A: Decisiones de Preprocesamiento

### Resumen de Pasos

| # | Elemento | Pregunta Clave | Alcance | Insights |
|---|----------|----------------|---------|----------|
| 1 | **Números** | ¿Eliminar o preservar? | Dígitos, precios, fechas, cantidades | Patrones temporales ("dos semanas", "un mes") son discriminativos |
| 2 | **Puntuación repetida** | ¿Eliminar o tokenizar? | "!!!", "???", "...", combinadas | — |
| 3 | **Errores ortográficos** | ¿Corregir o dejar? | Typos, tildes omitidas | **Pueden ser discriminativos** - "e recibido" aparece casi solo en negativo |
| 4 | **Mayúsculas (count)** | ¿Incluir como feature? | Conteo de letras mayúsculas | — |
| 5 | **Mixed case** | ¿Incluir como feature? | Capitalización atípica | — |
| 6 | **Emojis** | ¿Tokenizar, score, o eliminar? | Emojis y valor semántico | — |
| 7 | **text_length** | ¿Añadir como feature? | Longitud del texto | Sugerido como feature útil |
| 8 | **Stopwords personalizadas** | ¿Cuáles preservar? | Negadores, intensificadores, pronombres, artículos indefinidos | Lista específica definida |

---

### Paso 8 - Detalle: Stopwords Personalizadas

**Principio:** NO eliminar palabras que son marcadores fuertes de sentimiento.

| Categoría | Palabras | Justificación |
|-----------|----------|---------------|
| **Negadores** | `no`, `nunca`, `ni`, `tampoco`, `jamás` | Núcleo de queja: "no funciona", "nunca llegó" |
| **Intensificadores** | `muy`, `bastante`, `super`, `súper`, `totalmente`, `sin duda` | Polaridad fuerte |
| **Tiempo** | `día`, `días`, `semana`, `mes`, `meses`, `años` | Quejas por demora/duración |
| **Pronombres/Posesivos** | `me`, `mi`, `nos`, `estamos`, `hija`, `hijo` | "me encanta", "mi dinero" |
| **Artículos indefinidos** | `un`, `una` (en patrones fijos) | "un timo", "una estafa", "una pasada" |

**Nota:** Los patrones "un/una + sustantivo" son léxicos (artículo indefinido), no numéricos. Serán analizados en este paso junto con stopwords personalizadas.

---

## FASE B: Baseline y Experimentación

### Configuraciones TF-IDF a Evaluar

| # | Configuración | Opciones | Notas |
|---|---------------|----------|-------|
| 9 | **ngram_range** | (1,2), (1,3), (1,4) | Bigramas = núcleo semántico; trigramas/4-gramas = contexto |
| 10 | **max_features** | 10K, 20K, 30K | Límite de vocabulario |
| 11 | **min_df** | 2, 3, 5 | Frecuencia mínima de términos |
| 12 | **max_df** | 0.8, 0.9, 0.95 | Frecuencia máxima (filtrar muy comunes) |

---

## Flujo de Trabajo Acordado

```
1. TÚ CONFIRMAS    → Indicar paso a trabajar
       ↓
2. YO PROGRAMO    → Crear celda(s) de análisis en notebook
       ↓
3. TÚ EJECUTAS    → Correr celdas y revisar resultados
       ↓
4. DISCUTIMOS     → Interpretar resultados juntos
       ↓
5. TÚ DECIDES     → Tomar decisión final
       ↓
6. DOCUMENTAMOS   → Registrar decisión y justificación
       ↓
7. SIGUIENTE      → Repetir para el siguiente paso
```

---

## Entregables

| Momento | Entregable |
|---------|------------|
| Fase A completada | Este archivo **.md** actualizado con todas las decisiones |
| Propósito | Guía de implementación para Fase B |

---

## Hallazgos Clave del Análisis Previo

| # | Hallazgo | Fuente |
|---|----------|--------|
| 1 | Errores ortográficos pueden ser discriminativos (no siempre corregir) | Análisis IA sobre n-gramas |
| 2 | **Clase neutra es un RETO** - explorar enfoques para hacerla más discriminativa | Análisis IA + EDA |
| 3 | Campos semánticos claros: Fraude/engaño, Pérdida/dinero, No funcionamiento, Tiempo/espera | Análisis IA sobre n-gramas |
| 4 | TF-IDF con n-gramas captura muy bien la polaridad sin embeddings complejos | Análisis IA sobre n-gramas |

---

## Progreso Actual

### Fase A - Estado de Pasos

| # | Paso | Estado | Decisión | Justificación |
|---|------|--------|----------|---------------|
| 1 | Números | ✅ Completo | Ver sección 3.1 del notebook | Transformar patrones significativos, eliminar resto |
| 2 | Puntuación repetida | ✅ Completo | Ver sección 3.2 del notebook | Tokens granulares |
| 3 | Errores ortográficos | ⏳ Pendiente | — | — |
| 4 | Mayúsculas (count) | ⏳ Pendiente | — | — |
| 5 | Mixed case | ⏳ Pendiente | — | — |
| 6 | Emojis | ⏳ Pendiente | — | — |
| 7 | text_length | ⏳ Pendiente | — | — |
| 8 | Stopwords personalizadas | ⏳ Pendiente | — | — |

### Secciones Adicionales Completadas

| Sección | Estado | Decisión |
|---------|--------|----------|
| 3.3 Caracteres especiales | ✅ Completo | Tokenizar € y %, eliminar resto |

---

## Referencias

- **Notebook EDA:** `EDA_sentiment.ipynb`
- **Análisis n-gramas:** `ngramas_discriminativos_multi_n.csv`
- **Respuesta IA generativa:** `respuesta_iagenerativa.md`

---

## Registro de Decisiones (se actualiza durante el proceso)

### Decisión 1: Números (Sección 3.1)
- **Fecha:** Completado previamente
- **Decisión:** Transformar patrones significativos (duracion, numero_veces, cantidad, porcentaje, ordinal_typo, estrellas, precio_estandar) y eliminar números aislados restantes
- **Justificación:** Chi² significativo en múltiples patrones

### Decisión 2: Puntuación Repetida (Sección 3.2)
- **Fecha:** Completado previamente
- **Decisión:** Crear tokens granulares (`<EXCL_2>`, `<EXCL_3>`, `<EXCL_MULT>`, `<INTER_2>`, `<INTER_3>`, `<INTER_MULT>`, `<PUNTOS_SUSP>`, `<PUNT_MIXTA>`)
- **Justificación:** Patrones discriminativos por intensidad

### Decisión 3: Caracteres Especiales (Sección 3.3)
- **Fecha:** Completado previamente
- **Decisión:** Tokenizar `€` → `<PRECIO>`, `%` → `<PORCENTAJE>`. Eliminar resto.
- **Justificación:** Solo € y % tienen Chi² significativo y dominancia clara

### Decisión 4: Errores Ortográficos
- **Fecha:** Pendiente
- **Decisión:** —
- **Justificación:** —

### Decisión 5: Mayúsculas (count)
- **Fecha:** Pendiente
- **Decisión:** —
- **Justificación:** —

### Decisión 6: Mixed Case
- **Fecha:** Pendiente
- **Decisión:** —
- **Justificación:** —

### Decisión 7: Emojis
- **Fecha:** Pendiente
- **Decisión:** —
- **Justificación:** —

### Decisión 8: text_length
- **Fecha:** Pendiente
- **Decisión:** —
- **Justificación:** —

### Decisión 9: Stopwords Personalizadas
- **Fecha:** Pendiente
- **Decisión:** —
- **Justificación:** —
