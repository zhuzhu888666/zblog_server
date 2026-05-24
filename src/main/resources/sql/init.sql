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

-- tb_article 建表脚本
CREATE TABLE IF NOT EXISTS tb_article (
    article_id  BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '文章ID',
    user_id     BIGINT       NOT NULL                 COMMENT '作者用户ID',
    title       VARCHAR(256) NOT NULL                 COMMENT '文章标题',
    content     TEXT         NOT NULL                 COMMENT '文章正文',
    cover_key   VARCHAR(512)          DEFAULT NULL    COMMENT '封面图片RustFS对象键',
    status      INT          NOT NULL DEFAULT 1       COMMENT '状态 0-草稿 1-已发布',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (article_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
