# Stage 1: Builder 이미지
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /workspace

# 로컬 Maven 캐시 볼륨 마운트 (빌드 캐시 활용)
VOLUME /root/.m2

# mvnw 스크립트 및 .mvn 디렉토리 복사
COPY mvnw .
COPY .mvn .mvn

# pom.xml 및 소스 코드 복사
COPY pom.xml .
COPY src/ src/

# mvnw 실행 권한 부여
RUN chmod +x mvnw

# 비대화형 모드(-B)로 패키징 (테스트 스킵)
RUN ./mvnw clean package -DskipTests -B


# Stage 2: Runtime 이미지
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /workspace/target/*.jar app.jar

# 서비스 포트 노출
EXPOSE 8080

# 헬스체크 설정 (Spring Boot Actuator 필요)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
  CMD wget --quiet --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
