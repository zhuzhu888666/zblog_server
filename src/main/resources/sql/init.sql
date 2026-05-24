
-- tb_blog_favorite 建表脚本
CREATE TABLE IF NOT EXISTS tb_blog_favorite (
    favorite_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID',
    article_id  BIGINT   NOT NULL                COMMENT '文章ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (favorite_id),
    UNIQUE KEY uk_user_article (user_id, article_id),
    KEY idx_user_id (user_id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';
