# 🤝 Guía de Contribución y Estándares - CesiumFlow

Bienvenido a la guía de desarrollo del equipo. Este documento asegura que todos (Java y Python devs) estemos alineados para movernos rápido sin romper el código.

---

## 🌳 Flujo de Trabajo (Git Flow Simplificado)

Para mantener la velocidad en el Hackathon sin perder estabilidad:

-   **`main`**: 🔒 **Producción**. Código estable y desplegable.
    -   _Regla:_ Requiere **1 aprobación** (Tech Lead o Peer Review) para fusionar.
    -   _Prohibido:_ Hacer push directo (siempre vía Pull Request).
-   **`develop`**: 🚧 **Integración**. Aquí se juntan todas las piezas funcionales.
-   **Ramas**: Se crean siempre desde `develop`.

---

## 🎨 Estándares de Código

### General

-   **Codificación**: UTF-8.
-   **Idioma**: Variables y funciones en **Inglés** (recomendado) o Español (si hay consenso), pero **nunca Spanglish**.
-   **Final de línea**: LF (Estilo Unix/Linux).
    -   _Windows Users:_ Ejecuten este comando una vez para evitar romper el código a los demás:
        ```bash
        git config --global core.autocrlf input
        ```

### ☕ Backend (Java / Spring Boot)

-   **Estilo**: Google Java Format.
-   **Naming**: `camelCase` (variables/métodos), `PascalCase` (Clases), `UPPER_SNAKE` (Constantes).
-   **Lombok**: Uso obligatorio (`@Data`, `@Builder`) para mantener las clases limpias.
-   **Logging**: Usar `@Slf4j`. Prohibido `System.out.println` en producción.

### 🐍 Data Science (Python / FastAPI)

-   **Estilo**: PEP 8.
-   **Naming**: `snake_case` (variables/funciones), `PascalCase` (Clases).
-   **Notebooks (.ipynb)**: ⚠️ **CRÍTICO**.
    -   Deben subirse con los **outputs limpios** (sin gráficos ni tablas renderizadas).
    -   _Razón:_ Git no maneja bien los JSON de notebooks y genera conflictos masivos.

---

## 📝 Conventional Commits (Semántico)

Estandarizamos los mensajes para que el historial sea legible.
**Formato:** `tipo: descripción en imperativo` (como dando una orden).

| Tipo       | Uso                                      | Ejemplo                                   |
| :--------- | :--------------------------------------- | :---------------------------------------- |
| `feat`     | Nueva funcionalidad                      | `feat: add sentiment analysis endpoint`   |
| `fix`      | Corrección de error                      | `fix: resolve null pointer in service`    |
| `docs`     | Documentación                            | `docs: update api_spec with health check` |
| `refactor` | Cambio de código sin funcionalidad nueva | `refactor: simplify prediction logic`     |
| `chore`    | Configuración/Build                      | `chore: update docker-compose ports`      |

---

## 🚀 Ciclo de Desarrollo (Paso a Paso)

### 1. Inicio (Branching)

Siempre actualiza tu local antes de crear una rama:

```bash
git checkout develop
git pull origin develop
git checkout -b feat/nombre-funcionalidad
```

### 2. Desarrollo & Commit

Haz commits pequeños y atómicos.

```bash
git add .
git commit -m "feat: implement logic for timestamp handling"
```

### 3. Pull Request (PR)

1.  Sube tu rama: `git push origin feat/nombre-funcionalidad`.
2.  Abre el PR en GitHub apuntando a **`develop`**.
3.  **Checklist de Autocontrol**:
    -   [ ] ¿Compila/Ejecuta sin errores?
    -   [ ] ¿Borré los `print()` de debug?
    -   [ ] ¿Limpié los outputs del Jupyter Notebook?
4.  Avisar por Discord/Telegram/NoCountry: _"PR listo para revisión"_.

### 4. Merge

Al recibir el ✅ (Approve), realiza el Merge

---

## 🧹 Limpieza (Post-Merge)

Evita acumular ramas muertas en tu máquina local:

```bash
# 1. Volver a base y actualizar
git checkout develop
git pull origin develop

# 2. Borrar referencias remotas que ya no existen (Prune)
git fetch -p

# 3. Borrar tu rama local ya fusionada
git branch -d feat/mi-funcionalidad-vieja
```
