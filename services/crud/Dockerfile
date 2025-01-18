FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY ./demo/pom.xml .
COPY ./demo/src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
EXPOSE 50051
ENTRYPOINT ["java", "-jar", "app.jar"]