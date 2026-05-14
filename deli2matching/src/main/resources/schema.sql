-- MyBatis 전환 후 JPA 자동 DDL이 비활성화되므로, 직접 테이블 생성 필요
-- 애플리케이션 첫 실행 전에 이 SQL을 MySQL에서 실행하세요.

CREATE TABLE IF NOT EXISTS user_entity (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(255) NOT NULL,
    password      VARCHAR(255),
    role          VARCHAR(50),
    auth_provider VARCHAR(50),
    PRIMARY KEY (id),
    UNIQUE KEY uq_username (username)
);

CREATE TABLE IF NOT EXISTS todo (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    user_id BIGINT       NOT NULL,
    title   VARCHAR(500),
    done    TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_todo_user_id (user_id)
);
