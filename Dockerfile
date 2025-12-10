# Stage 1 - Build
FROM maven:3.8-openjdk-18-slim AS build
WORKDIR /build
COPY pom.xml .
COPY spring-redis-search-om-api/pom.xml ./spring-redis-search-om-api/
COPY spring-redis-search-om-api/src ./spring-redis-search-om-api/src
RUN mvn -f ./pom.xml clean package


## Stage 2 - Package
FROM eclipse-temurin:21-jdk-ubi10-minimal AS runtime
COPY --from=build /build/spring-redis-search-om-api/target/*.jar app.jar
COPY --from=build /build/spring-redis-search-om-api/src/main/resources/movies.json movies.json
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]