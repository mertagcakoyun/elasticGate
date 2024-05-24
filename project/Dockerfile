FROM registry/infrastructure/temurin/temurin-java19-jdk AS build-project
ADD . ./elastic-gate
WORKDIR /elastic-gate
RUN ./gradlew shadowJar

FROM registry/infrastructure/temurin/temurin-java19
EXPOSE 8080

COPY --from=build-project ./elastic-gate/build/libs/elastic-gate.jar ./elastic-gate.jar
ENV ARTIFACT_NAME="elastic-gate.jar"
