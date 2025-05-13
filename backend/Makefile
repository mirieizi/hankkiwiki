.PHONY: build test clean fclean docker-build docker-network docker-up docker-down run re logs

# Maven 빌드 (테스트 생략)
build:
	./mvnw clean package -DskipTests

# Maven 테스트
test:
	./mvnw test

# Maven 클린
clean:
	./mvnw clean

# 전체 초기화 (컨테이너, 이미지, 네트워크, 타겟 디렉토리)
fclean: clean docker-down
	rm -rf target
	docker rmi -f hankkiwiki-app || true
	docker network rm app-network || true

# Docker 이미지 빌드
docker-build: build
	docker build -t hankkiwiki-app .

# Docker 네트워크 생성 (없으면)
docker-network:
	docker network create app-network || true

# 전체 Docker 실행 (빌드 + 네트워크 + 실행)
docker-up: docker-network docker-build
	docker-compose up -d --build

# 전체 Docker 중지 및 볼륨 제거
docker-down:
	docker-compose down -v

# 전체 재배포: down → build → up
re:
	docker-compose down -v
	./mvnw clean package -DskipTests
	docker-compose up -d --build

# 로컬 Spring Boot 실행 (Docker 사용 안 함)
run:
	./mvnw spring-boot:run

# 컨테이너 로그 보기
logs:
	docker logs -f hankkiwiki-app
