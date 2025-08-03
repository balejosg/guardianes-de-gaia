FROM openjdk:17-jdk-slim AS builder

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (for better caching)  
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (optimized for production)
RUN ./mvnw clean package -DskipTests -B -Dmaven.compiler.debug=false -Dmaven.compiler.debuglevel=none

# Production stage
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/guardianes-backend-*.jar app.jar

# Create application user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
RUN chown -R appuser:appgroup /app
USER appuser

# Expose ports
EXPOSE 8080 9090

# Health check optimized for production
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with production-optimized JVM flags
CMD ["java", "-Xmx1024m", "-Xms512m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]