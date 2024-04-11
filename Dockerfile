FROM gradle:8.6.0-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble

FROM openjdk:19

RUN mkdir /app

COPY --from=build /home/gradle/src/app/build/libs/ /app/

ENTRYPOINT ["java","-jar","/app/Middleware.jar"]
# Use an official OpenJDK runtime as a parent image
# FROM openjdk:11

# # Set the working directory in the container
# WORKDIR /app

# # Copy Gradle build files
# COPY app/build.gradle.kts .
# COPY settings.gradle.kts .
# COPY gradlew .
# COPY gradle/ ./gradle/

# # Copy the application source code
# COPY app .

# # Build the application using Gradle
# RUN ./gradlew build --dry-run --no-daemon

# # Expose the port the application runs on
# EXPOSE 3005  
# # ${PORT}

# Define the command to run your application
#CMD ["java", "-jar", "build/libs/middleware_maraffa.jar"]

#docker build --network=host -t  middleware-maraffaonline .
#docker run -p 3005:3005 middleware-maraffaonline