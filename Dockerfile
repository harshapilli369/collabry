# ===========================================
# Stage 1: Build frontend
# ===========================================
# node:20-alpine (LTS) - avoids Docker Hub rate limits; node:22-alpine can hit toomanyrequests
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./

# Production API URL - override with --build-arg in CI if needed
ARG VITE_API_BASE_URL=http://localhost:8073/api/auth
ARG VITE_GOOGLE_CLIENT_ID
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID

RUN npm run build

# ===========================================
# Stage 2: Build backend (with frontend static baked in)
# ===========================================
FROM eclipse-temurin:17-jdk AS backend-build

WORKDIR /app/backend

COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
COPY backend/src ./src

# Overlay frontend build so Spring Boot serves it from /static
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
RUN ./mvnw clean package -DskipTests -B

# ===========================================
# Stage 3: Production image
# ===========================================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

# Expose port (matches deploy -p 8073:8073)
ENV SERVER_PORT=8073
EXPOSE 8073

# Override at runtime if needed (e.g. docker run -e APP_CONFIRMATION_BASE_URL=...)
ENV APP_CONFIRMATION_BASE_URL=http://csci5308-vm5.research.cs.dal.ca:8073

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${SERVER_PORT:-8073} -jar app.jar"]
