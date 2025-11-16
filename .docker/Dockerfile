# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS base
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Development stage
FROM base AS dev
RUN mvn -B dependency:get -Dartifact=org.springframework.boot:spring-boot-devtools:3.4.5

# for extracting springboot version from pom.xml and automatically using that to download matching dev tools
#RUN SPRING_BOOT_VERSION=$(mvn help:evaluate -Dexpression=project.parent.version -q -DforceStdout) && \
#    mvn dependency:get -DgroupId=org.springframework.boot -DartifactId=spring-boot-devtools -Dversion=${SPRING_BOOT_VERSION}
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev", "-DskipTests", "-Dmaven.test.skip=true", "-Dspring-boot.run.jvmArguments=-Xmx512m -XX:MaxRAMPercentage=75.0"]

FROM base AS builder
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]