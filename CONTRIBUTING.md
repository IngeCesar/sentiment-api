# 🤝 Guía de Contribución y Estándares

## 🌳 Flujo de Trabajo (Git Flow)

- **`main`**: 🔒 **Producción**. Requiere aprobación de **todo el equipo** para fusionar.
- **`develop`**: 🚧 **Integración**. Es la base para crear nuevas ramas.
- **Ramas**: Se crean desde `develop` y usan la convención `tipo/descripcion`.

## 🎨 Estándares de Código

- **Indentación**: 4 Espacios.
- **Codificación**: UTF-8.
- Idioma variables: Inglés.
- **Final de línea**: LF (Estilo Linux/Unix).

### ☕ Backend (Java)

- **Variables/Métodos**: `camelCase`.
- **Clases**: `PascalCase`.
- **Constantes**: `UPPER_SNAKE_CASE`.

### 🐍 Data Science (Python)

- **Variables/Funciones**: `snake_case`.
- **Clases**: `PascalCase`.
- **Notebooks**: Limpiar outputs antes de subir.

## 🏷️ Nombres de Ramas

Formato: `tipo/descripcion-corta` (minúsculas y guiones).

- `feat/` : Nuevas funcionalidades.
- `fix/` : Corrección de errores.
- `docs/` : Documentación.
- `chore/` : Mantenimiento.

## 🚀 Ciclo de Desarrollo

1.  **Inicio**:
    ```bash
    git checkout develop
    git pull origin develop
    git checkout -b feat/mi-nueva-funcionalidad
    ```
2.  **Desarrollo**:
    - Haz commits pequeños y descriptivos.
    - Sube tus cambios: `git push origin feat/mi-nueva-funcionalidad`
3.  **Pull Request (PR)**:
    - Abre el PR hacia `develop`.
    - Completa la checklist.
    - Usa **"Squash and merge"** al finalizar.

## 🧹 Limpieza (Post-Merge)

Una vez fusionado el PR, la rama remota se borra automáticamente. Para no acumular basura en tu local:

1.  **Vuelve a la base y actualiza:**
    ```bash
    git checkout develop
    git pull origin develop
    ```
2.  **Limpia referencias remotas (Prune):**
    - Esto avisa a tu git local que la rama ya no existe en el servidor.
    ```bash
    git fetch -p
    ```
3.  **Borra tu rama local:**
    ```bash
    git branch -d feat/mi-nueva-funcionalidad
    ```

---
