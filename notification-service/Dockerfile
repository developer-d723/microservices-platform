FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY notification-service/target/notification-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]