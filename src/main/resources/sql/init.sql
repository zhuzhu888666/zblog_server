
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

-- tb_blog_history 建表脚本
CREATE TABLE IF NOT EXISTS tb_blog_history (
    history_id  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '历史ID',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID',
    article_id  BIGINT   NOT NULL                COMMENT '文章ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (history_id),
    UNIQUE KEY uk_user_article (user_id, article_id),
    KEY idx_user_id (user_id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览历史表';

-- tb_music 建表脚本
CREATE TABLE IF NOT EXISTS tb_music (
    music_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '音乐ID',
    title       VARCHAR(256) NOT NULL                COMMENT '歌曲标题',
    artist_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '歌手ID（0=直接使用artist字段）',
    artist      VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '歌手名称',
    duration    VARCHAR(16)  NOT NULL DEFAULT '00:00' COMMENT '时长（格式 mm:ss）',
    file_path   VARCHAR(512) NOT NULL DEFAULT ''     COMMENT 'RustFS 音乐文件存储路径',
    cover_path  VARCHAR(512) NOT NULL DEFAULT ''     COMMENT 'RustFS 封面图片存储路径',
    genre       VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '音乐流派',
    release_time DATETIME   DEFAULT NULL             COMMENT '发行时间',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT(1)  NOT NULL DEFAULT 0       COMMENT '逻辑删除标记（0=正常, 1=已删除）',
    PRIMARY KEY (music_id),
    KEY idx_title (title),
    KEY idx_artist (artist),
    KEY idx_genre (genre),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音乐表';

-- tb_comment 建表脚本
CREATE TABLE IF NOT EXISTS tb_blog_comment (
    comment_id       BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    article_id       BIGINT   NOT NULL                COMMENT '文章ID',
    user_id          BIGINT   NOT NULL                COMMENT '用户ID',
    parent_id        BIGINT   DEFAULT NULL            COMMENT '父评论ID（NULL=顶级评论）',
    reply_to_user_id BIGINT   DEFAULT NULL            COMMENT '被回复的用户ID',
    content          TEXT     NOT NULL                COMMENT '评论内容',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (comment_id),
    KEY idx_article_id (article_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客评论表';

-- tb_music_favorite 建表脚本
CREATE TABLE IF NOT EXISTS tb_music_favorite (
    favorite_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID',
    music_id    BIGINT   NOT NULL                COMMENT '音乐ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (favorite_id),
    UNIQUE KEY uk_user_music (user_id, music_id),
    KEY idx_user_id (user_id),
    KEY idx_music_id (music_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音乐收藏表';

-- tb_chat_conversation 建表脚本
CREATE TABLE IF NOT EXISTS tb_chat_conversation (
    conversation_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user_id         BIGINT   NOT NULL                COMMENT '用户ID',
    title           VARCHAR(256) NOT NULL DEFAULT '' COMMENT '会话标题',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (conversation_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

-- tb_chat_message 建表脚本
CREATE TABLE IF NOT EXISTS tb_chat_message (
    message_id      BIGINT   NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    conversation_id BIGINT   NOT NULL                COMMENT '会话ID',
    role            VARCHAR(16) NOT NULL             COMMENT '角色: user | assistant',
    content         TEXT     NOT NULL                COMMENT '消息内容',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (message_id),
    KEY idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';
