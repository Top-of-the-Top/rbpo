# syntax=docker/dockerfile:1

# One Dockerfile for both components; pick the Gradle module with --build-arg MODULE=api|worker.

FROM eclipse-temurin:25-jdk AS build
ARG MODULE
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY api/build.gradle.kts ./api/
COPY worker/build.gradle.kts ./worker/
RUN ./gradlew --no-daemon :${MODULE}:dependencies || true

COPY ${MODULE}/src ./${MODULE}/src
RUN ./gradlew --no-daemon :${MODULE}:bootJar

FROM eclipse-temurin:25-jre
ARG MODULE
WORKDIR /app

RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /app/${MODULE}/build/libs/${MODULE}-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
