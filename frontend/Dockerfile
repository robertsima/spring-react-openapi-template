# Build from the REPO ROOT so the shared contract (openapi/) is in the build context:
#   podman build -f frontend/Dockerfile -t your_repo_name-frontend:latest .

# ---------- Generate the TypeScript client from the contract ----------
# Done here with the official generator image so the Node build stage doesn't need a JDK.
# Keep this tag in sync with frontend/openapitools.json and backend/pom.xml (openapi-generator 7.22.0).
FROM docker.io/openapitools/openapi-generator-cli:v7.22.0 AS openapi
COPY openapi/openapi.yaml /spec/openapi.yaml
RUN java -jar /opt/openapi-generator/modules/openapi-generator-cli/target/openapi-generator-cli.jar generate \
      -i /spec/openapi.yaml -g typescript-fetch -o /out \
      --additional-properties=supportsES6=true

# ---------- Build stage ----------
FROM docker.io/library/node:22-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi
COPY frontend/ ./
COPY --from=openapi /out ./src/generated/api
RUN npm run build

# ---------- Runtime stage ----------
FROM docker.io/nginxinc/nginx-unprivileged:1.27-alpine
COPY frontend/nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

# Defaults for local use; override per environment (see deploy/*.yaml)
ENV BACKEND_URL=http://localhost:8080 \
    APP_ENVIRONMENT=local \
    NGINX_ENVSUBST_FILTER="^(BACKEND_URL|APP_ENVIRONMENT)$"

# 8080 = app, 8081 = stub_status for the Prometheus exporter (internal)
EXPOSE 8080 8081
