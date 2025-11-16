FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY user-service/target/user-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]