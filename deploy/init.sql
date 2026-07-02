-- 创建数据库 (schema)
CREATE DATABASE IF NOT EXISTS `agent-builder` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- 确认数据库时区是否正确
SELECT NOW(), @@time_zone;
-- (可选) 创建用户并授权
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;