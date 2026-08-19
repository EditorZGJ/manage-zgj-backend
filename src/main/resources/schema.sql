CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    email VARCHAR(100)
);
-- 新增逻辑删除字段（如果已存在，不会重复添加）
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS deleted INT DEFAULT 0;
