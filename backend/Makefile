# ========================================
# 공통 설정
# ========================================
.PHONY: build test clean fclean docker-build docker-network docker-up docker-down re logs \
        wbuild wclean wup wdown wnetwork wfclean detect-os

NETWORK_NAME=app-network
IMAGE_NAME=app-1
JAVA_HOME_WIN=C:\Program Files\BellSoft\LibericaJDK-17
DOCKERFILE_PATH=Dockerfile
DOCKER_CONTEXT=.
DOCKER_COMPOSE_FILE=docker-compose-server.yml
PROJECT_NAME=hankkiwiki-server
DOCKER_COMPOSE_CMD=docker-compose -f $(DOCKER_COMPOSE_FILE) -p $(PROJECT_NAME)

# ========================================
# 공통 명령어 (macOS/Linux)
# ========================================
detect-os:
ifeq ($(OS),Windows_NT)
	@echo "🪟 Windows 환경입니다. wup 또는 wbuild를 사용하세요."
else
	@echo "🍎 macOS/Linux 환경입니다. make up 또는 make build를 사용하세요."
endif

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
	docker rmi -f $(IMAGE_NAME) || true
	docker network rm $(NETWORK_NAME) || true

# Docker 이미지 빌드
docker-build: build
	docker build -t $(IMAGE_NAME) -f $(DOCKERFILE_PATH) $(DOCKER_CONTEXT)

# Docker 네트워크 생성 (없으면)
docker-network:
	docker network create $(NETWORK_NAME) || true

# 전체 Docker 실행 (빌드 + 네트워크 + 실행)
docker-up: docker-network docker-build
	$(DOCKER_COMPOSE_CMD) up -d --build

# 전체 Docker 중지 및 볼륨 제거
docker-down:
	$(DOCKER_COMPOSE_CMD) down -v

# 전체 재배포: down → build → up
re:
	@make docker-down
	@make build
	$(DOCKER_COMPOSE_CMD) up -d --build

# 로컬 Spring Boot 실행 (Docker 사용 안 함)
run:
	./mvnw spring-boot:run

logs:
	docker logs -f $(IMAGE_NAME)

# ==========================
# Windows 전용 명령어 시작
# ==========================

# 윈도우 클린 (타겟 폴더, 이미지, 네트워크 초기화)
# 윈도우 클린
# 윈도우 빌드 (PowerShell 전용)
wbuild:
	powershell -Command "& { \
		$env:JAVA_HOME='$(JAVA_HOME_WIN)'; \
		$env:PATH='$$env:JAVA_HOME\\bin;$$env:PATH'; \
		./mvnw.cmd clean package -DskipTests; \
	}"

# 윈도우 클린
wclean:
	if exist target (rmdir /S /Q target) || exit 0
	docker rmi -f $(IMAGE_NAME) || exit 0
	docker network rm $(NETWORK_NAME) || exit 0

# 윈도우 전체 Docker 실행 (빌드 + 네트워크 + 실행)
wup: wnetwork wbuild
	cd .. && docker-compose up -d --build

# Docker 네트워크 생성
wnetwork:
	docker network create $(NETWORK_NAME) || exit 0

wup: wnetwork wbuild
	$(DOCKER_COMPOSE_CMD) up -d --build

# 윈도우 전체 Docker 중지 및 볼륨 제거
wdown:
	$(DOCKER_COMPOSE_CMD) down -v

# 윈도우 전체 초기화
wfclean: wdown
	if exist target (rmdir /S /Q target) || exit 0
	docker rmi -f $(IMAGE_NAME) || exit 0
	docker network rm $(NETWORK_NAME) || exit 0

# ==========================
# Windows 전용 명령어 끝
# ==========================