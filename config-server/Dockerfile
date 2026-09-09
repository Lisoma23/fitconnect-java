FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY class-service/pom.xml class-service/
COPY booking-service/pom.xml booking-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/

COPY config-server/src config-server/src
COPY config-server/config-repo config-server/config-repo

RUN mvn -q -pl config-server -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /workspace/config-server/target/config-server-*.jar app.jar
COPY --from=build /workspace/config-server/config-repo config-repo

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
