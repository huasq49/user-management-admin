FROM maven:3-eclipse-temurin-24 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 app
COPY --from=build /src/target/server-1.0.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]