# Paso 4: Testing con Docker

## ⚠️ Prerequisito: Docker Desktop debe estar instalado y corriendo

## 4.1 Build de la Imagen

```powershell
# Construir imagen desde la raíz del repositorio
cd "c:\Users\LENOVO\OneDrive\Cursos\Portafolio\No country\Hackaton ONE\sentiment-api"
docker build -t sentiment-engine:production-test data-science/
```

**Salida esperada:**
```
[+] Building 45.2s (19/19) FINISHED
=> [builder 1/8] FROM python:3.14.2-slim
=> [builder 2/8] RUN apt-get update && apt-get install...
=> [builder 7/8] COPY models/ ./models/
=> [builder 8/8] COPY src/ ./src/
=> [production] transferring files...
=> exporting to image
=> => naming to docker.io/library/sentiment-engine:production-test
```

## 4.2 Verificar la Imagen

```powershell
docker images | findstr sentiment-engine
```

**Salida esperada:**
```
sentiment-engine    production-test    abc123def456    2 minutes ago    450MB
```

## 4.3 Ejecutar Contenedor Individual

```powershell
# Iniciar contenedor
docker run -d --name sentiment-test -p 5000:5000 sentiment-engine:production-test

# Ver logs
docker logs sentiment-test -f
```

**Logs esperados:**
```
🚀 Iniciando servidor en el puerto: 5000
INFO:     Started server process [1]
INFO:     Waiting for application startup.
🔄 Inicializando SentimentPredictor...
✅ Motor de ML cargado correctamente (Modelo de Producción v2.0)
   - Accuracy: 83.33%
   - F1-Macro: 0.8344
   - Tiempo inferencia: ~3.88ms/predicción
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:5000
```

## 4.4 Probar Endpoints del Contenedor

```powershell
# Health check
Invoke-RestMethod -Uri "http://localhost:5000/health"

# Predicción
$body = @{
    text = "Excelente producto, muy recomendado"
    top_n_keywords = 5
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5000/predict" -Method POST -Body $body -ContentType "application/json"
```

**Respuesta esperada:**
```json
{
  "prediction": "Positivo",
  "probability": 0.99,
  "keywords": ["excelente", "recomendado", "muy"],
  "timestamp": "2026-01-25T..."
}
```

## 4.5 Detener y Limpiar

```powershell
# Detener contenedor
docker stop sentiment-test

# Eliminar contenedor
docker rm sentiment-test

# Eliminar imagen (opcional)
docker rmi sentiment-engine:production-test
```

## 4.6 Testing con Docker Compose Completo

```powershell
# Desde la raíz del repositorio
cd "c:\Users\LENOVO\OneDrive\Cursos\Portafolio\No country\Hackaton ONE\sentiment-api"

# Levantar todo el stack (frontend + core-service + data-science + db)
docker compose up -d --build

# Ver logs de todos los servicios
docker compose logs -f

# Ver solo logs del servicio data-science
docker compose logs -f data-science

# Verificar que todos los servicios están UP
docker compose ps
```

**Salida esperada de `docker compose ps`:**
```
NAME                    IMAGE                       STATUS       PORTS
sentiment-api-frontend  sentiment-api-frontend      Up           0.0.0.0:5173->5173/tcp
sentiment-api-core      sentiment-api-core          Up           0.0.0.0:8080->8080/tcp
sentiment-api-ml        sentiment-api-ml            Up           0.0.0.0:5000->5000/tcp
sentiment-api-db        postgres:15-alpine          Up           0.0.0.0:5432->5432/tcp
```

## 4.7 Validación End-to-End del Stack Completo

### Test desde Frontend (UI)

1. Abrir navegador: http://localhost:5173
2. Cargar un archivo CSV de reseñas
3. Verificar que las predicciones se muestran correctamente
4. Verificar que el dashboard actualiza las métricas

### Test desde Backend (API Java)

```powershell
# Verificar que el core-service puede comunicarse con data-science
Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/predict" -Method POST `
    -Body (@{text="Test desde core-service"} | ConvertTo-Json) `
    -ContentType "application/json"
```

### Test directo al Motor ML

```powershell
# Test directo al contenedor de data-science
Invoke-RestMethod -Uri "http://localhost:5000/predict" -Method POST `
    -Body (@{text="Test directo al motor ML"} | ConvertTo-Json) `
    -ContentType "application/json"
```

## 4.8 Troubleshooting

### Error: "Port 5000 is already allocated"

```powershell
# Encontrar proceso usando el puerto
netstat -ano | findstr :5000

# Matar el proceso (reemplaza PID con el número obtenido)
taskkill /F /PID <PID>
```

### Error: "Cannot connect to Docker daemon"

1. Abrir Docker Desktop
2. Esperar a que el ícono de Docker en la barra de tareas esté verde
3. Verificar: `docker version`

### Logs no muestran el modelo cargándose

```powershell
# Entrar al contenedor para debug
docker exec -it sentiment-test /bin/sh

# Verificar que los archivos existen
ls -lh /app/models/
ls -lh /app/src/cesiumflow_ml/

# Probar carga manual de Python
python -c "from cesiumflow_ml.predictor import SentimentPredictor; p = SentimentPredictor(); print('OK')"
```

## 4.9 Cleanup Completo

```powershell
# Detener todos los servicios
docker compose down

# Eliminar volúmenes (⚠️ borra datos de BD)
docker compose down -v

# Limpiar imágenes no utilizadas
docker image prune -a
```

---

## ✅ Checklist de Validación Docker

- [ ] Imagen construye sin errores
- [ ] Contenedor inicia correctamente
- [ ] Logs muestran "Motor de ML cargado correctamente"
- [ ] Endpoint /health responde {"status": "UP"}
- [ ] Endpoint /predict funciona con texto de prueba
- [ ] docker compose up inicia todos los servicios
- [ ] Frontend puede comunicarse con backend
- [ ] Backend puede comunicarse con motor ML
- [ ] No hay errores en logs de ningún servicio

---

## 🎯 Próximo Paso

Una vez validado Docker, proceder con:
- **Paso 5:** Commit y Push de cambios
- **Paso 6:** Pull Request a develop
- **Paso 7:** Merge y deployment
