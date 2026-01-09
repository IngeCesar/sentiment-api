# 🌊 CesiumFlow: SentimentAPI

> **Orquestador reactivo de análisis de sentimientos e inteligencia de feedback.**

![Release](https://img.shields.io/badge/Release-v1.0.0--beta-blue)
![Stack](https://img.shields.io/badge/Stack-Reactive--Microservices-green)
![License](https://img.shields.io/badge/License-MIT-gray)

## 🎯 Visión General

**CesiumFlow** es un ecosistema de microservicios diseñado para la transformación de feedback no estructurado en activos de datos accionables. Mediante un stack reactivo y procesamiento NLP descentralizado, la plataforma permite la categorización de sentimientos y extracción de métricas críticas con alta eficiencia y baja latencia.

### 🏛️ Arquitectura y Stack Técnico

El proyecto implementa un patrón de **desacoplamiento funcional** diseñado para la escalabilidad. A continuación, se detallan las piezas core del ecosistema:

| Componente        | Tecnología   | Versión / Distribución |
| :---------------- | :----------- | :--------------------- |
| **Runtime Java**  | Java         | 17 (Eclipse Temurin)   |
| **Build Tool**    | Maven        | 3.9.x                  |
| **Framework**     | Spring Boot  | 3.5.9 (WebFlux)        |
| **IA Engine**     | Python       | 3.14.2                 |
| **API Engine**    | FastAPI      | 0.127.x                |
| **ML Stack**      | Scikit-learn | 1.8.x                  |
| **Persistencia**  | PostgreSQL   | 15-alpine              |
| **Documentación** | OpenAPI 3.0  | Springdoc-openapi v2.x |

1. **Core Service:** Orquestador reactivo encargado de la lógica de negocio y persistencia R2DBC.
2. **Inference Engine:** Motor especializado en el procesamiento del pipeline NLP.
3. **Persistence Layer:** Almacén relacional optimizado para telemetría en tiempo real.

---

## 🛠️ Prerrequisitos y Herramientas

Para garantizar la estabilidad y paridad del entorno, se requiere la instalación de las siguientes herramientas oficiales:

### 🐳 Infraestructura y Runtime

-   [**Docker Desktop**](https://www.docker.com/products/docker-desktop/): Motor principal de virtualización. Debe estar en ejecución para levantar el clúster.
    -   _Nota: Incluye **Docker Compose**, necesario para la orquestación de los microservicios._

### ⚡ Automatización y Productividad (DX)

-   [**Just (Task Runner)**](https://github.com/casey/just#installation): Herramienta de automatización de comandos (IaC).
-   [**Mise-en-place**](https://mise.jdx.dev/getting-started.html): Gestor de versiones para sincronizar **Java 17** y **Python 3.14** localmente (opcional si se usa exclusivamente Docker).

---

## 🚀 Guía de Inicio Rápido (Quick Start)

### 1. Clonar el repositorio

```bash
git clone https://github.com/cesiumflow/sentiment-api.git && cd sentiment-api
```

### 2. Configurar el entorno

Copia el archivo de plantilla y ajusta tus credenciales (el archivo `.env` está protegido por `.gitignore`).

```bash
cp .env.example .env
```

### 3. Lanzar el ecosistema completo

Utilizamos **Just** para abstraer la complejidad operativa de Docker Compose y garantizar la paridad entre entornos.

```bash
# Inicializa el clúster en modo desarrollo con Hot-Reload activo
just dev
```

```bash
# Verificar el estado de los servicios y el stream de logs
just logs
```

> [!IMPORTANT]
>
> ### 💡 Tips para Desarrolladores (Windows)
>
> Dado que el ecosistema corre sobre contenedores Linux, es crucial mantener la compatibilidad de archivos:
>
> -   **Finales de línea (LF):** Asegúrate de que tu editor (VS Code/IntelliJ) esté configurado para usar **LF** en lugar de **CRLF**. Los archivos con `CRLF` pueden causar errores de ejecución en el `Justfile` y scripts dentro de Docker.
> -   **Terminal recomendada:** Utiliza **Git Bash** o **WSL2** para ejecutar los comandos de `just`. La terminal estándar de Windows (CMD) puede tener limitaciones con la sintaxis de los comandos de automatización.

## 💻 Configuración del Entorno Local (Opcional)

Si deseas contar con soporte completo de tu IDE (autocompletado, linting y tests locales) sin depender exclusivamente de los contenedores, sigue estos pasos para sincronizar tu entorno con **mise**:

```bash
# Autorizar, instalar runtimes y verificar paridad
mise trust && mise install
```

Una vez finalizada la instalación, confirme las versiones:

-   **Java:** Ejecute `java -version`
-   **Python:** Ejecute `python --version`

> [!NOTE]
>
> ### 💡 Soporte de IDE y Alternativas
>
> Este paso es altamente recomendado para habilitar las capacidades de **análisis estático de código** y autocompletado en **VS Code** o **IntelliJ IDEA**.
>
> -   **Alternativa Manual:** Si prefieres no usar `mise`, asegúrate de tener instalados localmente **Java 17 (Temurin)** y **Python 3.14.x** manualmente para garantizar la paridad con los contenedores y evitar errores de compilación en tu editor.

## 📂 Estructura del Ecosistema

El repositorio está organizado siguiendo un patrón de **Microservicios Políglotas**, desacoplando la inteligencia de datos de la lógica de negocio y la persistencia.

```text
.
├── core-service/          # Microservicio Java (Spring Boot + R2DBC)
│   ├── src/               # Lógica de dominio y orquestación
│   └── pom.xml            # Dependencias del ecosistema Maven
├── data-science/          # Engine Python (FastAPI + Scikit-learn)
│   ├── models/            # Modelos serializados (.joblib)
│   └── src/               # Inferencia NLP y procesamiento de texto
├── db/                    # Infraestructura de datos
│   └── init.sql           # Definición de esquemas y proyecciones
├── .env.example           # Contrato de variables de entorno
├── docker-compose.yml     # Orquestador de topología de red
└── Justfile               # Manifiesto de automatización operativa
```

## ⚙️ Comandos de Operación (Justfile)

Para agilizar el desarrollo y la gestión de infraestructura, hemos centralizado las tareas críticas en el `Justfile`. Esto garantiza que todos los miembros del equipo utilicen los mismos estándares operativos.

| Comando             | Acción          | Justificación Técnica                                                  |
| :------------------ | :-------------- | :--------------------------------------------------------------------- |
| `just dev`          | `up -d --build` | Inicializa el clúster con sincronización de código y persistencia.     |
| `just logs`         | `logs -f`       | Despliega el stream de telemetría de todos los servicios.              |
| `just clean`        | `down --rmi -v` | Purga total de contenedores, volúmenes y caché de imágenes.            |
| `just reset-docker` | `prune + rm`    | Hard reset ante colisiones críticas de red o estados inconsistentes.   |
| `just test-int`     | `mvn test`      | Ejecuta la suite de integración validando el contrato entre servicios. |

## 🛡️ Seguridad y Contratos API

El ecosistema **CesiumFlow** sigue principios de seguridad por diseño y documentación basada en contratos.

### 🔐 Gobernanza de Secretos

-   **Aislamiento de Configuración:** Las credenciales de infraestructura se inyectan dinámicamente vía `.env`.
-   **Protección de Versiones:** El archivo `.env` está estrictamente excluido del historial de Git para evitar fugas de seguridad. Consulte `.env.example` para la estructura base.

### 📜 Especificación Técnica (Contract-First)

La documentación del API se genera dinámicamente bajo el estándar **OpenAPI 3.0**. Esto permite que los consumidores (Frontend y Dashboards) mantengan sincronía con el esquema de datos del Core.

-   **Swagger UI:** `http://localhost:8080/swagger-ui.html`
-   **JSON Spec:** `http://localhost:8080/v3/api-docs`

---

**Squad 55 - CesiumFlow Project**
