create table tb_article
(
    article_id   bigint auto_increment comment '文章ID'
        primary key,
    user_id      bigint                                not null comment '作者用户ID',
    title        varchar(256)                          not null comment '文章标题',
    content      text                                  not null comment '文章正文',
    article_type varchar(32) default 'other'           not null comment '文章类型: tech-技术, life-生活, essay-随笔, notes-笔记, other-其他',
    cover_key    varchar(512)                          null comment '封面',
    status       int         default 1                 not null comment '状态 0-草稿 1-已发布',
    create_time  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '文章表' collate = utf8mb4_unicode_ci;

create index idx_create_time
    on tb_article (create_time);

create index idx_user_id
    on tb_article (user_id);

create table tb_blog_comment
(
    comment_id       bigint auto_increment comment '评论ID'
        primary key,
    article_id       bigint                             not null comment '文章ID',
    user_id          bigint                             not null comment '用户ID',
    parent_id        bigint                             null comment '父评论ID（NULL=顶级评论）',
    reply_to_user_id bigint                             null comment '被回复的用户ID',
    content          text                               not null comment '评论内容',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '博客评论表' collate = utf8mb4_unicode_ci;

create index idx_article_id
    on tb_blog_comment (article_id);

create index idx_parent_id
    on tb_blog_comment (parent_id);

create index idx_user_id
    on tb_blog_comment (user_id);

create table tb_blog_favorite
(
    favorite_id bigint auto_increment comment '收藏ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    article_id  bigint                             not null comment '文章ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '收藏时间',
    constraint uk_user_article
        unique (user_id, article_id)
)
    comment '用户收藏表' collate = utf8mb4_unicode_ci;

create index idx_article_id
    on tb_blog_favorite (article_id);

create index idx_user_id
    on tb_blog_favorite (user_id);

create table tb_blog_history
(
    history_id  bigint auto_increment comment '历史ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    article_id  bigint                             not null comment '文章ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '浏览时间',
    constraint uk_user_article
        unique (user_id, article_id)
)
    comment '用户浏览历史表' collate = utf8mb4_unicode_ci;

create index idx_article_id
    on tb_blog_history (article_id);

create index idx_user_id
    on tb_blog_history (user_id);

create table tb_blog_like
(
    like_id     bigint auto_increment comment '点赞ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    article_id  bigint                             not null comment '文章ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '点赞时间',
    constraint uk_user_article_like
        unique (user_id, article_id)
)
    comment '用户点赞表' collate = utf8mb4_unicode_ci;

create index idx_article_id
    on tb_blog_like (article_id);

create index idx_user_id
    on tb_blog_like (user_id);

create table tb_chat_conversation
(
    conversation_id bigint auto_increment comment '会话ID'
        primary key,
    user_id         bigint                                 not null comment '用户ID',
    title           varchar(256) default ''                not null comment '会话标题',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'AI聊天会话表' collate = utf8mb4_unicode_ci;

create index idx_user_id
    on tb_chat_conversation (user_id);

create table tb_chat_message
(
    message_id      bigint auto_increment comment '消息ID'
        primary key,
    conversation_id bigint                             not null comment '会话ID',
    role            varchar(16)                        not null comment '角色: user | assistant',
    content         text                               not null comment '消息内容',
    image_url       varchar(1024)                      null comment '生成的图片URL',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment 'AI聊天消息表' collate = utf8mb4_unicode_ci;

create index idx_conversation_id
    on tb_chat_message (conversation_id);

create table tb_music
(
    music_id     bigint auto_increment comment '音乐ID'
        primary key,
    title        varchar(256)                           not null comment '歌曲标题',
    artist_id    bigint       default 0                 not null comment '歌手ID（0=直接使用artist字段）',
    artist       varchar(128) default ''                not null comment '歌手名称',
    duration     varchar(16)  default '00:00'           not null comment '时长（格式 mm:ss）',
    file_path    varchar(512) default ''                not null comment '音乐存储路径',
    cover_path   varchar(512) default ''                not null comment '封面存储路径',
    genre        varchar(64)  default ''                not null comment '音乐流派',
    release_time datetime                               null comment '发行时间',
    create_time  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      tinyint(1)   default 0                 not null comment '逻辑删除标记（0=正常, 1=已删除）',
    lyric_path   varchar(500)                           null comment 'LRC歌词文件存储路径'
)
    comment '音乐表' collate = utf8mb4_unicode_ci;

create index idx_artist
    on tb_music (artist);

create index idx_create_time
    on tb_music (create_time);

create index idx_genre
    on tb_music (genre);

create index idx_title
    on tb_music (title);

create table tb_music_favorite
(
    favorite_id bigint auto_increment comment '收藏ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    music_id    bigint                             not null comment '音乐ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '收藏时间',
    constraint uk_user_music
        unique (user_id, music_id)
)
    comment '音乐收藏表' collate = utf8mb4_unicode_ci;

create index idx_music_id
    on tb_music_favorite (music_id);

create index idx_user_id
    on tb_music_favorite (user_id);

create table tb_music_history
(
    history_id bigint auto_increment comment '历史ID'
        primary key,
    user_id    bigint   not null comment '用户ID',
    music_id   bigint   not null comment '音乐ID',
    play_time  datetime not null comment '播放时间（客户端时间）',
    constraint uk_user_music
        unique (user_id, music_id)
)
    comment '音乐播放历史表' collate = utf8mb4_unicode_ci;

create index idx_user_id_play_time
    on tb_music_history (user_id, play_time);

create table tb_playlist
(
    playlist_id bigint auto_increment comment '歌单ID'
        primary key,
    user_id     bigint                                 not null comment '所有者用户ID',
    name        varchar(128)                           not null comment '歌单名称',
    description varchar(512) default ''                not null comment '歌单描述',
    cover_path  varchar(512) default ''                not null comment '封面存储路径',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint(1)   default 0                 not null comment '逻辑删除标记（0=正常, 1=已删除）'
)
    comment '歌单表' collate = utf8mb4_unicode_ci;

create index idx_user_id
    on tb_playlist (user_id);

create table tb_playlist_music
(
    id          bigint auto_increment comment '关联ID'
        primary key,
    playlist_id bigint                             not null comment '歌单ID',
    music_id    bigint                             not null comment '音乐ID',
    sort_order  int      default 0                 not null comment '排序序号',
    create_time datetime default CURRENT_TIMESTAMP not null comment '添加时间',
    constraint uk_playlist_music
        unique (playlist_id, music_id)
)
    comment '歌单歌曲关联表' collate = utf8mb4_unicode_ci;

create index idx_music_id
    on tb_playlist_music (music_id);

create index idx_playlist_id
    on tb_playlist_music (playlist_id);

-- ==================== 标签模块 ====================

create table tb_tag
(
    tag_id      bigint auto_increment comment '标签ID'
        primary key,
    name        varchar(32)                        not null comment '标签名称',
    icon        varchar(64)  default ''            not null comment '图标(FA class)',
    keywords    varchar(512) default ''            not null comment '匹配关键词，逗号分隔',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_tag_name
        unique (name)
)
    comment '标签表' collate = utf8mb4_unicode_ci;

create table tb_article_tag
(
    id         bigint auto_increment comment '关联ID'
        primary key,
    article_id bigint not null comment '文章ID',
    tag_id     bigint not null comment '标签ID',
    constraint uk_article_tag
        unique (article_id, tag_id)
)
    comment '文章标签关联表' collate = utf8mb4_unicode_ci;

create index idx_article_id
    on tb_article_tag (article_id);

create index idx_tag_id
    on tb_article_tag (tag_id);

-- 插入默认标签

INSERT INTO tb_tag (name, icon, keywords) VALUES
('技术', 'fa-solid fa-microchip', '技术,tech,编程,代码,开发,算法,架构'),
('生活', 'fa-solid fa-mug-hot', '生活,life,日常,感悟,日记'),
('随笔', 'fa-solid fa-feather', '随笔,essay,散文,感想'),
('笔记', 'fa-solid fa-book', '笔记,notes,记录,学习,教程'),
('其他', 'fa-solid fa-ellipsis', 'other,其它,杂项');

-- 迁移现有 article_type 数据到 tag 系统

INSERT INTO tb_article_tag (article_id, tag_id)
SELECT a.article_id, t.tag_id FROM tb_article a
JOIN tb_tag t ON (
    (a.article_type = 'tech' AND t.name = '技术') OR
    (a.article_type = 'life' AND t.name = '生活') OR
    (a.article_type = 'essay' AND t.name = '随笔') OR
    (a.article_type = 'notes' AND t.name = '笔记') OR
    (a.article_type = 'other' AND t.name = '其他')
)
WHERE a.article_type IS NOT NULL AND a.article_type != '';

create table tb_user
(
    user_id      bigint auto_increment comment '用户ID'
        primary key,
    email        varchar(128)                       not null comment '邮箱',
    password     varchar(256)                       not null comment '密码（BCrypt加密）',
    role         int      default 1                 not null comment '角色 1-普通用户 2-管理员',
    status       int      default 0                 not null comment '状态 0-正常 1-禁用',
    nickname     varchar(64)                        null comment '昵称',
    gender       int      default 0                 null comment '性别 0-未设置 1-男 2-女',
    user_avatar  varchar(512)                       null comment '头像URL',
    introduction varchar(512)                       null comment '个人简介',
    birthday     date                               null comment '生日',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_email
        unique (email)
)
    comment '用户表' collate = utf8mb4_unicode_ci;


