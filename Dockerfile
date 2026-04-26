# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -Dmaven.test.skip=true dependency:go-offline

COPY src src
RUN ./mvnw -q -Dmaven.test.skip=true clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -r -u 1001 spring
COPY --from=builder /build/target/*.jar app.jar
RUN chown spring:spring /app/app.jar

USER spring
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
  CMD sh -c 'exec 3<>/dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]