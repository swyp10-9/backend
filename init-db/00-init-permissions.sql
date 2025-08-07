-- 00-init-permissions.sql
-- MariaDB 권한 설정 스크립트

-- root 사용자에게 모든 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- swyp10 데이터베이스가 없으면 생성
CREATE DATABASE IF NOT EXISTS swyp10 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- root 사용자가 swyp10 데이터베이스에 접근할 수 있도록 명시적 권한 부여
GRANT ALL PRIVILEGES ON swyp10.* TO 'root'@'%';

-- 권한 적용
FLUSH PRIVILEGES;