# Use an official Maven image as a base image
FROM maven:3.8.2-openjdk-17

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project files
COPY . .

# Build the JAR inside the Docker container
RUN mvn clean package

# Your application's runtime image
FROM openjdk:21-bookworm
WORKDIR /app
COPY target/webcrawler-1.0.jar /app/

# Run the application
CMD ["java", "-jar", "/app/webcrawler-1.0.jar"]