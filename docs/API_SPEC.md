# 📜 Especificación de Contrato API - CesiumFlow (Squad 55)

Esta especificación define el estándar de comunicación técnica entre los componentes del ecosistema **CesiumFlow**, asegurando la paridad entre el Middleware (Java) y el Motor de Inferencia (Python).

| Metadato           | Detalle                                               |
| :----------------- | :---------------------------------------------------- |
| **Arquitectura**   | Middleware Reactivo (Core-Service ↔ Sentiment-Engine) |
| **Orquestación**   | Docker Compose (Health-Check Aware)                   |
| **Puerto Público** | `8080` (Java Core)                                    |
| **Puerto Privado** | `5000` (Python Engine)                                |
| **Persistencia**   | PostgreSQL (R2DBC)                                    |

---

## 🧠 1. Flujo de Arquitectura y Valor

El sistema opera bajo el patrón de **Pasarela Segura (Gateway)**. El Core-Service orquestra la validación, la llamada asíncrona a la IA y la persistencia en base de datos.

### Puertos y Visibilidad:

-   **Core-Service (8080):** Único punto de entrada público. Gestiona seguridad y Swagger.
-   **Sentiment-Engine (5000):** Red interna de Docker. Dedicado exclusivamente a inferencia NLP.
-   **Postgres (5432):** Almacenamiento de auditoría (Append-only).

---

## 🚀 2. Contrato de API Pública (REST)

### Recurso: Análisis de Sentimiento

-   **Endpoint:** `POST /api/v1/sentiment`
-   **Content-Type:** `application/json`

#### 📥 Estructura de Entrada (`SentimentRequest`)

Validado mediante **Jakarta Bean Validation** en el Middleware.

| Campo  | Tipo     | Req. | Validación                  | Descripción                   |
| :----- | :------- | :--- | :-------------------------- | :---------------------------- |
| `text` | `String` | Sí   | `@NotBlank`, `3-5000 chars` | Texto original para procesar. |

**Ejemplo de Petición:**

```json
{
    "text": "El equipo del Squad 55 ha logrado una integración excepcional."
}
```

### 📤 Estructura de Respuesta (`SentimentResponse`)

Payload enriquecido con metadatos de persistencia y trazabilidad.

| Campo             | Tipo     | Origen        | Descripción                                                                                                                                              |
| :---------------- | :------- | :------------ | :------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`id`**          | `UUID`   | **DB**        | Identificador único generado nativamente en Postgres (**Null** si hay error de conexión).                                                                |
| **`prediction`**  | `String` | **IA**        | Etiqueta del modelo: `Positivo`, `Negativo`, `Neutro`.                                                                                                   |
| **`probability`** | `Double` | **IA**        | Confianza del modelo (0.00 a 1.00).                                                                                                                      |
| **`keywords`**    | `List`   | **IA**        | Palabras clave relevantes extraídas.                                                                                                                     |
| **`timestamp`**   | `String` | **Core (DB)** | Fecha ISO-8601 UTC (`yyyy-MM-ddTHH:mm:ssZ`). **Nota:** Se ignora el tiempo del motor de IA para garantizar consistencia cronológica (`Source of Truth`). |

**Ejemplo de Respuesta (JSON):**

```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "prediction": "Positivo",
    "probability": 0.98,
    "keywords": ["excelente", "rápido", "servicio"],
    "timestamp": "2026-01-05T15:30:00Z"
}
```

## 🔌 3. Comunicación Interna (Core ↔ Engine)

Esta interfaz es **privada** y exclusiva para la comunicación entre contenedores dentro de la red de Docker. El usuario final no tiene acceso directo a estos recursos.

### Recurso: Inferencia NLP

-   **Host Interno:** `http://sentiment-engine:5000`
-   **Endpoint:** `/predict`
-   **Método:** `POST`

#### 📥 Payload Interno (Java a Python)

El Core-Service transforma la petición pública al formato estricto que espera el motor `FastAPI`.

| Campo                | Tipo      | Valor por defecto | Descripción                               |
| :------------------- | :-------- | :---------------- | :---------------------------------------- |
| **`text`**           | `String`  | (Obligatorio)     | Texto sanitizado proveniente del usuario. |
| **`top_n_keywords`** | `Integer` | `5`               | Cantidad de palabras clave a extraer.     |

**Ejemplo de JSON Interno:**

```json
{
    "text": "El servicio fue excelente.",
    "top_n_keywords": 5
}
```

---

## 🩺 4. Protocolos de Salud (Health Checks)

Endpoints utilizados por **Docker Compose** para la orquestación y auto-recuperación (Self-Healing).

| Componente           | Endpoint           | Tipo            | Propósito                                          |
| :------------------- | :----------------- | :-------------- | :------------------------------------------------- |
| **Core-Service**     | `/actuator/health` | Spring Actuator | Monitor de estado JVM, Disco y Conexión R2DBC.     |
| **Sentiment-Engine** | `/health`          | Custom Endpoint | Verifica carga de modelos (`.joblib`) y librerías. |
| **PostgreSQL**       | `pg_isready`       | CLI Command     | Disponibilidad del socket de base de datos.        |

## ⚠️ 5. Gestión de Errores y Resiliencia

El sistema implementa un manejo centralizado de excepciones (**Global Exception Handling**) para garantizar respuestas JSON consistentes y seguras, evitando exponer "Stack Traces" de Java al cliente final.

### Tabla de Códigos de Estado

| Código HTTP | Estado           | Causa Probable                              | Acción del Cliente                                  |
| :---------- | :--------------- | :------------------------------------------ | :-------------------------------------------------- |
| **200 OK**  | `Success`        | Operación exitosa o **Fallback de IA**.     | Consumir el JSON y verificar el campo `prediction`. |
| **400**     | `Bad Request`    | Violación de reglas (`@NotBlank`, `@Size`). | Corregir la longitud del texto y reintentar.        |
| **500**     | `Internal Error` | Fallo de conexión con DB o error crítico.   | Reportar al equipo de SRE.                          |

### 🛡️ Estrategia de Fallback (Circuit Breaker Pattern)

Si el **Sentiment-Engine** (Python) no responde (Timeout o contenedor `DOWN`), el **Core-Service** no devuelve un error 500. En su lugar, captura la excepción y retorna una **respuesta degradada controlada**.

**Payload de Contingencia (Ejemplo Real):**

```json
{
    "id": null,
    "prediction": "CONNECTION_ERROR",
    "probability": 0.0,
    "keywords": [],
    "timestamp": null
}
```

> **Justificación Técnica:** Este diseño prioriza la disponibilidad del sistema. Permite que el Frontend detecte el estado `CONNECTION_ERROR` y sepa que la solicitud **no fue persistida** (`id: null` y `timestamp: null`), mostrando un mensaje informativo al usuario sin romper la interfaz.

## 📚 6. Exploración y Pruebas (Developer Experience)

CesiumFlow prioriza la **Documentación Viva**. Recomendamos utilizar las interfaces gráficas generadas automáticamente para explorar y probar los contratos de manera interactiva.

### A. Interfaces Gráficas (Swagger UI)

Interactúe visualmente con los endpoints, esquemas y validaciones sin escribir código.

| Componente             | URL de Acceso                           | Descripción                                                                |
| :--------------------- | :-------------------------------------- | :------------------------------------------------------------------------- |
| **Core API (Pública)** | `http://localhost:8080/swagger-ui.html` | Interfaz principal para consumidores. Incluye esquemas DTO y "Try it out". |
| **Sentiment Engine**   | `http://localhost:5000/docs`            | (Solo Dev) Interfaz nativa de FastAPI para depuración aislada del modelo.  |

### B. Acceso por Terminal (Quick CLI)

Para verificaciones rápidas de conectividad o integración en scripts de automatización, puede utilizar el estándar `curl`.

```bash
# Test de Integración (Happy Path)
curl -X POST "http://localhost:8080/api/v1/sentiment" \
     -H "Content-Type: application/json" \
     -d '{"text": "La documentación viva facilita enormemente la integración."}'
```

---

## 📅 Historial de Versiones (Changelog)

Registro de la evolución arquitectónica del producto.

| Versión   | Autor    | Cambios Relevantes                                                                      |
| :-------- | :------- | :-------------------------------------------------------------------------------------- |
| **1.0.0** | Squad 55 | Definición inicial de arquitectura monolítica y scripts básicos.                        |
| **1.1.0** | Squad 55 | Desacoplamiento de servicios: Separación en contenedores Docker (Java/Python).          |
| **1.2.0** | Squad 55 | Migración a Stack Reactivo (WebFlux) e implementación de UUIDs nativos.                 |
| **1.2.1** | Squad 55 | **Versión Actual:** Inclusión de Health Checks, Resiliencia y Manejo de Errores Global. |
