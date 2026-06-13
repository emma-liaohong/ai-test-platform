-- AI Test Platform Database Initialization Script
-- Database: ai_test_platform

CREATE DATABASE IF NOT EXISTS `ai_test_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `ai_test_platform`;

-- ----------------------------
-- Table: sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'User ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT 'Username',
    `password`    VARCHAR(200) NOT NULL COMMENT 'Password (BCrypt encrypted)',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT 'Real name',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT 'Phone number',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT 'Avatar URL',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'tester' COMMENT 'Role: admin, tester, developer',
    `create_by`   BIGINT       DEFAULT NULL COMMENT 'Creator ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_by`   BIGINT       DEFAULT NULL COMMENT 'Updater ID',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT 'Deleted flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System User';

-- Insert default admin user (password: admin123, BCrypt encoded)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `status`, `role`)
VALUES ('admin', '$2a$10$EowSMkglOJHpwUGBIwR5XeGqo6FwHKjkXpfJBNi1k5y7m3WR3V0ey', 'Administrator', 'admin@aitest.com', 1, 'admin');

-- ----------------------------
-- Table: sys_system
-- ----------------------------
DROP TABLE IF EXISTS `sys_system`;
CREATE TABLE `sys_system` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'System ID',
    `name`        VARCHAR(100) NOT NULL COMMENT 'System name',
    `code`        VARCHAR(50)  NOT NULL COMMENT 'System code (unique identifier)',
    `description` TEXT         DEFAULT NULL COMMENT 'System description',
    `base_url`    VARCHAR(500) DEFAULT NULL COMMENT 'Base URL / API endpoint',
    `type`        VARCHAR(20)  NOT NULL DEFAULT 'llm' COMMENT 'System type: llm, agent, rag, etc.',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    `create_by`   BIGINT       DEFAULT NULL COMMENT 'Creator ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_by`   BIGINT       DEFAULT NULL COMMENT 'Updater ID',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT 'Deleted flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI System Under Test';
