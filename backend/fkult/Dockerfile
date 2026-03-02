# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and pom
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

# Copy the rest of the backend source
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR
COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/database /app/soundSampleUploads

# Expose Spring Boot port
EXPOSE 8080

# Adjust JVM heap size
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
