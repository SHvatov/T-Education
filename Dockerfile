FROM eclipse-temurin:23-jdk-alpine

ARG JAR_FILE=/target/*.jar
ARG APP_DIR=/opt/app

RUN mkdir $APP_DIR
WORKDIR $APP_DIR
COPY $JAR_FILE ./app.jar

EXPOSE 8080
ENTRYPOINT java -jar ./app.jar
