FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# my-operation 안에서 빌드할 것이므로 경로를 맞춰줍니다.
COPY build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]