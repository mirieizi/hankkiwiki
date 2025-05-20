# # Base image
# FROM openjdk:17-jdk-slim AS builder

# # 작업 디렉토리 설정
# WORKDIR /workspace

# # 빌드에 필요한 파일 복사
# COPY ./mvnw .
# COPY ./.mvn .mvn
# COPY ./pom.xml .
# COPY ./src ./src


# # 빌드
# RUN ./mvnw clean package -DskipTests

# # 런타임 이미지
# FROM openjdk:17-jdk-slim
# WORKDIR /app
# COPY --from=builder /workspace/target/*.jar app.jar

# # 애플리케이션 실행
# ENTRYPOINT ["java", "-jar", "app.jar"]



# Stage 1: build with Maven + JDK-17
FROM maven:3-openjdk-17-slim AS builder
WORKDIR /workspace

# 1) 복사 pom, 소스
COPY pom.xml ./
COPY src ./src

# 2) 패키징
RUN mvn clean package -DskipTests

# Stage 2: run the fat JAR on a slim JRE
FROM openjdk:17-jdk-slim
WORKDIR /workspace

# 3) 빌드된 JAR만 복사
COPY --from=builder /workspace/target/*.jar app.jar

# 4) 앱 실행
ENTRYPOINT ["java","-jar","app.jar"]

