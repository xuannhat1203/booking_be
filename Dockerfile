# Stage 1: Build the application
FROM gradle:9.3.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build without running tests to speed up the process
RUN gradle build --no-daemon -x test

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /home/gradle/src/build/libs/booking_app_be-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 6789

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
