# 🧠 API de Análisis de Sentimientos (Team 55)

Microservicio basado en Python (FastAPI) y Scikit-Learn para analizar el sentimiento de reseñas de productos.

## 📋 Pre-requisitos

- **Python 3.14.2** instalado.
- El puerto **5000** debe estar libre.

## ⚙️ Instalación y Configuración

El artefacto del modelo ya está incluido en el repositorio (`models/`) y las dependencias están congeladas.

1.  **Navegar a la carpeta del proyecto:**

    ```bash
    cd data-science
    ```

2.  **Configurar el Entorno Virtual:**
    Recomendamos crearlo dentro de la carpeta `ds-api`.

    ```bash
    # Crear
    python -m venv ds-api/venv

    # Activar (Windows)
    .\ds-api\venv\Scripts\activate

    # Activar (Mac/Linux)
    source ds-api/venv/bin/activate
    ```

3.  **Instalar Dependencias (CRÍTICO):**
    Usa este comando para asegurar compatibilidad con el modelo pre-entrenado.
    ```bash
    pip install -r requirements.txt
    ```

## 🚀 Ejecución

1.  Entra a la carpeta del código fuente:
    ```bash
    cd ds-api
    ```
2.  Levanta el servidor:
    ```bash
    python main.py
    ```

Verás el mensaje: `✅ Uvicorn running on http://0.0.0.0:5000`

---

## 🧪 Guía de Pruebas (Testing)

Tienes dos formas de probar que la API funciona correctamente.

### Opción A: Swagger UI (Visual y Rápido) ⚡

FastAPI genera documentación interactiva automática.

1.  Abre tu navegador y entra a: **http://localhost:5000/docs**
2.  Verás una barra verde que dice `POST /predict`. Haz clic en ella.
3.  Haz clic en el botón **"Try it out"** (arriba a la derecha).
4.  En el campo "Request body", escribe tu prueba:
    ```json
    {
        "text": "La calidad de producto es terrible"
    }
    ```
5.  Haz clic en el botón azul **"Execute"**.
6.  Verás la respuesta en "Server response".

### Opción B: Postman 🛠️

Sigue esta configuración exacta para evitar errores:

1.  **Method:** Selecciona `POST`.
2.  **URL:** `http://localhost:5000/predict`
3.  **Body:**
    - Ve a la pestaña **Body**.
    - Selecciona la opción **raw**.
    - En el desplegable que dice "Text", cámbialo a **JSON**.
4.  **Payload:** Pega el JSON de prueba:
    ```json
    {
        "text": "Estoy fascinado con la compra, funciona excelente"
    }
    ```
5.  Dale a **Send**. Deberías recibir un `Status: 200 OK`.

---

## ❓ Solución de Problemas Frecuentes

### ❌ Error: "Method Not Allowed" (405)

- **Causa:** Intentaste abrir `http://localhost:5000/predict` directamente en la barra de direcciones del navegador.
- **Solución:** Los navegadores envían peticiones `GET` por defecto, pero este endpoint solo acepta `POST`. Usa Swagger o Postman como se indica arriba.

### ❌ Error: "ModuleNotFoundError"

- **Causa:** No activaste el entorno virtual (`venv`) antes de ejecutar `python main.py`.

### ❌ Error: "InconsistentVersionWarning"

- **Causa:** No instalaste las dependencias usando `requirements.txt` y tienes versiones de `scikit-learn` diferentes a las del modelo. Ejecuta `pip install -r requirements.txt`.
