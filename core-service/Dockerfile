# --- STAGE: BUILD ENVIRONMENT (JDK) ---
# Entorno aislado para la generación de artefactos reproducibles.
FROM maven:3.9-eclipse-temurin-17 AS builder

# Configuración de runtime para compilación optimizada en contenedores.
ARG DEBIAN_FRONTEND=noninteractive
ENV MAVEN_OPTS="-Xmx512m -XX:+UseContainerSupport"

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Principio de menor privilegio: Restricción del proceso a un usuario no-root.
RUN useradd -m -u 1001 core-service

WORKDIR /app

# Estrategia de cacheo de capas: Sincronización de dependencias previa al escaneo de fuentes.
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN chown -R core-service:core-service /app

# Monitoreo de disponibilidad mediante el endpoint de salud de Actuator.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

USER core-service
EXPOSE 8080

# Entrypoint dinámico para acelerar el ciclo de desarrollo (hot-reload).
CMD ["mvn", "spring-boot:run"]

# Generación del artefacto productivo omitiendo validaciones unitarias.
USER root
RUN mvn clean package -DskipTests

# --- STAGE: RUNTIME ENVIRONMENT (JRE) ---
# Imagen mínima para reducción de la superficie de ataque y optimización de despliegue.
FROM eclipse-temurin:17-jre-jammy AS production

WORKDIR /app

RUN useradd -m -u 1001 core-service

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Transferencia de artefacto: Exclusión de código fuente y herramientas de build.
COPY --from=builder /app/target/*.jar app.jar

RUN chown core-service:core-service app.jar

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

USER core-service
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]