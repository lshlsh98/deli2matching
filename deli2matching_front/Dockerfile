#  1단계(build): React 코드를 사람이 만든 소스에서, 브라우저가 바로 읽을 수 있는 html/css/js 파일들로 구워내는 단계 
#  2단계(runtime): 다 구운 빵(정적 파일들)을 nginx라는 진열대에 올려서 손님에게 보여주는 단계

# ===== 1단계: 빌드(화면 굽기) =====
# node는 자바스크립트 코드를 다룰 수 있는 도구. alpine은 아주 작고 가벼운 버전 이라는 뜻
FROM node:22-alpine AS builder
WORKDIR /app

# package.json(어떤 부품들이 필요한지 적힌 목록)을 먼저 복사하고,
COPY package*.json ./
# 그 목록에 있는 부품(라이브러리)들을 전부 설치
# 소스 코드보다 이 목록을 먼저 복사해두면, 코드만 바뀌었을 때 이 단계를 다시 안 해도 돼서 빠름
RUN npm ci
# package-lock.json 을 기준으로 모든 라이브러리를 설치
# 설치 이후 node_modules/ 가 생성됨

# 이제 진짜 소스 코드를 전부 복사하고,
COPY . .
# Spring Boot는 src만 복사해도 빌드에 필요한 파일이 모두 준비된 상태였지만, React(Vite)는 src 외에도 여러 설정 파일과 정적 파일이 필요하기 때문에 COPY . .로 프로젝트 전체를 복사하는 것입니다. (.dockerignore를 통해 불필요한 파일은 제외합니다. [ex)node_modules] )

# VITE_API_BASE_URL은 우리 서버(백엔드) 주소가 어디인지 알려주는 값
# React 화면이 로그인 버튼을 누르거나 채팅할 때 이 주소로 요청을 보내게 됨
# 이 값은 빵을 구울 때(빌드할 때) 미리 반죽에 섞여 들어가기 때문에, 나중에 주소가 바뀌면 다시 구워야(다시 빌드해야) 함
ARG VITE_API_BASE_URL
# ARG란? Build ARGument 이미지를 빌드할 때 외부에서 값을 전달받는 변수
# 빌드할 때 VITE_API_BASE_URL이라는 값을 받을 준비를 해 라는 뜻
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
# 컨테이너 환경변수 = ARG 값
# ARG를 바로 안 쓰고 ENV로 옮기는 이유 -> npm run build가 환경 변수(ENV) 를 읽기 때문

# 진짜로 화면을 굽는(빌드하는) 명령어, 결과물은 /app/dist 폴더에 생김
RUN npm run build
# React 코드 -> JavaScript 변환 -> 압축(minify) -> 최적화 -> dist 생성

# ===== 2단계: 실행(진열대에 올리기) =====
# nginx는 정적인 파일(html/css/js)을 손님한테 아주 빠르게 보여주는 전문 프로그램
FROM nginx:alpine
# React는 npm run build가 끝나면 실행 파일이 아니라 정적 파일(HTML, CSS, JavaScript) 만 남습니다. 이 파일들은 계산을 하거나 코드를 컴파일할 필요가 없으므로, Nginx 같은 웹 서버가 단순히 파일을 브라우저에 전달해 주기만 하면 된다 따라서 Runtime 단계에서는 Node.js 대신 Nginx를 사용하는 것

# curl은 이 서버가 잘 살아있는지 확인할 때 쓸 도구
RUN apk add --no-cache curl

# 우리가 직접 쓴 nginx 설정 파일(길 안내도)을 nginx의 정해진 자리에 넣어주고,
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 1단계에서 구워낸 결과물(dist 폴더)을 nginx가 손님에게 보여줄 자리로 복사
COPY --from=builder /app/dist /usr/share/nginx/html
# /usr/share/nginx/html 이 폴더는 Nginx가 기본적으로 웹 페이지를 찾는 위치

# 이 상자는 80번 문(포트, 웹사이트 기본 문)으로 손님을 받는다고 알려주는 표시
EXPOSE 80

# 30초마다 "/healthz" 주소로 살짝 노크해서, 서버가 잘 응답하는지 자동으로 확인
HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=10s \
    --retries=3 \
    CMD curl -fs http://localhost/healthz || exit 1
# localhost 는 컨테이너 자기 자신

# 상자가 켜질 때 nginx를 계속 켜진 상태로(daemon off) 실행하라는 명령
CMD ["nginx", "-g", "daemon off;"]
# Docker는 메인 프로세스가 종료되면 컨테이너도 종료 
# 즉 nginx 실행 -> 백그라운드 -> CMD 종료 -> 컨테이너 종료 라는 문제 발생
# daemon off -> 백그라운드로 가지 말고 포그라운드에서 계속 실행 
# 그러면  컨테이너 시작 -> nginx 실행 -> 계속 실행 중 -> 컨테이너 유지
# java -jar app.jar 에서는 포그라운드에서 실행되기 때문에 daemon off 필요없음
