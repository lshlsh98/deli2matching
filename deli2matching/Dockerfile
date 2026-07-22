########### 빌드(builder) 단계

# eclipse-temurin:17-jdk-alpine 은 자바 코드를 만들 수 있는 도구가 들어있는 기본 재료
FROM eclipse-temurin:17-jdk-alpine AS builder
# /app 이라는 작업 폴더를 만들고, 그 안에서 작업
WORKDIR /app
#eclipse-temurin:17-jdk-alpine 이미지를 기반으로 컨테이너를 만들고, 그 컨테이너 안의 /app 디렉터리를 작업 디렉터리로 사용

# gradlew(빌드 도구 실행 파일)와 gradle 설정 폴더를 먼저 복사
# 소스 코드(src)보다 설정 파일"을 먼저 복사하면, 소스만 바뀌었을 때는 이 단계를 다시 안 해도 돼서 더 빠름
# gradlew = Gradle Wrapper 실행 파일(Gradle을 실행하는 프로그램)
COPY gradlew ./
# gradlew는 혼자서는 실행되지 않음 -> Wrapper 정보가 들어있는 gradle/wrapper 폴더도 함께 있어야함
COPY gradle ./gradle
# build.gradle -> Gradle의 설정 파일(어떤 라이브러리를 사용할지)
# settings.gradle -> 프로젝트 이름, 멀티 모듈 정보 등이 들어있는 설정 파일
COPY build.gradle settings.gradle ./
# COPY . . 안하는 이유 -> Docker Cache 때문
# src만 수정했다고 가정 -> 의존성 그대로 -> COPY . . 이면 의존성을 전부 다시 다운 -> 느림 

# gradlew에 실행 권한을 주고, 필요한 부품(의존성 라이브러리)들을 미리 다운로드
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
# ./gradlew build 하면 의존성 다운, 컴파일, 테스트, jar 생성 까지 하는데 아직 src를 복사하지 않아서 컴파일 못함 -> 의존성만 미리 다운로드
# --no-daemon -> 백그라운드 프로세스를 만들지 말라는 옵션 -> Docker에서는 컨테이너가 빌드가 끝나면 바로 종료 -> Daemon을 유지할 이유가 없음
# 데몬(Daemon) -> 백그라운드에서 계속 실행되면서 필요한 작업을 처리하는 프로그램

# 이제 진짜 소스 코드를 복사하고, 실행 가능한 jar 파일(우리 서버 프로그램 하나로 뭉친 파일)을 만듦
COPY src ./src
# RUN Docker 이미지를 만드는 과정에서 Linux 명령을 실행하라는 뜻
RUN ./gradlew bootJar --no-daemon -x test
# ./gradlew -> Gradle Wrapper 실행
# bootJar 를 실행하면 (컴파일 -> 클래스 생성 -> 라이브러리 포함 -> 실행 가능한 JAR 생성) 을 수행
# -x test -> 테스트 하지 말고 빨리 만들어

########### 실제 서버를 실행하기 위한 실행(runtime) 단계

# 이번엔 만들기 도구는 필요 없고, 실행만 할 수 있는 더 가벼운 재료 사용 (jre = 실행 전용)
# 새로운 이미지의 새로운 단계가 시작 -> 이전 builder 단계 여기서 종료 -> 새 runtime 단계 시작
FROM eclipse-temurin:17-jre-alpine
# jdk -> java 실행 + 컴파일
# jre -> java 실행 가능, 컴파일 불가능

# tzdata는 시간대(한국 시간) 정보 서버 시간이 한국 시간으로 맞게 나오도록 설치
# addgroup/adduser는 spring 이라는 이름의, 힘이 약한(권한이 적은) 사용자를 새로 만듦
# 관리자(root) 권한으로 프로그램을 돌리면 위험할 수 있어서, 안전하게 일반 사용자로 실행하기 위해
RUN apk add --no-cache tzdata \
    && addgroup -S spring && adduser -S spring -G spring
    # spring 이라는 시스템용(-S) 그룹 생성, spring 이라는 시스템용(-S) 유저를 생성하고 방금 만든 spring 그룹에 넣어라
ENV TZ=Asia/Seoul

# 1단계 /app과는 전혀 다른 /app
WORKDIR /app

# 1단계(builder 컨테이너)에서 완성한 jar 파일을 app.jar 이름으로 가지고옴
COPY --from=builder /app/build/libs/*.jar app.jar
# 이 파일의 주인을 방금 만든 spring 사용자로 바꿈
# chown (Change Owner)
# COPY로 복사한 파일의 주인은 보통 root 이다
# chown 사용자:그룹
RUN chown spring:spring app.jar
# 이제부터는 root가 아니라 spring 사용자로 실행
USER spring

# 이 컨테이너는 8080 포트를 사용하는 애플리케이션이라고 문서처럼 알려주는 역할
EXPOSE 8080

# 서버 컴퓨터(EC2)가 메모리가 작을 수도 있으니까, 네가 쓸 수 있는 메모리의 75%까지만 써도 돼 라고 자바한테 알려주는 설정
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# HEALTHCHECK는 "이 상자 안의 프로그램이 아직 잘 살아있는지" 자동으로 확인해주는 기능이에요.
# 30초마다 한 번씩, 8080번 문이 열려 있는지 노크(nc -z)해봐요. 대답이 없으면 "고장났다"고 표시돼요.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
    CMD nc -z localhost 8080 || exit 1

# 상자가 켜질 때 실제로 실행할 명령어예요. "위에서 정한 옵션으로 app.jar를 실행해라!"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
