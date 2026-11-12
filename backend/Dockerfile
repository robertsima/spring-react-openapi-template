# Build from the REPO ROOT so the shared contract (openapi/) is in the build context:
#   podman build -f backend/Dockerfile -t your_repo_name-backend:latest .

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY openapi ./openapi
COPY backend/pom.xml ./backend/pom.xml
RUN mvn -f backend/pom.xml -B dependency:go-offline

COPY backend/src ./backend/src

RUN mvn -f backend/pom.xml -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 8080

# Lets the JVM size its heap from the container memory limit set in deploy/podman-kube.yaml
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
