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
# OS 감지
# ========================================
detect-os:
ifeq ($(OS),Windows_NT)
	@echo "🪟 Windows 환경입니다. wup 또는 wbuild를 사용하세요."
else
	@echo "🍎 macOS/Linux 환경입니다. make up 또는 make build를 사용하세요."
endif

# ========================================
# macOS/Linux 명령어
# ========================================
build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

clean:
	./mvnw clean

fclean: clean docker-down
	rm -rf target
	docker rmi -f $(IMAGE_NAME) || true
	docker network rm $(NETWORK_NAME) || true

docker-build: build
	docker build -t $(IMAGE_NAME) -f $(DOCKERFILE_PATH) $(DOCKER_CONTEXT)

docker-network:
	docker network create $(NETWORK_NAME) || true

docker-up: docker-network docker-build
	$(DOCKER_COMPOSE_CMD) up -d --build

docker-down:
	$(DOCKER_COMPOSE_CMD) down -v

re:
	@make docker-down
	@make build
	$(DOCKER_COMPOSE_CMD) up -d --build

run:
	./mvnw spring-boot:run

logs:
	docker logs -f $(IMAGE_NAME)

# ========================================
# Windows 전용 명령어
# ========================================
wbuild:
	powershell -Command "& { \
		$env:JAVA_HOME='$(JAVA_HOME_WIN)'; \
		$env:PATH='$$env:JAVA_HOME\\bin;$$env:PATH'; \
		./mvnw.cmd clean package -DskipTests; \
	}"

wclean:
	if exist target (rmdir /S /Q target) || exit 0
	docker rmi -f $(IMAGE_NAME) || exit 0
	docker network rm $(NETWORK_NAME) || exit 0

wnetwork:
	docker network create $(NETWORK_NAME) || exit 0

wup: wnetwork wbuild
	$(DOCKER_COMPOSE_CMD) up -d --build

wdown:
	$(DOCKER_COMPOSE_CMD) down -v

wfclean: wdown
	if exist target (rmdir /S /Q target) || exit 0
	docker rmi -f $(IMAGE_NAME) || exit 0
	docker network rm $(NETWORK_NAME) || exit 0
