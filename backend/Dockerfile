FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p /app/photos
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]