FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["bash", "-c", "java $JAVA_OPTS -jar app.jar"]
