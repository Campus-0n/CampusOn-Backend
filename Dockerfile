# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# gradle wrapper와 설정 파일만 먼저 복사 -> 의존성 캐싱으로 빌드 속도 향상
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 실제 빌드 (테스트는 이미지 빌드 시 생략)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---------- 2단계: 실행 ----------
FROM eclipse-temurin:25-jre AS run
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
