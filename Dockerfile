FROM gradle:8.10.2-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the JVM fat jar containing the Ktor GraphQL server
RUN ./gradlew fatJar --no-daemon

FROM eclipse-temurin:21-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*-fat.jar /app/econometrics-backend.jar
ENTRYPOINT ["java", "-jar", "/app/econometrics-backend.jar"]
