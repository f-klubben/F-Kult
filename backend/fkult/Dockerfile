# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and pom first (better layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the backend source
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Create directories for SQLite DB and uploads
RUN mkdir -p /app/database /app/soundSampleUploads

# Expose Spring Boot port
EXPOSE 8080

# Optional JVM tuning
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
