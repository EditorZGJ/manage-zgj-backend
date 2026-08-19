CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    email VARCHAR(100),
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS auth_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- 插入默认管理员账号（密码 Admin123，后续讲加密）
INSERT IGNORE INTO auth_user (username, password) VALUES ('admin', 'Admin123');