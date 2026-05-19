-- tb_user 建表脚本
-- 基于 cc.ztzhome.zblog.bean.entity.User

CREATE TABLE IF NOT EXISTS tb_user (
    user_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    email        VARCHAR(128) NOT NULL                 COMMENT '邮箱',
    password     VARCHAR(256) NOT NULL                 COMMENT '密码（BCrypt加密）',
    role         INT          NOT NULL DEFAULT 1       COMMENT '角色 1-普通用户 2-管理员',
    status       INT          NOT NULL DEFAULT 0       COMMENT '状态 0-正常 1-禁用',
    nickname     VARCHAR(64)           DEFAULT NULL    COMMENT '昵称',
    gender       INT                   DEFAULT 0       COMMENT '性别 0-未设置 1-男 2-女',
    user_avatar  VARCHAR(512)          DEFAULT NULL    COMMENT '头像URL',
    introduction VARCHAR(512)          DEFAULT NULL    COMMENT '个人简介',
    birthday     DATE                  DEFAULT NULL    COMMENT '生日',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
