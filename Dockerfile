# Backend Dockerfile (simple multi-stage build)

# Build stage: compile the Spring Boot app
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy Maven files first to leverage layer caching
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw mvnw
# Download dependencies
RUN ./mvnw -q -DskipTests dependency:go-offline
# Copy source and build
COPY src/ src/
RUN ./mvnw -q -DskipTests package

# Runtime stage: run the app with a smaller JRE image
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
# Start the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
