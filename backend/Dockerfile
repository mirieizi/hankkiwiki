FROM maven:3-openjdk-17-slim AS builder
WORKDIR /workspace

# 1️⃣ 의존성 캐싱을 위해 먼저 pom.xml만 복사
COPY pom.xml ./

# 2️⃣ 의존성만 미리 다운 (이 단계는 캐시가 적용됨!)
RUN mvn dependency:go-offline -B

# 3️⃣ 이후 나머지 소스 복사
COPY . .

# 4️⃣ 빌드 (의존성 캐시가 되어 빠르게 수행됨)
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /workspace
COPY --from=builder /workspace/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
