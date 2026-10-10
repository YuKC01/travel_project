-- ============================================
-- 数据库迁移脚本（可重复执行，字段存在则跳过）
-- 解决：重复执行报错导致后续字段未添加的问题
-- ============================================

-- ========== 1. user 表：安全添加字段 ==========
-- status：账号状态（1正常 0禁用）
DROP PROCEDURE IF EXISTS add_user_status;
DELIMITER $$
CREATE PROCEDURE add_user_status()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'status') THEN
        ALTER TABLE `user` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT '账号状态：1正常 0禁用' AFTER `is_admin`;
    END IF;
END$$
DELIMITER ;
CALL add_user_status();
DROP PROCEDURE IF EXISTS add_user_status;

-- nick_name：昵称
DROP PROCEDURE IF EXISTS add_user_nick_name;
DELIMITER $$
CREATE PROCEDURE add_user_nick_name()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'nick_name') THEN
        ALTER TABLE `user` ADD COLUMN `nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称' AFTER `status`;
    END IF;
END$$
DELIMITER ;
CALL add_user_nick_name();
DROP PROCEDURE IF EXISTS add_user_nick_name;

-- avatar：头像
DROP PROCEDURE IF EXISTS add_user_avatar;
DELIMITER $$
CREATE PROCEDURE add_user_avatar()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'avatar') THEN
        ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL' AFTER `nick_name`;
    END IF;
END$$
DELIMITER ;
CALL add_user_avatar();
DROP PROCEDURE IF EXISTS add_user_avatar;

-- 给已有用户设置默认昵称（用用户名兜底）
UPDATE `user` SET `nick_name` = `username` WHERE `nick_name` IS NULL;

-- ========== 2. travel_comment 表：安全添加 parent_id 字段 ==========
DROP PROCEDURE IF EXISTS add_comment_parent_id;
DELIMITER $$
CREATE PROCEDURE add_comment_parent_id()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'travel_comment' AND COLUMN_NAME = 'parent_id') THEN
        ALTER TABLE `travel_comment` ADD COLUMN `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID，0=顶级评论' AFTER `comment_content`;
    END IF;
END$$
DELIMITER ;
CALL add_comment_parent_id();
DROP PROCEDURE IF EXISTS add_comment_parent_id;

-- 给已有评论设置 parent_id=0
UPDATE `travel_comment` SET `parent_id` = 0 WHERE `parent_id` IS NULL;

-- ========== 3. 攻略状态说明 ==========
-- status = 0 下线
-- status = 1 上线
-- status = 2 待审核（新发布默认值）
-- 已有攻略保持原有 status 不变
