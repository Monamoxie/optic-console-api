FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/optic-console.jar optic-console.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "optic-console.jar"]
