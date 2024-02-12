# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR ./
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ /app/src/
RUN mvn package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17
WORKDIR /app
COPY --from=build /target/vicarius.jar /app
CMD ["java", "-jar", "vicarius.jar"]
#FROM openjdk:21
#WORKDIR /app
#ADD target/vicarius.jar /app
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "vicarius.jar"]