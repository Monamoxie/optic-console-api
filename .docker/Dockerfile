# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline


# For development
FROM maven:3.9.9-eclipse-temurin-21 AS dev
WORKDIR /app
COPY . .
EXPOSE 5005
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=dev","-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]


# For production
FROM build AS prod
COPY src ./src
RUN mvn clean package -DskipTests

# Final production image
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]