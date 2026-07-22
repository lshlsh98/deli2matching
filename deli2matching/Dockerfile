# 이 파일은 "우리 서버 프로그램을 상자(컨테이너) 안에 어떻게 포장할지" 알려주는 설명서예요.
# 크게 두 단계로 나뉘어요.
#  1단계(build): 요리 재료를 손질하고 요리를 만드는 "주방"
#  2단계(runtime): 다 만든 요리만 예쁘게 담아서 손님(사용자)에게 내놓는 "테이블"
# 이렇게 나누면 최종 상자 안에는 요리 도구(빌드 도구)는 빼고 완성된 요리(jar 파일)만 남아서
# 상자 크기가 훨씬 작고 가벼워져요.

# ===== 1단계: 빌드(요리 만들기) =====
# eclipse-temurin:17-jdk-alpine 은 자바 코드를 "만들 수 있는" 도구가 들어있는 기본 재료예요.
FROM eclipse-temurin:17-jdk-alpine AS builder
# /app 이라는 작업 폴더를 만들고, 지금부터 그 안에서 작업할게요.
WORKDIR /app

# gradlew(빌드 도구 실행 파일)와 gradle 설정 폴더를 먼저 복사해요.
# 소스 코드보다 "설정 파일"을 먼저 복사하면, 소스만 바뀌었을 때는 이 단계를 다시 안 해도 돼서 더 빨라요.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
# gradlew에 실행 권한을 주고, 필요한 부품(의존성 라이브러리)들을 미리 다운로드해요.
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 이제 진짜 소스 코드를 복사하고,
COPY src ./src
# 실행 가능한 jar 파일(우리 서버 프로그램 하나로 뭉친 파일)을 만들어요.
# -x test 는 "지금은 테스트는 하지 말고 빨리 만들어줘" 라는 뜻이에요.
RUN ./gradlew bootJar --no-daemon -x test

# ===== 2단계: 실행(손님상 차리기) =====
# 이번엔 "만들기" 도구는 필요 없고, "실행"만 할 수 있는 더 가벼운 재료를 써요. (jre = 실행 전용)
FROM eclipse-temurin:17-jre-alpine

# tzdata는 시간대(한국 시간) 정보예요. 서버 시간이 한국 시간으로 맞게 나오도록 설치해요.
# addgroup/adduser는 "spring"이라는 이름의, 힘이 약한(권한이 적은) 사용자를 새로 만드는 거예요.
# 관리자(root) 권한으로 프로그램을 돌리면 위험할 수 있어서, 안전하게 일반 사용자로 실행하려고요.
RUN apk add --no-cache tzdata \
    && addgroup -S spring && adduser -S spring -G spring
ENV TZ=Asia/Seoul

WORKDIR /app
# 1단계(주방)에서 완성한 jar 파일만 쏙 가져와요. (요리 도구는 안 가져와요!)
COPY --from=builder /app/build/libs/*.jar app.jar
# 이 파일의 주인을 방금 만든 spring 사용자로 바꿔주고,
RUN chown spring:spring app.jar
# 이제부터는 관리자가 아니라 spring 사용자로 실행할게요.
USER spring

# 이 상자는 8080번 문(포트)으로 손님을 받는다고 알려주는 표시예요.
EXPOSE 8080

# 서버 컴퓨터(EC2)가 메모리가 작을 수도 있으니까,
# "네가 쓸 수 있는 메모리의 75%까지만 써도 돼" 라고 자바한테 알려주는 설정이에요.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# HEALTHCHECK는 "이 상자 안의 프로그램이 아직 잘 살아있는지" 자동으로 확인해주는 기능이에요.
# 30초마다 한 번씩, 8080번 문이 열려 있는지 노크(nc -z)해봐요. 대답이 없으면 "고장났다"고 표시돼요.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
    CMD nc -z localhost 8080 || exit 1

# 상자가 켜질 때 실제로 실행할 명령어예요. "위에서 정한 옵션으로 app.jar를 실행해라!"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
