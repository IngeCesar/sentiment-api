# 🎭 SentimentAPI

> **Análisis de Sentimientos para Atención al Cliente impulsado por IA.**

![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow)
![Java](https://img.shields.io/badge/Backend-Java%20Spring-red)
![Python](https://img.shields.io/badge/Data%20Science-Python-blue)

## 📖 Descripción

**SentimentAPI** es una solución diseñada para procesar feedback de clientes (reseñas, comentarios) y clasificarlos automáticamente mediante modelos de Inteligencia Artificial. Permite a las empresas detectar crisis de reputación y entender la voz del cliente en tiempo real.

### 🚀 Propuesta de Valor

- **Clasificación Automática:** Detecta si un comentario es Positivo, Neutro o Negativo.
- **Arquitectura Híbrida:** Potencia de Java en el Backend + Flexibilidad de Python en IA.
- **Escalable:** Diseño listo para integrarse con bases de datos robustas (PostgreSQL).

---

## 🛠️ Tecnologías

### Backend (API & Lógica)

- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3
- **Gestor de Paquetes:** Maven
- **Base de Datos:** H2 (Dev) / PostgreSQL (Prod)

### Data Science (Modelo IA)

- **Lenguaje:** Python 3.9+
- **Librerías:** Pandas, NumPy, Scikit-learn
- **Modelo:** Regresión Logística / TF-IDF (Baseline)

---

## 👥 Equipo de Desarrollo (Squad 55)

| Rol                | Miembro               | Stack     |
| :----------------- | :-------------------- | :-------- |
| **Data Scientist** | Sofia Elizabeth López | 🐍 Python |
| **Data Scientist** | Harrison Tutalcha     | 🐍 Python |
| **Data Scientist** | Arnold Vásquez        | 🐍 Python |
| **Data Scientist** | Raquel Araniva        | 🐍 Python |
| **Data Scientist** | Jose de Jesus Ruesga  | 🐍 Python |
| **Backend Dev**    | Oscar Orlando Florez  | ☕ Java   |
| **Backend Dev**    | Cesar Omar Ordoñez    | ☕ Java   |
| **Backend Dev**    | Bryan Hernández       | ☕ Java   |
| **Backend Dev**    | Agustin Reyes         | ☕ Java   |
| **Backend Dev**    | Eluizer de Leon       | ☕ Java   |

---

## 📂 Estructura del Proyecto

- `backend/` → Código fuente de Spring Boot.
- `data-science/` → Notebooks y scripts de entrenamiento Python.
- `frontend/` → (Opcional) Cliente web ligero.

---

## 🚀 Configuración del Entorno (Setup)

Para garantizar la estabilidad del proyecto, requerimos el uso de **Java 17** y **Python 3.14**. Recomendamos usar [**mise-en-place**](https://mise.jdx.dev/) para gestionar estas versiones automáticamente, aunque puedes hacerlo de forma manual.

### Guía de Inicio Rápido

```bash
# 1. Instalar mise (Omitir si ya lo tienes o prefieres instalación manual)
# Guía oficial de instalación: https://mise.jdx.dev/getting-started.html

# 2. Clonar el proyecto y entrar al directorio
git clone https://github.com/arnoldevs/sentiment-api.git
cd sentiment-api

# 3. Cambiar a la rama de desarrollo
git checkout develop

# 4. Configurar el entorno con mise (Solo si instalaste mise en el paso 1)
mise trust       # Autoriza la configuración local del proyecto
mise install     # Instala Java 17 y Python 3.14 automáticamente

# 5. Verificación de versiones
java -version    # Debe mostrar 17.x
python --version  # Debe mostrar 3.14.x
```
