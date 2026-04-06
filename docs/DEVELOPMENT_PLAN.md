# 博客管理后台 - 功能开发计划表

> 基于 PRD 需求文档，按 **依赖关系** 和 **优先级** 排列的开发任务清单

---

## 开发总览

| 阶段 | 名称 | 任务数 | 预估工时 | 产出物 |
|------|------|--------|----------|--------|
| Phase 0 | 环境搭建 | 3 | 4h | 项目可编译启动 |
| Phase 1 | 认证鉴权核心 | 6 | 12h | 登录注册+JWT+RBAC |
| Phase 2 | 用户管理 | 5 | 8h | 用户CRUD+角色分配 |
| Phase 3 | 文章核心(上) | 6 | 14h | 文章CRUD+状态机 |
| Phase 4 | 文章核心(下) | 4 | 8h | 标签+分类+文件上传 |
| Phase 5 | 评论互动 | 4 | 8h | 评论/回复/审核 |
| Phase 6 | 统计与日志 | 4 | 8h | 仪表盘+AOP日志 |
| Phase 7 | 非功能增强 | 5 | 10h | Redis/限流/测试/Docker |

**总计: 37个任务 | 预估 ~72h (约9个工作日)**

---

## Phase 0 - 环境搭建 (前置条件)

### T0.1 基础环境准备
| 属性 | 内容 |
|------|------|
| **优先级** | P0-阻塞 |
| **依赖** | 无 |
| **预估** | 1h |
| **任务内容** | 安装并启动: MySQL 8 → 导入 `data/sql/blog_schema.sql` → Nacos 2.x (:8848) → Redis (可选) |
| **验收标准** | MySQL中 blog_db 库存在且16张表创建成功; Nacos控制台可访问 |

### T0.2 编译验证
| 属性 | 内容 |
|------|------|
| **优先级** | P0-阻塞 |
| **依赖** | T0.1 |
| **预估** | 1h |
| **任务内容** | 根目录执行 `mvn clean compile -DskipTests`, 修复编译错误; 确保7个模块全部通过 |
| **验收标准** | `mvn compile` 输出 BUILD SUCCESS, 0 errors |

### T0.3 服务启动验证
| 属性 | 内容 |
|------|------|
| **优先级** | P0-阻塞 |
| **依赖** | T0.2 |
| **预估** | 2h |
| **任务内容** | 按顺序启动: Nacos → Gateway(:8080) → Auth(:8081) → User(:8082); 验证Nacos服务列表注册成功 |
| **验收标准** | 4个服务在Nacos控制台"服务列表"中显示为UP状态 |

---

## Phase 1 - 认证鉴权核心 (P0)

### T1.1 BCrypt密码加密
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T0.3 |
| **预估** | 1h |
| **模块** | blog-auth / blog-common |
| **任务内容** | 引入 spring-boot-starter-security(仅用BCrypt); 重写 UserDomainService.encodePassword() 使用 BCryptPasswordEncoder; 更新 SQL 初始化数据中的 admin 密码为 BCrypt 加密后的 `admin123` |
| **验收标准** | 注册用户密码存储为 `$2a$10$...` 格式; 登录时密码校验通过 |

### T1.2 JWT Token 完整链路
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T1.1 |
| **预估** | 2h |
| **模块** | blog-auth |
| **任务内容** | 完善 AuthApplicationService.login(): JWT payload 含 {sub:userId, username, roles}; 实现 refresh() 接口; 配置 jwt.secret 从配置中心读取; Token有效期可配 |
| **验收标准** | 登录返回有效Token; 用该Token可解析出userId和username |

### T1.3 网关鉴权过滤器增强
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T1.2 |
| **预估** | 2h |
| **模块** | blog-gateway |
| **任务内容** | 增强 AuthGlobalFilter: 解析JWT提取 userId→写入请求头 `X-User-Id`; 解析 username→`X-Username`; Token过期返回401具体原因; 白名单路径支持配置化 |
| **验收标准** | 无Token访问受保护接口返回401; 有效Token请求头携带用户信息 |

### T1.4 RBAC 数据模型实现
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T0.3 |
| **预估** | 3h |
| **模块** | blog-auth |
| **任务内容** | 创建 Role/Menu 聚合(entity/valueobject/repository/infrastructure四层); RoleApplicationService: 角色CRUD + 分配菜单; MenuApplicationService: 树形菜单CRUD; 初始化默认菜单数据(文章管理/分类管理等) |
| **验收标准** | 可通过接口创建角色、分配权限菜单; 菜单树形结构正确返回 |

### T1.5 用户-角色绑定
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T1.4 |
| **预估** | 2h |
| **模块** | blog-auth |
| **任务内容** | UserRoleRepository (操作 t_user_role); AuthApplicationService 扩展: 登录时查询用户角色列表写入JWT; 提供分配角色接口 `PUT /auth/user/{id}/roles` |
| **验收标准** | 登录返回的JWT中包含roles字段; 可给用户分配/移除角色 |

### T1.6 接口级别权限注解
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T1.5 |
| **预估** | 2h |
| **模块** | blog-common / 各微服务 |
| **任务内容** | 自定义 `@RequirePermission("article:create")` 注解 + AOP切面; 切面从请求头取用户角色→查Redis缓存权限集合→匹配; 无权限抛出 FORBIDDEN 异常 |
| **验收标准** | 无权限用户调用受保护接口返回403; 有权限用户正常访问 |

---

## Phase 2 - 用户管理 (P0)

### T2.1 用户分页列表
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T1.3 |
| **预估** | 1.5h |
| **模块** | blog-user |
| **任务内容** | UserController.list(): 支持 keyword(模糊搜username/nickname/email)、status筛选、分页; 返回 PageResult<UserVO>; UserVO 补充 roleName 字段(需Feign调auth或联表) |
| **验收标准** | GET /api/user/list 返回分页数据; keyword搜索结果准确 |

### T2.2 用户增删改
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T2.1 |
| **预估** | 2h |
| **模块** | blog-user |
| **任务内容** | POST 新增用户(密码自动BCrypt); PUT 编辑(昵称/邮箱/手机/头像); DELETE 逻辑删除; PATCH 启用禁用切换status字段; 所有写操作记录 operation_log |
| **验收标准** | CRUD四个接口均可用; 删除后查询不到(逻辑删除生效) |

### T2.3 重置密码 & 修改密码
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T2.2 |
| **预估** | 1.5h |
| **模块** | blog-user / blog-auth |
| **任务内容** | 管理员重置: PUT /api/user/{id}/reset-password → 重置为 "123456" 的BCrypt; 自助修改: PUT /api/user/password → 校验旧密码→更新新密码 |
| **验收标准** | 重置后可用新密码登录; 修改密码校验旧密码错误时提示 |

### T2.4 当前用户信息接口
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T1.3 |
| **预估** | 1h |
| **模块** | blog-user |
| **任务内容** | GET /api/user/info → 从请求头 X-User-Id 取userId→查库返回完整信息(不含密码); PUT /api/user/info → 修改昵称/头像/个人简介 |
| **验收标准** | 登录后调用info接口返回当前用户信息 |

### T2.5 Feign 远程调用: auth→user
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T2.1 |
| **预估** | 2h |
| **模块** | blog-auth → blog-user |
| **任务内容** | blog-auth 定义 UserFeignClient 接口(@FeignClient("blog-user")); blog-user 提供 internal API (/internal/user/{id}); auth登录/发表文章时远程获取用户名和头像填充VO |
| **验收标准** | auth服务可通过Feign调用user服务获取用户详情 |

---

## Phase 3 - 文章核心·上 (P0 - 核心领域)

### T3.1 文章 CRUD 基础
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T1.3 |
| **预估** | 3h |
| **模块** | blog-article |
| **任务内容** | 完善 ArticleApplicationService: create() 自动注入authorId+初始化时间; update() 校验所有权(作者或管理员); getById() 返回完整ArticleVO(含categoryName); delete() 已发布不可删校验; content字段支持Markdown长文本 |
| **验收标准** | 文章增删改查四个接口全部可用; 非作者编辑返回403 |

### T3.2 文章状态机
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T3.1 |
| **预估** | 2h |
| **模块** | blog-article (domain层) |
| **任务内容** | Article实体增加状态流转方法: publish()/withdraw()/offline(); ApplicationService暴露 publish(id)/withdraw(id) 接口; 状态变更时自动设置publishTime; 不合法状态转换抛异常 |
| **验收标准** | 草稿可发布; 已发布可撤回/下架; 已下架可重新发布; 状态机不可逆操作报错 |

### T3.3 文章分页搜索
| 属性 | 内容 |
|------|------|
| **优先级** | P0 |
| **依赖** | T3.1 |
| **预估** | 2h |
| **模块** | blog-article |
| **任务内容** | list() 接口扩展: categoryId精确筛选; status筛选; keyword模糊搜索(title+content, 利用FULLTEXT索引); 排序策略(置顶优先→发布时间倒序); 返回 PageResult<ArticleVO> |
| **验收标准** | 多条件组合搜索结果正确; 关键词搜索命中标题和内容 |

### T3.4 文章置顶功能
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T3.2 |
| **预估** | 1h |
| **模块** | blog-article |
| **任务内容** | PATCH /api/article/{id}/top?top=0\|1; 设置isTop字段; 列表查询时 isTop=1 的记录排在最前 |
| **验收标准** | 置顶文章在列表顶部展示; 取消置顶后恢复普通排序 |

### T3.5 浏览计数 (防刷)
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T3.1 |
| **预估** | 2h |
| **模块** | blog-article |
| **任务内容** | POST /api/article/{id}/view; 防刷策略: 同一IP+同一文章5分钟内只计1次; view_count +1 (SQL UPDATE ... SET view_count = view_count + 1); 后续优化: Redis incr 后定时刷DB |
| **验收标准** | 调用view接口浏览量+1; 短时间重复调用不重复计数 |

### T3.6 点赞功能
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T3.1 |
| **预估** | 2h |
| **模块** | blog-article |
| **任务内容** | POST /api/article/{id}/like; 查 t_article_like 表判断是否已点赞; 未点赞→插入+article.like_count+1; 已点赞→删除+article.like_count-1(toggle模式); userId+articleId联合唯一索引防重复 |
| **验收标准** | 首次调用点赞数+1; 再次调用取消点赞数-1; 重复点赞不会出错 |

---

## Phase 4 - 文章核心·下 (P1)

### T4.1 分类树 CRUD
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T0.3 |
| **预估** | 3h |
| **模块** | blog-category |
| **任务内容** | CategoryApplicationService.getCategoryTree(): 查全表→内存构建递归树; create() 校验父分类存在; delete() 校验无子分类+articleCount=0; 最多支持3级(parentId递归深度校验); slug唯一性校验 |
| **验收标准** | 分类树层级正确; 父子关系无误; 删除有子分类的分类时报明确错误 |

### T4.2 标签管理
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T0.3 |
| **预估** | 2h |
| **模块** | blog-article (或独立blog-tag) |
| **任务内容** | Tag聚合(entity/repository/impl/mapper); TagApplicationService: CRUD + hot标签(按articleCount排序); name唯一约束; 创建/编辑文章时可关联多个标签(操作t_article_tag) |
| **验收标准** | 标签CRUD正常; 热门标签按使用频率排序; 文章可关联多标签 |

### T4.3 文件上传 (本地存储)
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T0.3 |
| **预估** | 3h |
| **模块** | blog-article 或 blog-file |
| **任务内容** | FileUploadController: MultipartFile接收; 校验文件类型(image: jpg/png/gif/webp; file: 限制扩展名白名单); 校验大小(图片≤5MB, 文件≤20MB); 按 `uploads/{yyyy/MM/dd}/{uuid}.{ext}` 存储本地; 返回可访问URL; 记录t_file表 |
| **验收标准** | 上传图片返回URL可直接访问; 超大文件/非法格式被拒绝 |

### T4.4 文章与分类/标签联动
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T4.1, T4.2 |
| **预估** | 2h |
| **模块** | blog-article |
| **任务内容** | 创建文章时categoryId→category.articleCount+1; 删除文章时-1; 创建文章时同步写入t_article_tag; 删除文章时清理t_article_tag; 删除标签时校验t_article_tag无关联才允许 |
| **验收标准** | 文章数量与分类统计一致; 删除分类/标签时有完整性校验 |

---

## Phase 5 - 评论互动 (P1)

### T5.1 发表评论
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T1.3 |
| **预估** | 2h |
| **模块** | blog-comment |
| **任务内容** | POST /api/comment: articleId+content必填; parentId=0(顶层评论); 自动从请求头取userId; status默认PENDING(待审核); 敏感词过滤(可选,简单关键词替换); comment_count +1 到 t_article |
| **验收标准** | 登录用户可发表评论; 评论关联到正确文章 |

### T5.2 楼中楼回复
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T5.1 |
| **预估** | 2h |
| **模块** | blog-comment |
| **任务内容** | POST /api/comment/reply: parentId(父评论id)+replyToId(被回复的评论id)必填; 校验父评论存在; replyToUserId自动填充; 支持无限层级回复(建议最多3层) |
| **验收标准** | 回复显示正确的父子关系; @某人 信息正确 |

### T5.3 评论审核
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T5.1 |
| **预估** | 1.5h |
| **模块** | blog-comment |
| **任务内容** | PATCH /api/comment/{id}/audit?status=1\|2; 状态机: PENDING→APPROVED/REJECTED; 仅管理员/博主可操作; 审核通过后前端可见 |
| **验收标准** | 待审核评论审核后状态正确变更; 非管理员操作返回403 |

### T5.4 评论列表(树形)
| 属性 | 内容 |
|------|------|
| **优先级** | P1 |
| **依赖** | T5.2 |
| **预估** | 2h |
| **模块** | blog-comment |
| **任务内容** | GET /api/comment?articleId=: 按文章ID查顶层评论(分页); 每条顶层评论携带children(最近3条回复); 通过Feign补全username/avatar; 支持按status筛选(管理员视角) |
| **验收标准** | 评论列表展示正确的楼中楼结构; 用户名和头像正确显示 |

---

## Phase 6 - 统计与日志 (P2)

### T6.1 AOP 操作日志
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T0.3 |
| **预估** | 2h |
| **模块** | blog-common |
| **任务内容** | @LogAnnotation 注解(module, operation); AOP切面拦截: 记录方法参数、返回值、耗时、操作人IP; 异常时记录error_msg; 异步写入 t_operation_log (用@Async避免影响主流程) |
| **验收标准** | 所有带@LogAnnotation的接口自动记录日志; 日志包含完整上下文 |

### T6.2 登录日志
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T1.2 |
| **预估** | 1.5h |
| **模块** | blog-auth |
| **任务内容** | AuthApplicationService.login() 成功/失败均写 t_login_log; 记录: username/ip/browser/os/status/message); 失败次数超限可触发告警(预留) |
| **验收标准** | 每次登录尝试均有日志记录; 包含IP和浏览器信息 |

### T6.3 数据仪表盘
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T3.3, T5.4 |
| **预估** | 3h |
| **模块** | blog-article (DashboardApplicationService) |
| **任务内容** | overview: SELECT COUNT各表 + SUM(view_count); trend: 按天GROUP BY近N天发文/访问量; hotArticles: TOP10 ORDER BY view_count; latestComments: 最新10条; categoryStats: 各分类文章数占比; 使用DTO聚合多表数据 |
| **验收标准** | 5个仪表盘接口均返回正确统计数据 |

### T6.4 系统配置管理
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T0.3 |
| **预估** | 1.5h |
| **模块** | blog-auth 或独立 blog-system |
| **任务内容** | SystemConfigApplicationService: getByKey/setValue/getByGroup; GET/PUT /api/system/config; 友情链接CRUD: /api/system/friend-link/**; 配置变更后刷新缓存 |
| **验收标准** | 可读写系统配置; 友情链接增删改查正常 |

---

## Phase 7 - 非功能增强 (P2)

### T7.1 Redis 缓存集成
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T4.1, T4.2 |
| **预估** | 3h |
| **模块** | 全局 |
| **任务内容** | 引入 spring-data-redis; 缓存策略: 分类树(30min)、热门标签(10min)、站点配置(30min)、用户权限(与Token同效); 使用 @Cacheable / @CacheEvict; 缓存穿透防护(空值缓存) |
| **验收标准** | 缓存命中时响应<50ms; 数据变更后缓存自动失效 |

### T7.2 Sentinel 接口限流
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T0.3 |
| **预估** | 2h |
| **模块** | blog-gateway |
| **任务内容** | 引入 spring-cloud-starter-alibaba-sentinel; 网关层配置限流规则: 全局限流200QPS; 登录接口20QPS(防暴力破解); 限流返回 429 Too Many Requests; Sentinel Dashboard 可视化 |
| **验收标准** | 超过QPS限制返回429; 登录接口被单独限流 |

### T7.3 Knife4j 接口文档
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T3.1 |
| **预估** | 2h |
| **模块** | 全局(各微服务) |
| **任务内容** | 引入 knife4j-openapi3; 各Controller补充 @Tag/@Operation/@Parameter 注解; DTO补充 @Schema 描述; 网关聚合文档入口; 生产环境禁用 |
| **验收标准** | 访问 /doc.html 可查看所有接口文档; 支持在线调试 |

### T7.4 单元测试
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | T3.1, T2.2 |
| **预估** | 3h |
| **模块** | blog-auth, blog-user, blog-article |
| **任务内容** | JUnit 5 + Mockito; 重点覆盖: ApplicationService 层业务逻辑; Domain层状态机; Converter转换正确性; 目标覆盖率 > 60% |
| **验收标准** | `mvn test` 全部通过; 核心业务逻辑有测试覆盖 |

### T7.5 Docker Compose 一键部署
| 属性 | 内容 |
|------|------|
| **优先级** | P2 |
| **依赖** | 全部完成 |
| **预估** | 2h |
| **模块** | 项目根目录 |
| **任务内容** | docker-compose.yml: MySQL + Nacos + Redis + 各应用服务; 各服务 Dockerfile(mvn package → java -jar); 环境变量外部化(.env文件); docker-compose up -d 一键启动全套环境 |
| **验收标准** | `docker-compose up -d` 后全部服务正常运行 |

---

## 任务依赖关系图

```
T0.1 ──▶ T0.2 ──▶ T0.3 ─┬──▶ T1.1 ──▶ T1.2 ──▶ T1.3 ─┬──▶ T2.1 ──▶ T2.2 ──▶ T2.3
                         │                        │              │              │
                         │                        │              ├──▶ T2.4      ├──▶ T2.5
                         │                        │              │
                         │                        ├──▶ T1.4 ──▶ T1.5 ──▶ T1.6
                         │                        │
                         ├──▶ T1.4               │
                         │                        ├──▶ T3.1 ──┬──▶ T3.2 ──▶ T3.4
                         │                        │           ├──▶ T3.3
                         │                        │           ├──▶ T3.5
                         │                        │           └──▶ T3.6
                         │                        │
                         ├──▶ T4.1 ──────────────▶ T4.4 ◀─── T4.2
                         │        ▲                      
                         │        │                      
                         ├──▶ T4.2 ┘                      
                         │                                  
                         ├──▶ T4.3                          
                         │                                  
                         ├──▶ T5.1 ──▶ T5.2 ──▶ T5.4       
                         │              │                  
                         │              └──▶ T5.3           
                         │                                  
                         ├──▶ T6.1                          
                         ├──▶ T1.2 ──▶ T6.2              
                         ├──▶ T3.3 + T5.4 ──▶ T6.3        
                         └──▶ T6.4                          

Phase 7 (T7.1~T7.5) 无强依赖, 可并行
```

---

## 每日开发建议节奏

| 天数 | 上午 (4h) | 下午 (4h) | 当日产出 |
|------|-----------|-----------|----------|
| Day 1 | T0.1~T0.3 环境搭建 | T1.1~T1.2 密码+JWT | 项目跑通+能登录 |
| Day 2 | T1.3 网关鉴权 | T1.4 RBAC模型 | 鉴权链路打通 |
| Day 3 | T1.5~T1.6 权限注解 | T2.1~T2.2 用户CRUD | 权限体系+用户管理 |
| Day 4 | T2.3~T2.5 密码+Feign | T3.1~T3.2 文章CRUD+状态机 | 文章核心可用 |
| Day 5 | T3.3~T3.4 搜索+置顶 | T3.5~T3.6 浏览+点赞 | 文章功能完善 |
| Day 6 | T4.1~T4.2 分类+标签 | T4.3~T4.4 上传+联动 | 辅助功能完成 |
| Day 7 | T5.1~T5.3 评论+审核 | T5.4 评论列表树 | 互动功能完成 |
| Day 8 | T6.1~T6.2 AOP日志 | T6.3~T6.4 仪表盘+配置 | 统计系统完成 |
| Day 9 | T7.1~T7.3 缓存+限流+文档 | T7.4~T7.5 测试+Docker | 项目交付就绪 |
