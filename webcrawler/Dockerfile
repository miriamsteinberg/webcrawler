# Use a base image with JRE
FROM openjdk:11

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/your-application.jar app.jar

# Expose the port
EXPOSE 8080

# Define the command to run your application
CMD ["java", "-jar", "app.jar"]