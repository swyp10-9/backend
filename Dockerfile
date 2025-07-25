    # Start with a base image that includes OpenJDK
    FROM openjdk:21-jdk-slim

    # Set the working directory inside the container
    WORKDIR /app

    # Copy the build artifact to the container
    COPY build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

    # Expose the port that the app will run on
    EXPOSE 8080

    # Run the Spring Boot application
    ENTRYPOINT ["java", "-jar", "app.jar"]
