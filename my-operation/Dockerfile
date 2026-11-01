# M2 맥북 맞춤형 강제 지정
FROM --platform=linux/arm64 eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar app.jar

# -Xmx256m: 최대 힙 메모리 256MB로 제한 (5분 폭파용)
# -XX:+HeapDumpOnOutOfMemoryError: OOM 터질 때 덤프 파일 생성
# -XX:HeapDumpPath=/app/heapdump.hprof: 덤프 파일 저장 경로 지정
ENTRYPOINT ["java", "-Xmx256m", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/app/heapdump.hprof", "-jar", "app.jar"]