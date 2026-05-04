FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/finance-tracker-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]