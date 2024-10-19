FROM openjdk:22
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir secret
COPY src/main/secret/MongoDB.secret secret/MongoDB.secret
ENTRYPOINT ["java","-jar","/app.jar"]
