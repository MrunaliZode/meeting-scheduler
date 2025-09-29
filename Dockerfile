# ---------- Build Stage ----------
FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
