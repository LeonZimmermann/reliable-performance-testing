# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Gradle infrastructure first (layer is cached as long as these don't change)
COPY gradle/ gradle/
COPY gradlew ./
RUN chmod +x gradlew

# Copy build scripts for all modules Gradle needs to configure
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY buildSrc/ buildSrc/
COPY oas/ oas/
COPY backend/build.gradle.kts backend/build.gradle.kts

# Pre-download dependencies (cached layer — re-runs only when build scripts change)
RUN ./gradlew :backend:dependencies --no-daemon 2>/dev/null || true

# Copy source and build the executable fat JAR
COPY backend/src/ backend/src/
RUN ./gradlew :backend:bootJar --no-daemon

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
