# === 1. Build Stage ===
FROM node:18-alpine AS builder
WORKDIR /app

# 의존성 설치
COPY package*.json ./
RUN npm ci

# 소스 복사 & 빌드
COPY . .
# 환경변수 파일(.env.production)이 존재해야 하고, VITE_APP_API_URL이 설정되어 있어야 합니다.
RUN npm run build

# === 2. Run Stage ===
FROM nginx:stable-alpine
# 기본 Nginx 설정 덮어쓰기 (optional: 프록시 설정 포함)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드된 정적 파일 복사
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
