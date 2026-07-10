# Use a lightweight JRE base image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the fat jar created by the build process
# Adjust the name to match the Gradle output for the shadow/fat jar
COPY build/libs/*jvm.jar /app/ktor-server.jar

# Expose the port Ktor is configured to use
EXPOSE 8080

# Run the Ktor server
CMD ["java", "-jar", "/app/ktor-server.jar"]
