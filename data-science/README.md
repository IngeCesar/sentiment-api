# 🧠 CesiumFlow: Inference Engine

> **Motor de procesamiento NLP descentralizado y servicio de inferencia de alta performance.**

![Python](https://img.shields.io/badge/Python-3.14.2-blue?logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.127.x-009688?logo=fastapi&logoColor=white)
![Scikit-Learn](https://img.shields.io/badge/ML-Scikit--Learn-orange?logo=scikit-learn&logoColor=white)
![Status](https://img.shields.io/badge/Status-Development-yellow)

## 🎯 Alcance del Servicio

Este microservicio encapsula la lógica de **Inteligencia Artificial** del ecosistema CesiumFlow. Su responsabilidad única es recibir texto crudo, preprocesarlo y ejecutar inferencias sobre modelos serializados (`.joblib`), devolviendo predicciones probabilísticas al **Core-Service**.

### 🏗️ Stack Técnico (ML Ops)

Selección tecnológica orientada a la **reproducibilidad** y baja latencia de inferencia.

| Componente        | Tecnología    | Rol Arquitectónico                                      |
| :---------------- | :------------ | :------------------------------------------------------ |
| **Runtime**       | Python 3.14.2 | Entorno de ejecución optimizado con GIL improvements.   |
| **API Framework** | FastAPI       | Exposición de endpoints asíncronos de alto rendimiento. |
| **ASGI Server**   | Uvicorn       | Servidor de aplicaciones web ligero y rápido.           |
| **ML Core**       | Scikit-learn  | Algoritmos de clasificación y vectorización (TF-IDF).   |
| **Serialization** | Joblib        | Persistencia eficiente de arrays de NumPy.              |
| **Data Standard** | Pydantic v2   | Validación estricta de esquemas de entrada/salida.      |

---

## 📂 Estructura del Módulo

El repositorio separa explícitamente el entorno de experimentación (**Laboratorio**) del código productivo (**Inferencia**).

```text
data-science/
├── data/               # 💾 Almacén Local (Ignorado por Git/Docker)
│   ├── raw/            # Fuente de verdad inmutable (CSVs originales)
│   └── processed/      # Datasets limpios listos para entrenamiento
├── notebooks/          # 🔬 Laboratorio (Jupyter Notebooks de exploración)
├── models/             # 🧠 Artefactos binarios (Modelos serializados .joblib)
├── src/                # 🧪 Lógica pura de ML (Preprocesamiento y Training)
├── ds-api/             # 🔌 Capa de transporte (Endpoints FastAPI y DTOs)
│   └── main.py         # Punto de entrada de la aplicación
├── requirements.txt    # 🔒 Dependencias congeladas (Pinned Versions)
└── Dockerfile          # 🐳 Definición de infraestructura inmutable
```

---

## ⚙️ Configuración de Entorno Local

Aunque recomendamos usar **Docker** (vía `just dev` en la raíz), para desarrollo de modelos o depuración profunda, puedes ejecutar el servicio nativamente.

### 1. Preparación del Entorno (Python 3.14)

Recomendamos aislar las dependencias para evitar conflictos con el sistema (Cross-contamination).

```bash
# Crear entorno virtual dentro del módulo
python -m venv venv

# Activar (Windows Git Bash / PowerShell)
source venv/Scripts/activate

# Activar (Linux / Mac)
source venv/bin/activate
```

### 2. Hidratación de Dependencias

> [!IMPORTANT]
> **Integridad del Modelo:** Es crítico instalar las versiones exactas definidas en `requirements.txt`. Versiones diferentes de `scikit-learn` pueden causar errores de deserialización (`InconsistentVersionWarning`).

```bash
pip install -r requirements.txt
```

### 3. Ejecución del Servidor (Hot Reload)

```bash
# Ejecutar desde la raíz de la carpeta 'data-science'
uvicorn ds-api.main:app --reload --port 5000
```

---

## 🔌 Contrato de Interfaz (Internal API)

Este servicio está diseñado para ser consumido internamente por el **Core-Service** dentro de la red Docker, no por el usuario final.

| Método | Endpoint     | Descripción                        | Payload Ejemplo                  |
| :----- | :----------- | :--------------------------------- | :------------------------------- |
| `POST` | **/predict** | Realiza inferencia de sentimiento. | `{"text": "Excelente servicio"}` |
| `GET`  | **/health**  | Verificación de estado (K8s).      | N/A                              |

### 🧪 Verificación Rápida (Smoke Test)

Una vez levantado el servicio, puedes validar la carga del modelo visitando la documentación interactiva:

- **Swagger UI:** [http://localhost:5000/docs](http://localhost:5000/docs)
- **Redoc:** [http://localhost:5000/redoc](http://localhost:5000/redoc)

---

## ❓ Solución de Problemas (Troubleshooting)

### ❌ Error: "ModuleNotFoundError"

- **Diagnóstico:** Intentaste ejecutar el script sin activar el entorno virtual o sin estar en la carpeta raíz correcta.
- **Solución:** Asegúrate de ver `(venv)` en tu terminal y ejecutar `uvicorn` desde la carpeta `data-science`, no desde `ds-api`.

### ❌ Error: "ValueError: node array from the pickle has an incompatible dtype"

- **Diagnóstico:** Incompatibilidad de versiones de Scikit-learn.
- **Solución:** El modelo fue entrenado con una versión distinta a la que tienes instalada. Ejecuta `pip install -r requirements.txt --force-reinstall`.

### ❌ Error: "Method Not Allowed" (405) en `/predict`

- **Diagnóstico:** Intentaste acceder vía navegador (GET).
- **Solución:** Este endpoint es estrictamente **POST**. Usa Swagger UI o `curl`.
