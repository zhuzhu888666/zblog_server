# ZBlog 后端项目结构说明

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 语言 | **Java** | 25 |
| 框架 | **Spring Boot** | 4.0.6 |
| 构建 | **Maven** | — |
| ORM | **MyBatis-Plus** | 3.5.15 |
| 数据库 | **MySQL** (HikariCP 连接池) | — |
| 缓存 | **Redis** (Lettuce 客户端 + 连接池) | — |
| 安全 | **Spring Security** + **JWT** (jjwt) + **BCrypt** | 0.12.6 |
| AI 集成 | **Spring AI** (DeepSeek 模型) | 2.0.0-M8 |
| 对象存储 | **AWS SDK v2** (S3 兼容 / RustFS) | 2.25.28 |
| 工具 | **Lombok**、**Jakarta Validation** | — |

---

## 顶层目录

```
zblog_server/
├── pom.xml                   # Maven 项目描述（依赖、构建配置）
├── HELP.md                   # Spring Boot 入门文档（自动生成）
├── mvnw / mvnw.cmd           # Maven Wrapper 脚本（无需安装 Maven）
├── .mvn/wrapper/             # Maven Wrapper 属性配置
├── .gitignore                # Git 忽略规则
├── .gitattributes            # Git 属性配置
└── src/
    ├── main/
    │   ├── java/cc/ztzhome/zblog/   # 应用源码（见下方详细说明）
    │   └── resources/
    │       ├── application.yaml     # 主配置文件（服务、数据库、Redis、JWT、S3、AI）
    │       ├── sql/
    │       │   └── init.sql         # 数据库建表 DDL（10 张表）
    │       └── cc/ztzhome/zblog/mapper/  # MyBatis XML 映射文件（12 个）
    └── test/java/cc/ztzhome/zblog/  # 单元测试
```

---

## `src/main/java/cc/ztzhome/zblog/` 源码目录结构

### 1. 入口文件

| 文件 | 说明 |
|---|---|
| `ZblogApplication.java` | 应用主入口：`@SpringBootApplication` → `SpringApplication.run()` |

**服务配置**（来自 `application.yaml`）：
- 端口：`8080`
- 上下文路径：`/api`（所有接口统一前缀 `/api`）

---

### 2. `bean/` — 数据对象层

按职责分为四类对象，严格分离入参/出参/持久化。

```
bean/
├── dto/                       # 请求 DTO（7 个文件）—— 接收前端入参
│   ├── LoginDto.java          # 登录请求
│   ├── RegisterDto.java       # 注册请求
│   ├── ChangePasswordDto.java # 修改密码请求
│   ├── UpdateUserDto.java     # 更新用户资料请求
│   ├── CreateCommentDto.java  # 创建评论请求
│   ├── SendMessageDto.java    # 发送 AI 消息请求
│   └── BatchStatusDto.java    # 批量修改用户状态请求
│
├── entity/                    # 数据库实体（12 个文件）—— 映射数据库表
│   ├── User.java              # 用户
│   ├── Article.java           # 文章
│   ├── Comment.java           # 评论（支持嵌套：parentId、replyToUserId）
│   ├── Favorite.java          # 文章收藏
│   ├── BlogHistory.java       # 阅读历史
│   ├── Music.java             # 音乐（支持软删除）
│   ├── MusicFavorite.java     # 音乐收藏
│   ├── MusicHistory.java      # 播放历史
│   ├── Playlist.java          # 歌单（支持软删除）
│   ├── PlaylistMusic.java     # 歌单-歌曲关联（sortOrder 排序）
│   ├── ChatConversation.java  # AI 聊天会话
│   └── ChatMessageEntity.java # AI 聊天消息（role: user/assistant）
│
├── response/                  # 统一响应包装
│   └── ResponseModel.java     # 通用 API 响应：{ code, message, data }
│
└── vo/                        # 视图对象（9 个文件）—— 返回给前端的出参
    ├── UserVo.java            # 用户信息（已过滤密码等敏感字段）
    ├── ArticleVo.java         # 文章详情
    ├── CommentVo.java         # 评论（含子评论列表）
    ├── LoginVo.java           # 登录响应（含 token 和用户信息）
    ├── MusicVo.java           # 音乐信息
    ├── PlaylistVo.java        # 歌单信息
    ├── PlaylistDetailVo.java  # 歌单详情（含歌曲列表）
    ├── ChatMessageVo.java     # 聊天消息
    ├── ConversationVo.java    # 会话摘要
    └── PageResult.java        # 分页包装：{ records, total, page, size }
```

---

### 3. `config/` — 配置层

跨切面基础设施，通过 Spring `@Configuration` / `@Bean` 装配。

```
config/
├── SecurityConfig.java            # Spring Security 过滤器链配置
│                                  #   - 无状态会话（STATELESS），禁用 CSRF
│                                  #   - URL 三级权限：/public/** 放行、/admin/** 需 ROLE_2、其余需登录
│                                  #   - JWT 过滤器插入 UsernamePasswordAuthenticationFilter 之前
│
├── JwtAuthenticationFilter.java   # JWT 认证过滤器（继承 OncePerRequestFilter）
│                                  #   - 从 Authorization 头提取 Bearer token
│                                  #   - 验证签名/过期时间
│                                  #   - 检查 Redis 中 token 是否存在（支持管理员踢人）
│                                  #   - Redis 不可用时降级为纯 JWT 验证
│                                  #   - 设置 SecurityContextHolder 认证信息
│                                  #   - 设置 request.setAttribute("userId", userId)
│
├── CorsConfig.java                # 跨域配置
│                                  #   - 允许所有源、所有头、所有方法
│                                  #   - 允许凭证传递
│                                  #   - 预检请求缓存 3600 秒
│
├── GlobalExceptionHandler.java    # 全局异常处理器（@RestControllerAdvice）
│                                  #   - MethodArgumentNotValidException → 400
│                                  #   - Exception → 500
│                                  #   - 统一返回 ResponseModel 格式
│
└── RustFsS3Config.java            # S3 存储客户端工厂
                                   #   - 创建 S3Client / S3Presigner Bean
                                   #   - 配置端点、区域、访问密钥
```

---

### 4. `constant/` — 应用常量

```
constant/
└── AppConstants.java              # 存储路径常量、URL 超时时间等
```

---

### 5. `controller/` — REST 控制器层

仅负责参数提取和调用 Service，不含业务逻辑。用户身份通过 `request.getAttribute("userId")` 获取（由 `JwtAuthenticationFilter` 注入）。

```
controller/
├── AuthController.java            # 认证模块
│   POST   /api/public/login                  # 登录
│   POST   /api/public/register               # 注册
│   POST   /api/user/password                 # 修改密码
│   POST   /api/user/logout                   # 登出
│
├── UserController.java            # 用户模块
│   POST   /api/user/profile                  # 获取/更新个人资料
│   POST   /api/user/avatar                   # 上传头像
│   GET    /api/public/getAvatar/{userId}     # 获取头像（公开）
│
├── AdminController.java           # 管理员模块（@RequestMapping("/admin")）
│   GET    /api/admin/users                   # 用户分页列表
│   POST   /api/admin/users                   # 新增用户
│   PUT    /api/admin/users/{id}              # 更新用户
│   DELETE /api/admin/users/{id}              # 删除用户
│   PUT    /api/admin/users/batch             # 批量修改用户状态
│   GET    /api/admin/online-users            # 在线用户列表
│   DELETE /api/admin/online-users/{userId}   # 踢出在线用户
│
├── ArticleController.java         # 文章模块
│   POST   /api/article/create                # 创建文章
│   GET    /api/public/article/list           # 文章分页列表（公开）
│   GET    /api/public/article/random         # 随机获取文章（公开）
│   GET    /api/public/article/{articleId}    # 文章详情（公开）
│   DELETE /api/user/article/{articleId}      # 删除文章
│   GET    /api/user/articles                 # 当前用户的文章列表
│
├── CommentController.java         # 评论模块
│   GET    /api/public/article/{id}/comments  # 文章评论列表（公开，树形嵌套）
│   GET    /api/public/article/{id}/comment/count  # 评论总数（公开）
│   POST   /api/user/comment                  # 创建评论
│   DELETE /api/user/comment/{commentId}      # 删除评论
│
├── FavoriteController.java        # 文章收藏模块
│   POST   /api/user/favorite/{articleId}     # 切换收藏（收藏/取消）
│   GET    /api/user/favorites                # 收藏列表
│   GET    /api/user/favorite/{articleId}/status  # 收藏状态
│
├── BlogHistoryController.java     # 阅读历史模块
│   POST   /api/user/history/{articleId}      # 记录阅读
│   GET    /api/user/history                  # 历史列表
│   DELETE /api/user/history/{articleId}      # 删除单条历史
│   DELETE /api/user/history                  # 清空历史
│
├── MusicController.java           # 音乐模块
│   GET    /api/public/music/list             # 公开音乐列表
│   GET    /api/music/song/url                # 获取歌曲播放 URL
│   GET    /api/music/lyric/{songId}          # 获取歌词
│   POST   /api/admin/music/upload            # 上传音乐（管理员）
│   GET    /api/admin/music/list              # 音乐管理列表（管理员）
│   GET    /api/admin/music/{id}              # 音乐详情（管理员）
│   PUT    /api/admin/music/{id}              # 更新音乐信息（管理员）
│   DELETE /api/admin/music/{id}              # 删除音乐（管理员）
│   POST   /api/admin/music/{id}/lyric        # 上传歌词（管理员）
│
├── MusicFavoriteController.java   # 音乐收藏模块
│   POST   /api/music/favorite/{musicId}      # 切换收藏
│   GET    /api/music/favorites               # 收藏列表
│   GET    /api/music/favorite/{musicId}/status  # 收藏状态
│
├── MusicHistoryController.java    # 播放历史模块
│   POST   /api/music/history/{musicId}       # 记录播放
│   GET    /api/music/history                 # 历史列表
│   DELETE /api/music/history/{musicId}       # 删除单条历史
│   DELETE /api/music/history                 # 清空历史
│
├── PlaylistController.java        # 歌单模块
│   POST   /api/music/playlist                # 创建歌单
│   GET    /api/music/playlists               # 歌单列表
│   GET    /api/music/playlist/{id}           # 歌单详情（含歌曲列表）
│   PUT    /api/music/playlist/{id}           # 更新歌单
│   DELETE /api/music/playlist/{id}           # 删除歌单
│   POST   /api/music/playlist/{id}/music/{mid}   # 添加歌曲到歌单
│   DELETE /api/music/playlist/{id}/music/{mid}   # 从歌单移除歌曲
│
└── AiChatController.java          # AI 聊天模块
    POST   /api/chat/send                     # 发送消息（SSE 流式返回）
    GET    /api/chat/conversations            # 会话列表
    GET    /api/chat/conversation/{id}        # 会话消息列表
    DELETE /api/chat/conversation/{id}        # 删除会话
```

---

### 6. `service/` — 业务逻辑层

采用 **接口-实现分离** 模式，每个业务领域一个接口 + 一个实现类。

```
service/
├── IAuthService.java             # 认证：登录/注册/登出/密码修改
├── IUserService.java             # 用户：资料/头像/管理员用户 CRUD
├── IArticleService.java          # 文章：CRUD/分页/随机/封面上传
├── ICommentService.java          # 评论：CRUD/嵌套树构建/计数
├── IFavoriteService.java         # 文章收藏：切换/列表/状态
├── IBlogHistoryService.java      # 阅读历史：记录/列表/删除/清空
├── IMusicService.java            # 音乐：上传/列表/CRUD/歌词
├── IMusicFavoriteService.java    # 音乐收藏：切换/列表/状态
├── IMusicHistoryService.java     # 播放历史：记录/列表/删除/清空
├── IPlaylistService.java         # 歌单：CRUD/歌曲管理
├── IChatService.java             # AI 聊天：对话管理/消息流式回复
├── RustFsService.java            # 文件存储：上传/下载/删除
│
└── impl/                         # 实现类（12 个）
    ├── AuthService.java          # 认证业务：BCrypt 密码校验、JWT 生成、Redis token 存储
    ├── UserServiceImpl.java      # 用户业务：资料更新、头像上传
    ├── ArticleServiceImpl.java   # 文章业务：文章 CRUD + 封面图 S3 上传
    ├── CommentServiceImpl.java   # 评论业务：创建评论 + 树形嵌套查询
    ├── FavoriteServiceImpl.java  # 收藏业务：INSERT ON DUPLICATE KEY 切换
    ├── BlogHistoryServiceImpl.java  # 历史业务：记录阅读时间 + 去重
    ├── MusicServiceImpl.java     # 音乐业务：文件上传 S3 + 歌词解析
    ├── MusicFavoriteServiceImpl.java # 音乐收藏业务
    ├── MusicHistoryServiceImpl.java  # 播放历史业务
    ├── PlaylistServiceImpl.java  # 歌单业务：歌单 CRUD + 歌曲增删排序
    ├── ChatServiceImpl.java      # AI 聊天：加载历史消息 → 构建 Prompt → 调用 DeepSeek → 流式返回
    └── RustFsServiceImpl.java    # RustFS S3 存储：文件上传/下载/删除操作
```

---

### 7. `mapper/` — 数据访问层

每个实体对应一个 Mapper 接口 + 一个 XML 映射文件，所有 SQL 均为手写动态 SQL。

```
mapper/
├── UserMapper.java               # 用户查询（按用户名/邮箱/ID）
├── ArticleMapper.java            # 文章查询（分页、随机、条件筛选）
├── CommentMapper.java            # 评论查询（按文章 ID、嵌套树）
├── FavoriteMapper.java           # 收藏查询（按用户、按文章）
├── BlogHistoryMapper.java        # 历史查询（按用户、去重写入）
├── MusicMapper.java              # 音乐查询（条件筛选、分页、软删除）
├── MusicFavoriteMapper.java      # 音乐收藏查询
├── MusicHistoryMapper.java       # 播放历史查询
├── PlaylistMapper.java           # 歌单查询（按用户、软删除）
├── PlaylistMusicMapper.java      # 歌单歌曲关联查询（按歌单、排序）
├── ChatConversationMapper.java   # 会话查询（按用户、分页）
└── ChatMessageMapper.java        # 消息查询（按会话、按角色）
```

对应的 XML 文件位于 `src/main/resources/cc/ztzhome/zblog/mapper/`，关键 SQL 模式：
- `useGeneratedKeys="true"` 自动回填自增主键
- `LIMIT #{limit} OFFSET #{offset}` 手动分页
- `INSERT ... ON DUPLICATE KEY UPDATE` 实现收藏/历史的切换和去重
- `<if>` / `<trim>` / `<foreach>` 动态 SQL 拼接

---

### 8. `utils/` — 工具类

```
utils/
├── JwtUtil.java                  # JWT 工具
│                                 #   - generateToken(userId, role, rememberMe)
│                                 #   - validateToken(token) → boolean
│                                 #   - getUserId(token) / getRole(token)
│                                 #   - 普通 7 天过期，"记住我" 30 天过期
│
├── RedisUtil.java                # Redis 操作封装
│                                 #   - 通用 String/Hash/Set/List 操作
│                                 #   - 支持过期时间设置
│                                 #   - token 存储、在线用户管理
│
├── BCryptUtil.java               # 密码加密工具
│                                 #   - encrypt(rawPassword) → 密文
│                                 #   - match(rawPassword, encodedPassword) → boolean
│
├── FileTypeUtil.java             # 文件类型检测
│                                 #   - 按扩展名分类：音乐(mp3/flac/...)、图片(jpg/png/...)、视频、文本等
│
└── LrcParserUtil.java            # LRC 歌词解析器
                                  #   - 从 InputStream 解析 LRC 格式
                                  #   - 返回 List<LyricLine>（毫秒时间戳 + 歌词文本）
```

---

## 配置文件

| 文件 | 说明 |
|---|---|
| `application.yaml` | 运行时配置：服务端口/上下文路径、MySQL 数据源、Redis 连接、MyBatis-Plus、JWT 密钥/过期时间、RustFS S3 端点/凭证、DeepSeek/Qwen AI 密钥 |
| `pom.xml` | Maven 构建配置：Spring Boot 4.0.6 父 POM、所有依赖坐标、Java 25 编译配置 |
| `init.sql` | 数据库初始化脚本：10 张表的完整 DDL（含索引、唯一约束） |

### 关键配置项

| 配置项 | 值 |
|---|---|
| 服务端口 | `8080` |
| 上下文路径 | `/api` |
| JWT 普通过期 | 7 天 |
| JWT "记住我"过期 | 30 天 |
| Redis 最大连接数 | 8（Lettuce 连接池） |
| MySQL 连接池 | HikariCP 5~15 连接 |
| S3 存储桶 | `zblog` |
| AI 模型 | DeepSeek V4 Flash |

---

## 数据库表

共 10 张业务表，位于远程 MySQL `ztzhome.cc:3306/zblog`。

| 表名 | 说明 | 关键设计 |
|---|---|---|
| `tb_user` | 用户 | 用户名/邮箱唯一 |
| `tb_article` | 文章 | 关联用户、封面图 |
| `tb_blog_comment` | 评论 | parent_id 自引用（嵌套评论）、reply_to_user_id |
| `tb_blog_favorite` | 文章收藏 | (user_id, article_id) 唯一约束 |
| `tb_blog_history` | 阅读历史 | (user_id, article_id) 唯一、记录阅读时间 |
| `tb_music` | 音乐 | title/artist/cover/genre、deleted 软删除 |
| `tb_music_favorite` | 音乐收藏 | (user_id, music_id) 唯一约束 |
| `tb_music_history` | 播放历史 | (user_id, music_id) 唯一、记录播放时间 |
| `tb_chat_conversation` | AI 会话 | 关联用户、会话标题 |
| `tb_chat_message` | AI 消息 | 关联会话、role(user/assistant) |
| `tb_playlist` | 歌单 | 关联用户、deleted 软删除 |
| `tb_playlist_music` | 歌单歌曲 | (playlist_id, music_id) 关联、sort_order 排序 |

---

## 架构要点

### 分层架构

```
HTTP 请求
    │
    ▼
JwtAuthenticationFilter    ← 提取 JWT → 验证签名/过期 → Redis 校验 → 注入 userId
    │
    ▼
SecurityFilterChain        ← URL 权限校验（/public/** 放行、/admin/** 需 ROLE_2）
    │
    ▼
Controller                 ← 提取参数/DTO、调用 Service、包装 ResponseModel
    │
    ▼
Service                    ← 业务逻辑、校验、调用 Mapper、组装 VO
    │
    ▼
Mapper + XML               ← MyBatis 动态 SQL → MySQL
    │
    ▼
ResponseModel<VO>          ← 统一 JSON 响应：{ code, message, data }
```

### 安全模型

| URL 前缀 | 认证要求 | 角色要求 |
|---|---|---|
| `/api/public/**` | 无 | 无 |
| `/api/admin/**` | 必须登录 | `ROLE_2`（管理员） |
| 其他 | 必须登录 | 任意已认证用户 |

### 用户身份传递

1. `JwtAuthenticationFilter` 解析 JWT → 提取 userId/role
2. 设置 `SecurityContextHolder.getContext().setAuthentication(...)`
3. 设置 `request.setAttribute("userId", userId)`
4. Controller 通过 `request.getAttribute("userId")` 获取当前用户

### 数据对象流转

```
前端请求 → DTO（入参校验） → Service 处理 → Entity（数据库映射） → VO（脱敏出参） → 前端
```

- **DTO**：接收前端参数，`@Valid` 校验
- **Entity**：与数据库表字段一一对应
- **VO**：返回给前端，已过滤敏感字段（如 password）
- **ResponseModel**：统一包装 `{ code, message, data }`，`code=200` 表示成功

### Redis 用途

- **Token 存储**：JWT 签发后存入 Redis，管理员删除对应 key 即可踢出用户
- **在线用户**：通过 Redis key 模式匹配统计在线用户
- **高可用降级**：Redis 不可用时，JWT 过滤器降级为纯本地验证

### 文件存储

- 使用 S3 兼容协议的对象存储（RustFS）
- `RustFsService` 接口抽象存储操作，`RustFsServiceImpl` 为 S3 实现
- 支持的文件：头像、文章封面、音乐文件、歌词文件
- 更换存储后端只需替换实现类

### AI 聊天

- 使用 Spring AI 框架对接 DeepSeek 模型
- 发送消息时加载完整历史会话 → 构建 System Prompt + 历史消息 → 调用 `chatModel.call()`
- 支持 SSE（Server-Sent Events）流式返回
- 同时预留了 Qwen（通义千问）兼容端点配置

### 软删除

- `tb_music` 和 `tb_playlist` 支持软删除（`deleted` 字段）
- MyBatis-Plus `@TableLogic` 配置自动过滤已删除记录
