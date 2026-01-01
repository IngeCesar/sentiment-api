# =========================================================
# STAGE 1: BUILDER & DEVELOPMENT
# =========================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

# 1. Optimization & Args
ARG DEBIAN_FRONTEND=noninteractive
ENV MAVEN_OPTS="-Xmx512m -XX:+UseContainerSupport"

# 2. System Dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 3. Security: Create non-root user (Same as before)
RUN useradd -m -u 1001 core-service

WORKDIR /app

# 4. Caching Dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# 5. Copy Source Code
COPY src ./src

# 6. Permissions for Dev
RUN chown -R core-service:core-service /app

# 7. Healthcheck pointing to Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

# 8. Set User & Port
USER core-service
EXPOSE 8080

# 9. Development Command (Running with Maven)
CMD ["mvn", "spring-boot:run"]

# 10. Build Artifact (Runs as root temporarily to write to target, then switches back)
# Note: We switch to root just to package, avoiding permission issues in some setups
USER root
RUN mvn clean package -DskipTests

# =========================================================
# STAGE 2: PRODUCTION
# =========================================================
# We use a lightweight JRE image for the final artifact
FROM eclipse-temurin:17-jre-jammy AS production

WORKDIR /app

# Create non-root user for production
RUN useradd -m -u 1001 core-service

# Install curl for Healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Permissions
RUN chown core-service:core-service app.jar

# Production Healthcheck pointing to Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

USER core-service
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
