FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN mkdir -p /data
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9002
ENTRYPOINT ["java", "-jar", "app.jar"]
