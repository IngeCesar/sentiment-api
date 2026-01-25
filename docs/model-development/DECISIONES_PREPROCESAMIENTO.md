# Decisiones de Preprocesamiento - Clasificador de Sentimientos

## Información del Proyecto
- **Dataset:** Amazon Reviews Multi (Español)
- **Objetivo:** Clasificación de sentimientos (3 clases: negativo, neutro, positivo)
- **Modelo:** TF-IDF + Modelo Clásico
- **Fecha de inicio:** 23 de enero de 2026

---

## Resumen de Decisiones

| Sección | Elemento | Decisión | Estado |
|---------|----------|----------|--------|
| 3.1 | Números | Transformar patrones significativos, eliminar resto | ✅ Completado |
| 3.2 | Puntuación repetida | Tokens granulares por intensidad | ✅ Completado |
| 3.3 | Caracteres especiales | Tokenizar € y %, eliminar resto | ✅ Completado |
| 3.4 | Errores ortográficos | **No hacer nada** - Preservar errores | ✅ Completado |
| 3.5 | Mayúsculas (count) | **Normalización selectiva** - Preservar MAYÚSCULAS completas | ✅ Completado |
| 3.6 | Mixed case | **Cubierto por 3.5** - Normalizado a minúsculas | ✅ Completado |
| 3.7 | Emojis | **Eliminar** - Frecuencia muy baja (0.38%) | ✅ Completado |
| 3.8 | text_length | **Probar en experimentación** - Validar con CV | ✅ Completado |
| 3.9 | Stopwords personalizadas | **Lista custom refinada** - 403 eliminadas, 118 preservadas | ✅ Completado |

---

## 3.1. Números

### Análisis Realizado
- **Frecuencia:** 16.34% de reseñas contienen números
- **Chi² global:** 1287.43 (p < 0.001) - Altamente significativo
- **Patrones analizados:** 14 tipos de patrones numéricos
- **Patrones significativos:** 10 de 14

### Decisión Final

| Patrón | Acción | Transformación | Justificación |
|--------|--------|----------------|---------------|
| `duracion` | TRANSFORMAR | "2 días" → "dos días" | Chi² sig. Neg: 5.57% vs Pos: 2.92%. Indica durabilidad corta. |
| `numero_veces` | TRANSFORMAR | "3 veces" → "tres veces" | Chi² sig. Neg: 1.35% vs Pos: 0.34% (4x más en negativo). |
| `cantidad` | TRANSFORMAR | "5 unidades" → "cinco unidades" | Chi² sig. Neg: 0.92% vs Pos: 0.28%. |
| `porcentaje` | TRANSFORMAR | "100%" → "cien por ciento" | Chi² sig. Pos: 1.55% vs Neg: 0.63%. |
| `ordinal_typo` | TRANSFORMAR | "3er" → "tercer" | Chi² sig. Contextos discriminativos. |
| `estrellas` | TRANSFORMAR | "5 estrellas" → "cinco estrellas" | Chi² sig. Preservar expresión natural. |
| `precio_estandar` | NORMALIZAR | "15.99€" o "$29.99" → `<PRECIO>` | Chi² sig. Valor exacto no aporta. Incluye € y $. |
| `duracion_typo` | PRESERVAR | "24h" mantener | Significado semántico propio. |
| `cero_absoluto` | PRESERVAR | "cero problemas" mantener | Expresión idiomática positiva. |
| `rango` | ELIMINAR | "2-3" → "" | NO significativo (p=0.126). |
| `fraccion` | ELIMINAR | "8/10" → "" | NO significativo. |
| `fecha` | ELIMINAR | Eliminar | Muy pocos casos. |
| `precio_typo` | ELIMINAR | "10eur" → "" | Muy pocos casos. |
| `numero_aislado` | ELIMINAR | Eliminar restantes | Sin información discriminativa. |

### Orden de Procesamiento
1. Patrones compuestos (precio, duración, cantidad, etc.)
2. Patrones typos (ordinal_typo, duracion_typo)
3. Números aislados restantes (eliminar)

### Tokens Generados
- `<PRECIO>` (para valores monetarios: número + € o $)

> ⚠️ **Nota sobre orden:** Las transformaciones a texto (duracion, numero_veces, etc.) se aplican ANTES de eliminar números aislados, protegiendo así los números con contexto semántico.

---

## 3.2. Puntuación Repetida

### Análisis Realizado
- **Frecuencia:** ~8% de reseñas contienen puntuación repetida
- **Chi² global:** Significativo (p < 0.001)
- **Hallazgo clave:** La INTENSIDAD de la puntuación correlaciona con sentimiento

### Decisión Final

| Patrón | Token | Justificación |
|--------|-------|---------------|
| `!!` (exactamente 2) | `<EXCL_2>` | Énfasis moderado |
| `!!!` (exactamente 3) | `<EXCL_3>` | Énfasis alto |
| `!!!!+` (4 o más) | `<EXCL_MULT>` | Énfasis extremo |
| `??` (exactamente 2) | `<INTER_2>` | Duda moderada |
| `???` (exactamente 3) | `<INTER_3>` | Duda alta |
| `????+` (4 o más) | `<INTER_MULT>` | Duda extrema |
| `...` (3 puntos) | `<SUSP_3>` | Suspensión estándar |
| `....+` (4+ puntos) | `<SUSP_MULT>` | Suspensión extendida |
| `!?`, `?!`, etc. | `<MIXTO>` | Puntuación mixta |

### Implementación
```python
def tokenizar_puntuacion_repetida(text):
    # Exclamaciones (orden: más largo primero)
    text = re.sub(r'!{4,}', ' <EXCL_MULT> ', text)
    text = re.sub(r'!{3}', ' <EXCL_3> ', text)
    text = re.sub(r'!{2}', ' <EXCL_2> ', text)
    
    # Interrogaciones
    text = re.sub(r'\?{4,}', ' <INTER_MULT> ', text)
    text = re.sub(r'\?{3}', ' <INTER_3> ', text)
    text = re.sub(r'\?{2}', ' <INTER_2> ', text)
    
    # Puntos suspensivos
    text = re.sub(r'\.{4,}', ' <SUSP_MULT> ', text)
    text = re.sub(r'\.{3}', ' <SUSP_3> ', text)
    
    # Mixtos
    text = re.sub(r'[!?¡¿]{2,}', ' <MIXTO> ', text)
    
    return text
```

### Tokens Generados
- `<EXCL_2>`, `<EXCL_3>`, `<EXCL_MULT>`
- `<INTER_2>`, `<INTER_3>`, `<INTER_MULT>`
- `<SUSP_3>`, `<SUSP_MULT>`
- `<MIXTO>`

---

## 3.3. Caracteres Especiales

### Análisis Realizado
- **Caracteres analizados:** 26 tipos
- **Con Chi² significativo:** 7 caracteres (frecuencia ≥ 0.1%)
- **Hallazgo clave:** Solo `€` y `%` tienen valor semántico discriminativo

### Resultados Chi²

| Carácter | χ² | p-value | Dominante | Interpretación |
|----------|-----|---------|-----------|----------------|
| `%` | **456.6** | 7.18e-100 | POSITIVO | Descuentos, ofertas |
| `€` | **191.5** | 2.57e-42 | NEGATIVO | Quejas sobre precio |
| `-` | 240.5 | 5.90e-53 | positivo | Multiuso (ambiguo) |
| `/` | 133.9 | 8.44e-30 | positivo | Multiuso (ambiguo) |
| `( )` | ~80 | <0.001 | neutro | No discrimina |
| `+` | 3.2 | 0.203 | - | NO significativo |

### Decisión Final

| Acción | Caracteres | Razón |
|--------|------------|-------|
| **TOKENIZAR** | `número + €` o `$ + número` → `<PRECIO>` | χ²=191.5, dominante NEGATIVO. Solo cuando acompañan número. |
| **TOKENIZAR** | `número + %` → `<PORCENTAJE>` | χ²=456.6, dominante POSITIVO. Solo cuando acompaña número. |
| **ELIMINAR** | `( ) - / + * = # & _ ^ ~ \ \| [ ] { } @ £ ¥` | Sin valor discriminativo o frecuencia insuficiente |

> ⚠️ **Nota:** Los caracteres `<` y `>` se eliminan en el paso 0 de limpieza de ruido, ANTES de la tokenización, para evitar conflictos con los tokens especiales como `<PRECIO>`.

### Implementación
```python
def tokenizar_caracteres_especiales(texto):
    # número + € o $ + número → <PRECIO>
    texto = re.sub(r'\d+[.,]?\d*\s*€', ' <PRECIO> ', texto)  # 15.99€, 20 €
    texto = re.sub(r'€\s*\d+[.,]?\d*', ' <PRECIO> ', texto)  # €15.99
    texto = re.sub(r'\$\s*\d+[.,]?\d*', ' <PRECIO> ', texto)  # $29.99
    texto = re.sub(r'\d+[.,]?\d*\s*\$', ' <PRECIO> ', texto)  # 29.99$
    
    # número + % → <PORCENTAJE>
    texto = re.sub(r'\d+\s*%', ' <PORCENTAJE> ', texto)  # 50%, 100 %
    
    return texto
```

### Tokens Generados
- `<PRECIO>` (unificado con sección 3.1)
- `<PORCENTAJE>`

---

## 3.4. Errores Ortográficos

### Análisis Realizado

#### Tildes Omitidas
- **Frecuencia global:** 85.17% de reseñas tienen al menos 1 tilde omitida
- **Chi² global:** 1364.21 (p ≈ 0) - Altamente significativo
- **Palabras analizadas:** 39
- **Palabras con Chi² significativo:** 36

| Sentimiento | % Reseñas con tildes omitidas |
|-------------|-------------------------------|
| Negativo | 88.05% |
| Neutro | 86.33% |
| Positivo | 81.70% |

**Diferencia relativa:** 7.77% (Negativos escriben peor)

#### Typos (Errores Ortográficos)
- **Frecuencia global:** 20.33% de reseñas tienen al menos 1 typo
- **Chi² global:** 2315.00 (p ≈ 0) - Altamente significativo
- **Errores analizados:** 37
- **Errores con Chi² significativo:** 23

| Sentimiento | % Reseñas con typos |
|-------------|---------------------|
| Negativo | 25.29% |
| Neutro | 19.57% |
| Positivo | 15.76% |

**Diferencia relativa:** 60.47% (Negativos tienen mucho más typos)

### Hallazgos Clave

1. **Errores de auxiliar "haber"** son marcadores de sentimiento NEGATIVO:
   - `"e recibido"` (en lugar de "he recibido")
   - `"a llegado"` (en lugar de "ha llegado")
   - `"a pasado"`, `"e tenido"`, `"e pedido"`

2. **TF-IDF los diferencia automáticamente:**
   - `"mas"` ≠ `"más"` → son tokens distintos
   - El modelo puede aprender estos patrones sin intervención

3. **85% tiene tildes omitidas:**
   - Es característica del corpus (escritura digital informal)
   - No es ruido, es información del comportamiento del usuario

### Decisión Final

| Aspecto | Decisión |
|---------|----------|
| **Acción** | **OPCIÓN C: NO HACER NADA** |
| **Tildes omitidas** | Preservar tal cual |
| **Typos** | Preservar tal cual |
| **Tokens especiales** | Ninguno |

### Justificación

| Factor | Detalle |
|--------|---------|
| **Correlación estadística** | Chi² significativo. Errores SÍ correlacionan con sentimiento. |
| **Señal discriminativa** | Negativos: 25% typos vs Positivos: 16%. El modelo puede aprovechar esto. |
| **TF-IDF maneja esto** | Tokens diferentes automáticamente. Sin código adicional. |
| **Riesgo de corrección** | Normalizar introduce errores (ej: "caro" ¿typo o "expensive"?). |
| **Principio KISS** | Menos transformaciones = menos complejidad = menos bugs. |

### Implementación

```python
# NO SE REQUIERE CÓDIGO DE PREPROCESAMIENTO
# Los errores ortográficos se preservan tal cual
# TF-IDF los tratará como tokens independientes
```

### Tokens Generados
- Ninguno (no se agregan tokens especiales)

---

## 3.5. Mayúsculas (Normalización Selectiva)

### Análisis Realizado

#### Métricas de Mayúsculas por Sentimiento

| Sentimiento | Media caps_count | % Reseñas con palabras MAYÚSCULAS |
|-------------|------------------|-----------------------------------|
| Negativo | 3.90 | 5.2% |
| Neutro | 3.13 | 3.5% |
| Positivo | 3.02 | 3.2% |

**Diferencia relativa:** Negativos tienen **28.9% más** letras mayúsculas que positivos.

#### Pruebas Chi-cuadrado

| Test | Chi² | p-valor | Cramér's V | Significativo |
|------|------|---------|------------|---------------|
| Presencia palabras MAYÚSCULAS | 457.73 | 4.03e-100 | 0.0473 | ✅ Sí (efecto débil) |
| Intensidad (caps_ratio) | 1,732.50 | ~0 | 0.0650 | ✅ Sí (efecto débil) |

#### Palabras MAYÚSCULAS Discriminativas

| Exclusivas NEGATIVO | Exclusivas POSITIVO |
|---------------------|---------------------|
| ESTAFA (27) | EXCELENTE (19) |
| NADIE (23) | CONTENTA (11) |
| UPS (19) | PERFECTO (42) |
| ROTO (17) | BUENA (105) |
| DESASTRE (10) | BUEN (88) |

**Hallazgo clave:** "NO" en mayúsculas aparece **9x más** en negativos (1,922) que en positivos (213).

### Decisión Final

| Aspecto | Decisión |
|---------|----------|
| **Acción** | **Normalización selectiva de mayúsculas** |
| **Palabras COMPLETAMENTE mayúsculas** | PRESERVAR (ej: "ESTAFA", "NO", "EXCELENTE") |
| **Palabras con inicial mayúscula** | Convertir a minúsculas (ej: "El" → "el") |
| **Palabras mixtas** | Convertir a minúsculas (ej: "iPhone" → "iphone") |
| **Tokens especiales** | Ninguno |

### Implementación

```python
def normalizar_mayusculas(texto):
    """
    Preserva palabras COMPLETAMENTE en mayúsculas (≥2 letras),
    normaliza el resto a minúsculas.
    
    Ejemplos:
        "El PRODUCTO es MUY malo" → "el PRODUCTO es MUY malo"
        "EXCELENTE compra" → "EXCELENTE compra"
        "Amazon vende ESTAFA" → "amazon vende ESTAFA"
    """
    palabras = texto.split()
    resultado = []
    for palabra in palabras:
        if palabra.isupper() and len(palabra) >= 2:
            resultado.append(palabra)  # Preservar MAYÚSCULAS
        else:
            resultado.append(palabra.lower())  # Normalizar a minúsculas
    return ' '.join(resultado)
```

### Justificación

| Factor | Detalle |
|--------|---------|
| **Significancia estadística** | Chi² = 457.73, p ≈ 0 → Las mayúsculas SÍ discriminan |
| **Valor semántico** | "ESTAFA" y "EXCELENTE" son 100% discriminativas (Lift = ∞) |
| **Reducción de ruido** | "El" al inicio de oración ≠ "EL" enfático |
| **Énfasis emocional** | Palabras MAYÚSCULAS representan intensidad del usuario |
| **Efecto acumulativo** | Aunque Cramér's V es débil, suma con otras señales |

### Configuración TF-IDF Requerida

```python
TfidfVectorizer(
    lowercase=False,  # CRÍTICO: No aplicar lowercase automático
    token_pattern=r'(?u)\b\w+\b|<[A-Z_0-9]+>',
    # ... resto de parámetros
)
```

### Nota para Stopwords (Sección 3.9)

⚠️ **Importante:** Considerar NO eliminar stopwords en MAYÚSCULAS.

Ejemplo: "NO" es stopword, pero "NO" en mayúsculas es altamente discriminativo:
- Negativo: 1,922 ocurrencias
- Positivo: 213 ocurrencias

Opciones a evaluar en 3.9:
1. Excluir versiones MAYÚSCULAS de la lista de stopwords
2. Crear lista de stopwords solo en minúsculas

### Tokens Generados
- Ninguno (no se agregan tokens especiales)

---

## 3.6. Mixed Case (Capitalización Atípica)

### Decisión Final

| Aspecto | Decisión |
|---------|----------|
| **Acción** | **CUBIERTO POR SECCIÓN 3.5** |
| **Justificación** | La función `normalizar_mayusculas()` ya maneja mixed_case |

### Análisis Previo (Sección 2.7.2.2)

| Métrica | Valor |
|---------|-------|
| % Reseñas con mixed_case | 2.2% - 2.8% |
| Top palabras | iPhone, iPad, MacBook, WiFi, YouTube |
| Diferencia relativa entre sentimientos | 24.7% |

### ¿Por qué no crear feature separada?

1. **Frecuencia muy baja:** Solo 2-3% de reseñas tienen mixed_case
2. **Ya resuelto:** `normalizar_mayusculas()` convierte "iPhone" → "iphone"
3. **El valor está en la palabra:** "AliExpress" es discriminativo por ser AliExpress, no por su capitalización
4. **Principio KISS:** No añadir complejidad innecesaria

### Comportamiento

```python
# La función normalizar_mayusculas() de 3.5 ya maneja esto:
"iPhone"      → "iphone"       # Mixed case → minúsculas
"MacBook"     → "macbook"      # Mixed case → minúsculas  
"ESTAFA"      → "ESTAFA"       # MAYÚSCULA completa → preservada
```

### Tokens Generados
- Ninguno

---

## 3.7. Emojis

### Análisis Realizado
- **Frecuencia global:** 0.38% de reseñas contienen emojis (782 de 205,000)
- **Chi²:** 12.47 (p = 0.002) - Significativo pero efecto NEGLIGIBLE
- **Cramér's V:** 0.0078 (< 0.1 = efecto negligible)
- **Coherencia emoji-sentimiento:** 95.4% (alta cuando presentes)

### Distribución por Sentimiento

| Sentimiento | Reviews con emoji | Porcentaje |
|-------------|------------------|------------|
| Negativo | 334 | 0.41% |
| Neutro | 117 | 0.29% |
| Positivo | 331 | 0.40% |

**Diferencia absoluta:** 0.12 puntos porcentuales
**Diferencia relativa:** 42.7%

### Hallazgos Clave

1. **Frecuencia muy baja:** Solo 0.38% de reviews contienen emojis
2. **Efecto estadístico negligible:** Cramér's V = 0.0078
3. **Alta coherencia pero irrelevante:** 95.4% coherente, pero aplica solo a 0.38% de datos
4. **Impacto esperado en modelo:** ~0% en accuracy global

### Decisión Final

**ELIMINAR todos los emojis**

| Aspecto | Valor |
|---------|-------|
| Frecuencia | 0.38% (< 1% umbral) |
| Cramér's V | 0.0078 (negligible) |
| Complejidad | Mínima |
| Tokens adicionales | 0 |

### Implementación

```python
import re

# Patrón regex para emojis
EMOJI_PATTERN = re.compile(
    "["
    "\U0001F600-\U0001F64F"  # emoticons
    "\U0001F300-\U0001F5FF"  # symbols & pictographs
    "\U0001F680-\U0001F6FF"  # transport & map
    "\U0001F900-\U0001F9FF"  # supplemental
    "\U00002702-\U000027B0"  # dingbats
    "]+",
    flags=re.UNICODE
)

def eliminar_emojis(texto):
    """Elimina todos los emojis del texto"""
    if pd.isna(texto):
        return texto
    return EMOJI_PATTERN.sub('', str(texto))
```

### Justificación

1. **Frecuencia insuficiente:** 0.38% << 1% (umbral para tokenización)
2. **Principio KISS:** Solución más simple cuando impacto es marginal
3. **Efecto negligible:** Cramér's V confirma que emojis no discriminan
4. **Sin penalización:** No añade complejidad ni tokens extra

### Tokens Generados
- Ninguno

---

## 3.8. text_length (Longitud del texto)

### Análisis Realizado

#### Distribución por sentimiento (palabras)

| Sentimiento | N | Media | Mediana | Std | P95 |
|-------------|---------|-------|---------|-----|-----|
| Negativo | 82,000 | 29.87 | 23 | 24.38 | 75 |
| Neutro | 41,000 | 28.81 | 23 | 24.60 | 74 |
| Positivo | 82,000 | 25.21 | 20 | 23.45 | 67 |

**Diferencia relativa:** 18.5% (negativo vs positivo)

#### Resultados Chi² (longitud categorizada en cuartiles)

| Métrica | Valor |
|---------|-------|
| Chi² | 3,305.38 |
| p-value | ≈ 0 (significativo) |
| DoF | 6 |
| Cramér's V | **0.0898** |
| Tamaño del efecto | Negligible/Pequeño (límite) |

#### Proporciones por categoría de longitud

| Categoría | % Negativo | % Neutro | % Positivo |
|-----------|------------|----------|------------|
| Corto (Q1) | 32.5% | 18.5% | 49.1% |
| Largo (Q4) | 45.4% | 21.2% | 33.4% |

**Observación:** Reviews largas tienen +13pp de negativos vs cortas

### Consideraciones Técnicas

1. **TF-IDF con `norm='l2'` NO captura longitud absoluta**
   - La normalización L2 anula el efecto de longitud
   - text_length aportaría información adicional

2. **Correlación palabras ↔ caracteres:** 0.994
   - Solo necesitamos UNA métrica de longitud

3. **Comparación con otros features:**
   - text_length V = 0.0898 > emojis V = 0.0078

### Decisión Final

**PROBAR EN EXPERIMENTACIÓN (Opción C)**

| Aspecto | Detalle |
|---------|--------|
| Acción | Preparar feature `review_length` pero validar utilidad en fase de modelado |
| Validación | Cross-validation comparando modelo con y sin text_length |
| Métrica objetivo | ΔF1-Macro |
| Umbral de inclusión | Incluir si ΔF1-Macro ≥ 0.5% |

### Justificación

1. **Efecto en el límite:** Cramér's V = 0.09 está entre "negligible" y "pequeño"
2. **No descartar prematuramente:** El feature tiene costo mínimo (1 valor extra)
3. **Validación empírica:** Solo la experimentación confirmará si aporta al modelo
4. **Principio pragmático:** Preparar pero validar antes de comprometerse

### Implementación Propuesta

```python
# En fase de preprocesamiento: calcular feature
df['text_length'] = df['review_body'].apply(lambda x: len(str(x).split()))

# En fase de modelado: probar ambas configuraciones
# Config A: TF-IDF solo
# Config B: TF-IDF + text_length (normalizado con StandardScaler)

# Validar con cross-validation
from sklearn.preprocessing import StandardScaler
from scipy.sparse import hstack

# Si text_length aporta ≥ 0.5% en F1-Macro → incluir
# Si no → descartar
```

### Tokens Generados
- Ninguno (es un feature numérico, no un token)

---

## Orden de Preprocesamiento (Pipeline)

El orden es **crítico** para evitar conflictos:

```
0. LIMPIEZA DE RUIDO (ejecutar primero)
   ├── Eliminar URLs
   ├── Eliminar correos electrónicos
   ├── Eliminar menciones (@usuario)
   ├── Eliminar hashtags (#tema)
   └── Eliminar caracteres < > del texto original

1. Eliminar emojis ✅
2. Tokenizar caracteres especiales (€ → <PRECIO>, % → <PORCENTAJE>) ✅
3. Tokenizar patrones numéricos (duración, cantidad, etc.) ✅
4. Tokenizar puntuación repetida (!! → <EXCL_2>, etc.) ✅
5. Eliminar caracteres especiales restantes ✅
6. Normalización selectiva de mayúsculas (preservar MAYÚSCULAS, resto → minúsculas) ✅
```

> ⚠️ **IMPORTANTE sobre Stopwords:** Las stopwords NO se eliminan en el preprocesamiento.
> Se configuran como parámetro `stop_words` en TF-IDF:
> - `stop_words = list(STOPWORDS_ELIMINAR)` → 403 palabras que TF-IDF IGNORA
> - Las 118 palabras preservadas NO están en esa lista → TF-IDF las INCLUYE automáticamente

---

## Token Pattern para TF-IDF

Para capturar tokens especiales, usar:

```python
token_pattern = r'(?u)\b\w+\b|<[A-Z_0-9]+>'
```

---

## Inventario de Tokens Especiales

| Categoría | Tokens |
|-----------|--------|
| **Numéricos** | `<PRECIO>` |
| **Puntuación** | `<EXCL_2>`, `<EXCL_3>`, `<EXCL_MULT>`, `<INTER_2>`, `<INTER_3>`, `<INTER_MULT>`, `<SUSP_3>`, `<SUSP_MULT>`, `<MIXTO>` |
| **Caracteres** | `<PORCENTAJE>` |

**Total:** 11 tokens especiales definidos

---

## Historial de Actualizaciones

| Fecha | Sección | Cambio |
|-------|---------|--------|
| 2026-01-23 | 3.1-3.3 | Documento inicial con decisiones |
| 2026-01-23 | 3.4 | Decisión: No hacer nada con errores ortográficos |
| 2026-01-23 | 3.5 | Decisión: Normalización selectiva de mayúsculas |
| 2026-01-23 | 3.6 | Decisión: Cubierto por 3.5 (KISS) |
| 2026-01-23 | 3.7 | Decisión: Eliminar emojis (frecuencia 0.38% < 1%) |
| 2026-01-23 | 3.8 | Decisión: Probar en experimentación (Cramér's V = 0.09) |
| 2026-01-23 | 3.9 | Decisión: Lista custom refinada (403 eliminadas, 118 preservadas) |
| 2026-01-23 | Pipeline | Añadido paso 0 (limpieza de ruido), corregido orden, aclarado stopwords en TF-IDF |

---

## 3.9. Stopwords Personalizadas

### Análisis Realizado

#### Metodología en 4 Fases

**FASE 1: Análisis Chi² de 521 stopwords (spaCy español)**

| Métrica | Valor |
|---------|-------|
| Stopwords analizadas | 521 |
| Con Chi² significativo | 256 (49.1%) |
| Cramér's V máximo | 0.0614 ("no") |
| Cramér's V medio | 0.0072 |
| Umbral V ≥ 0.02 | 9 stopwords |

**FASE 2: Cruce con N-gramas Discriminativos**

Se cruzaron las stopwords con el archivo `ngramas_discriminativos_multi_n.csv` (7,223 n-gramas):

| Stopword | Apariciones en N-gramas | V Cramér |
|----------|-------------------------|----------|
| no | 1,452 | 0.0614 |
| muy | 454 | 0.0459 |
| ni | 273 | 0.0193 |
| para | 208 | 0.0031 |
| y | 186 | 0.0048 |
| me | 176 | 0.0068 |
| pero | 170 | 0.0282 |
| se | 122 | 0.0049 |
| con | 115 | 0.0025 |
| de | 115 | 0.0013 |

**FASE 3: Matriz de Clasificación Inicial**

| Categoría | Criterio | Cantidad |
|-----------|----------|----------|
| 🟢 PRESERVAR | Chi² sig. + En n-gramas | 116 |
| 🟡 EVALUAR | Chi² sig. + NO en n-gramas | 264 |
| 🔴 ELIMINAR | Chi² NO sig. | 141 |

**FASE 3.5: Análisis V por Sentimiento Individual (Idea del usuario)**

Para las 264 stopwords 🟡 EVALUAR, se calculó Cramér's V **binario** para cada sentimiento:
- V_negativo: palabra vs NO-negativo
- V_neutro: palabra vs NO-neutro  
- V_positivo: palabra vs NO-positivo

Resultados:

| Subcategoría | Criterio | Cantidad |
|--------------|----------|----------|
| 🟢 MANTENER EN EVALUAR | V_max ≥ 0.02 | **0** |
| 🟠 BAJO IMPACTO | 0.01 ≤ V_max < 0.02 | **2** |
| 🔴 MOVER A ELIMINAR | V_max < 0.01 | **262** |

**Hallazgo crítico:** 0 stopwords discriminan la clase NEUTRO (V_neutro ≥ 0.01 = 0)

Stopwords de bajo impacto (preservadas por cautela):
- `aunque` (V_neg=0.0115, V_pos=0.0112)
- `demasiado` (V_neg=0.0108, V_pos=0.0107)

### Clasificación Final Refinada

| Categoría | Cantidad | Porcentaje |
|-----------|----------|------------|
| 🟢 PRESERVAR | **118** | 22.6% |
| 🔴 ELIMINAR | **403** | 77.4% |

### Decisión Final

**OPCIÓN A REFINADA: Lista Custom con análisis Chi² + N-gramas + V por Sentimiento**

| Aspecto | Detalle |
|---------|---------|
| Stopwords a ELIMINAR | 403 (77.4%) |
| Stopwords a PRESERVAR | 118 (22.6%) |
| N-gramas afectados | ~740 (10.2%) |
| Método | Chi² + cruce n-gramas + V binario por sentimiento |

### Stopwords a PRESERVAR (118)

**🟢 Con Chi² significativo + presentes en N-gramas discriminativos (116):**

```
a, ahora, al, alguna, antes, aun, aún, bastante, bien, buen, buena,
bueno, buenos, cinco, como, con, cual, cualquier, da, de, dejó, del,
desde, después, dice, dos, día, días, el, era, es, esta, estaba, estado,
estamos, este, estoy, está, fue, gran, ha, haber, había, hace, han, hay,
he, hoy, la, las, le, llegó, llevar, lo, los, mal, manera, mas, me,
medio, mejor, menos, mi, mis, mismo, mucho, muy, más, nada, nadie, ni,
ninguna, no, nunca, para, parece, pasado, peor, pero, poco, pocos,
poner, por, porque, primer, puede, puedo, que, se, segundo, si, sido,
sin, solo, son, su, tal, tengo, tenido, tenía, tiene, toda, todas,
todavía, todo, tres, un, una, usar, va, veces, ver, voy, y, ya, yo
```

**🟠 Bajo impacto pero conservadas por cautela (2):**

```
aunque, demasiado
```

### Stopwords a ELIMINAR (403)

Todas las stopwords de spaCy español que:
1. NO tienen Chi² significativo (141 originales), O
2. Tienen Chi² sig. pero NO están en n-gramas Y V_max < 0.01 (262 adicionales)

### Implementación

```python
# Lista de stopwords a PRESERVAR (no eliminar del texto)
# Total: 118 stopwords (116 por Chi²+N-gramas + 2 por cautela)
# Fuente: EDA_sentiment.ipynb - Sección 3.9 (STOPWORDS_PRESERVAR_FINAL)
STOPWORDS_PRESERVAR = {
    'a', 'ahora', 'al', 'alguna', 'antes', 'aun', 'aunque', 'aún',
    'bastante', 'bien', 'buen', 'buena', 'bueno', 'buenos', 'cinco',
    'como', 'con', 'cual', 'cualquier', 'da', 'de', 'dejó', 'del',
    'demasiado', 'desde', 'después', 'dice', 'dos', 'día', 'días',
    'el', 'era', 'es', 'esta', 'estaba', 'estado', 'estamos', 'este',
    'estoy', 'está', 'fue', 'gran', 'ha', 'haber', 'había', 'hace',
    'han', 'hay', 'he', 'hoy', 'la', 'las', 'le', 'llegó', 'llevar',
    'lo', 'los', 'mal', 'manera', 'mas', 'me', 'medio', 'mejor',
    'menos', 'mi', 'mis', 'mismo', 'mucho', 'muy', 'más', 'nada',
    'nadie', 'ni', 'ninguna', 'no', 'nunca', 'para', 'parece',
    'pasado', 'peor', 'pero', 'poco', 'pocos', 'poner', 'por',
    'porque', 'primer', 'puede', 'puedo', 'que', 'se', 'segundo',
    'si', 'sido', 'sin', 'solo', 'son', 'su', 'tal', 'tengo',
    'tenido', 'tenía', 'tiene', 'toda', 'todas', 'todavía', 'todo',
    'tres', 'un', 'una', 'usar', 'va', 'veces', 'ver', 'voy', 'y',
    'ya', 'yo'
}

# Lista de stopwords a ELIMINAR (usar con TfidfVectorizer)
import spacy
nlp = spacy.blank('es')
STOPWORDS_SPACY = nlp.Defaults.stop_words

STOPWORDS_ELIMINAR = STOPWORDS_SPACY - STOPWORDS_PRESERVAR
# Total: 403 stopwords
```

### Justificación

| Factor | Detalle |
|--------|---------|
| **Análisis estadístico** | Chi² + Cramér's V identificaron 256 stopwords significativas |
| **Cruce con n-gramas** | 116 stopwords aparecen en n-gramas discriminativos |
| **V por sentimiento** | 262 de 264 dudosas tienen V_max < 0.01 |
| **Neutro no discriminado** | 0 stopwords tienen V_neutro ≥ 0.01 |
| **Conservador** | Se preservan 2 de bajo impacto ("aunque", "demasiado") |
| **Balance** | Elimina 77% de stopwords preservando información discriminativa |

### Comparación de Opciones

| Opción | Eliminadas | Preservadas | N-gramas Perdidos |
|--------|------------|-------------|-------------------|
| A Original | 141 (27%) | 380 | ~20 (0.3%) |
| **A Refinada** ✅ | 403 (77%) | 118 | ~740 (10.2%) |
| B (Sin stopwords) | 0 | 521 | 0 |

### Tokens Generados
- Ninguno (se eliminan tokens, no se crean nuevos)
