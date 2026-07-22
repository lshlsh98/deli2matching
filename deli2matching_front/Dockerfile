# 이 파일은 "우리 웹사이트(React 화면)를 상자 안에 어떻게 넣을지" 알려주는 설명서예요.
# 역시 두 단계로 나뉘어요.
#  1단계(build): React 코드를 사람이 만든 소스에서, 브라우저가 바로 읽을 수 있는
#                 html/css/js 파일들로 "구워내는" 단계 (마치 반죽을 빵으로 굽는 것처럼요)
#  2단계(runtime): 다 구운 빵(정적 파일들)을 nginx라는 "진열대"에 올려서 손님에게 보여주는 단계

# ===== 1단계: 빌드(화면 굽기) =====
# node는 자바스크립트 코드를 다룰 수 있는 도구예요. alpine은 "아주 작고 가벼운 버전"이라는 뜻이에요.
FROM node:22-alpine AS builder
WORKDIR /app

# package.json(어떤 부품들이 필요한지 적힌 목록)을 먼저 복사하고,
COPY package*.json ./
# 그 목록에 있는 부품(라이브러리)들을 전부 설치해요.
# 소스 코드보다 이 목록을 먼저 복사해두면, 코드만 바뀌었을 때 이 단계를 다시 안 해도 돼서 빨라요.
RUN npm ci

# 이제 진짜 소스 코드를 전부 복사하고,
COPY . .

# VITE_API_BASE_URL은 "우리 서버(백엔드) 주소가 어디인지" 알려주는 값이에요.
# React 화면이 로그인 버튼을 누르거나 채팅할 때 이 주소로 요청을 보내게 돼요.
# 이 값은 빵을 구울 때(빌드할 때) 미리 반죽에 섞여 들어가기 때문에,
# 나중에 주소가 바뀌면 다시 구워야(다시 빌드해야) 해요.
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
# 진짜로 화면을 굽는(빌드하는) 명령어예요. 결과물은 /app/dist 폴더에 생겨요.
RUN npm run build

# ===== 2단계: 실행(진열대에 올리기) =====
# nginx는 "정적인 파일(html/css/js)을 손님한테 아주 빠르게 보여주는" 전문 프로그램이에요.
FROM nginx:alpine

# curl은 "이 서버가 잘 살아있는지" 확인할 때 쓸 도구예요.
RUN apk add --no-cache curl

# 우리가 직접 쓴 nginx 설정 파일(길 안내도)을 nginx의 정해진 자리에 넣어주고,
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 1단계에서 구워낸 결과물(dist 폴더)을 nginx가 손님에게 보여줄 자리로 복사해요.
COPY --from=builder /app/dist /usr/share/nginx/html

# 이 상자는 80번 문(포트, 웹사이트 기본 문)으로 손님을 받는다고 알려주는 표시예요.
EXPOSE 80

# 30초마다 "/healthz" 주소로 살짝 노크해서, 서버가 잘 응답하는지 자동으로 확인해요.
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -fs http://localhost/healthz || exit 1

# 상자가 켜질 때 nginx를 계속 켜진 상태로(daemon off) 실행하라는 명령이에요.
CMD ["nginx", "-g", "daemon off;"]
