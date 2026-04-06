# 博客管理后台 PRD 产品需求文档

> **版本**: v1.0  
> **技术栈**: Java 17 + Spring Cloud (2023.0.1) + Spring Boot 3.2 + MyBatis-Plus + Nacos + MySQL 8  
> **架构模式**: DDD (领域驱动设计) 分层架构  
> **文档日期**: 2026-04-03

---

## 一、项目概述

### 1.1 项目定位
一个面向个人/小团队的博客内容管理系统（CMS）后端，采用微服务架构，支持多用户、RBAC权限控制、文章发布审核、评论互动等完整博客生态功能。

### 1.2 目标用户
| 角色 | 说明 |
|------|------|
| 超级管理员 | 系统全局配置、用户管理、角色权限分配 |
| 博主(编辑) | 文章CRUD、分类标签管理、评论审核 |
| 普通用户 | 浏览文章、发表评论、修改个人资料 |

### 1.3 核心价值
- **内容创作友好**: Markdown编辑、草稿自动保存、一键发布
- **权限精细可控**: RBAC模型，菜单+按钮两级权限
- **架构可扩展**: DDD分层 + 微服务，易于迭代新业务

---

## 二、系统架构

### 2.1 技术架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│              前端(Vue3/React) / 移动端 / 第三方               │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/HTTPS
┌──────────────────────▼──────────────────────────────────────┐
│                     API 网关层                               │
│   blog-gateway (:8080)                                       │
│   └─ 路由转发 │ JWT鉴权 │ 白名单 │ 限流熔断                    │
└───┬───────────┬───────────┬───────────┬───────────┬─────────┘
    │           │           │           │           │
┌───▼───┐ ┌───▼───┐ ┌────▼───┐ ┌────▼───┐ ┌────▼───┐
│ auth  │ │ user  │ │article │ │category│ │comment │
│ :8081 │ │ :8082 │ │ :8083  │ │ :8084  │ │ :8085  │
└───────┘ └───────┘ └────────┘ └────────┘ └────────┘

┌─────────────────────────────────────────────────────────────┐
│                      基础设施层                              │
│  Nacos(:8848) │ MySQL(:3306) │ Redis(:6379) │ RocketMQ     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 服务职责划分

| 服务 | 职责 | 数据库表 |
|------|------|----------|
| blog-gateway | 统一入口、路由、鉴权过滤 | 无 |
| blog-auth | 登录/注册/JWT签发/Token刷新 | t_user, t_role, t_menu, t_user_role, t_role_menu |
| blog-user | 用户信息管理/角色分配 | t_user |
| blog-article | 文章全生命周期管理 | t_article, t_article_tag, t_article_like, t_tag, t_file |
| blog-category | 分类树形管理 | t_category |
| blog-comment | 评论/回复/审核 | t_comment |

### 2.3 DDD 分层规范

```
模块名/
├── interfaces/          ← 接口层 (Controller + DTO)
│   ├── facade/          REST控制器
│   └── dto/             请求/响应对象
├── application/         ← 应用层 (编排用例)
│   └── *ApplicationService.java
├── domain/              ← 领域层 (核心业务逻辑)
│   └── {聚合名}/
│       ├── entity/      聚合根实体
│       ├── valueobject/ 值对象/枚举
│       ├── repository/  仓储接口(端口)
│       └── service/     领域服务(跨实体逻辑)
└── infrastructure/      ← 基础设施层 (技术实现)
    └── persistence/
        ├── po/          持久化对象(DB映射)
        ├── converter/   Entity ↔ PO 转换器
        ├── mapper/      MyBatis Mapper接口
        └── *RepositoryImpl.java  仓储实现(适配器)
```

---

## 三、功能需求详述

### 3.1 用户认证与授权 (P0 - 最高优先级)

#### 3.1.1 登录
| 字段 | 规则 |
|------|------|
| 接口 | `POST /auth/login` |
| 入参 | username, password |
| 出参 | token(Bearer), expireIn(秒) |
| 业务规则 | 密码BCrypt校验; JWT包含userId+username; Token有效期2h |

#### 3.1.2 注册
| 字段 | 规则 |
|------|------|
| 接口 | `POST /auth/register` |
| 入参 | username(唯一), password(≥6位), email(唯一) |
| 出参 | 无(201) |
| 业务规则 | 默认角色=USER; 密码BCrypt加密存储 |

#### 3.1.3 Token刷新
| 字段 | 规则 |
|------|------|
| 接口 | `POST /auth/refresh` |
| 入参 | Header: Authorization (旧Token) |
| 出参 | 新Token |
| 业务规则 | 仅在Token过期前30分钟内允许刷新 |

#### 3.1.4 RBAC 权限模型
```
用户(User) N:N 角色(Role) N:N 菜单(Menu)

t_role: SUPER_ADMIN / BLOGGER / USER
t_menu: 三级树形结构 (目录→菜单→按钮)
t_menu.menu_type: 1=目录 2=菜单 3=按钮
t_menu.perms: 权限标识, 如 "article:create"
```

### 3.2 用户管理 (P0)

| 功能 | 接口 | 说明 |
|------|------|------|
| 用户列表 | `GET /api/user/list?current=1&size=10&keyword=&status=` | 分页+搜索+状态筛选 |
| 用户详情 | `GET /api/user/{id}` | 返回完整信息含角色列表 |
| 新增用户 | `POST /api/user` | 管理员手动创建 |
| 编辑用户 | `PUT /api/user/{id}` | 修改昵称/邮箱/手机/头像 |
| 删除用户 | `DELETE /api/user/{id}` | 逻辑删除 |
| 启用/禁用 | `PATCH /api/user/{id}/status?status=0|1` | 冻结账号 |
| 重置密码 | `PUT /api/user/{id}/reset-password` | 重置为默认密码 |
| 分配角色 | `PUT /api/user/{id}/roles` | Body: [roleId1, roleId2] |
| 我的资料 | `GET /api/user/info` | 当前登录用户信息 |
| 修改密码 | `PUT /api/user/password` | 校验旧密码后更新 |

### 3.3 文章管理 (P0 - 核心领域)

#### 3.3.1 文章状态机
```
        ┌────────┐  publish()   ┌──────────┐  offline()  ┌────────┐
  创建 → │ 草稿  │ ──────────▶  │ 已发布   │ ──────────▶ │ 已下架 │
        └────────┘  ◀─────────  └──────────┘             └────────┘
                       withdraw()
```

| 状态值 | 含义 | 可执行操作 |
|--------|------|-----------|
| 0(DRAFT) | 草稿 | 编辑/发布/删除 |
| 1(PUBLISHED) | 已发布 | 下架/置顶/点赞/浏览 |
| 2(OFFLINE) | 已下架 | 重新发布/删除 |

#### 3.3.2 接口清单

| 功能 | 接口 | 说明 |
|------|------|------|
| 文章列表 | `GET /api/article?current=&size=&categoryId=&status=&keyword=` | 多条件分页 |
| 文章详情 | `GET /api/article/{id}` | 含作者信息+分类名+标签 |
| 创建文章 | `POST /api/article` | 自动填充authorId |
| 更新文章 | `PUT /api/article/{id}` | 仅作者或管理员可操作 |
| 删除文章 | `DELETE /api/article/{id}` | 已发布文章不可删 |
| 发布文章 | `POST /api/article/{id}/publish` | 草稿→已发布 |
| 撤回发布 | `POST /api/article/{id}/withdraw` | 已发布→草稿 |
| 置顶/取消 | `PATCH /api/article/{id}/top?top=0\|1` | isTop字段 |
| 浏览计数 | `POST /api/article/{id}/view` | 防刷(IP+时间窗口) |
| 点赞/取消 | `POST /api/article/{id}/like` | 防重复(userId去重) |

### 3.4 分类管理 (P1)

| 功能 | 接口 | 说明 |
|------|------|------|
| 分类树 | `GET /api/category/tree` | 递归返回父子层级 |
| 分类详情 | `GET /api/category/{id}` | 单个分类信息 |
| 新增分类 | `POST /api/category` | 支持指定parentId |
| 编辑分类 | `PUT /api/category/{id}` | 名称/描述/图标/排序 |
| 删除分类 | `DELETE /api/category/{id}` | 校验无子分类+无关联文章 |
| 排序调整 | `PATCH /api/category/sort` | 批量更新sortOrder |

**数据约束:**
- 支持最多3级分类
- slug全局唯一(URL别名)
- articleCount冗余字段, 文章增删时联动更新

### 3.5 标签管理 (P1)

| 功能 | 接口 | 说明 |
|------|------|------|
| 标签列表 | `GET /api/tag?current=&size=&keyword=` | 分页+模糊搜索 |
| 新增标签 | `POST /api/tag` | name唯一 |
| 编辑标签 | `PUT /api/tag/{id}` | 修改名称 |
| 删除标签 | `DELETE /api/tag/{id}` | 校验无关联文章 |
| 热门标签 | `GET /api/tag/hot?limit=10` | 按articleCount降序 |

**与文章关系:** 通过 t_article_tag 多对多关联

### 3.6 评论管理 (P1)

| 功能 | 接口 | 说明 |
|------|------|------|
| 评论列表 | `GET /api/comment?articleId=&status=&current=&size=` | 分页筛选 |
| 发表评论 | `POST /api/comment` | 顶层评论(parentId=0) |
| 回复评论 | `POST /api/comment/reply` | 楼中楼(replyToId) |
| 删除评论 | `DELETE /api/comment/{id}` | 级联删除子评论 |
| 审核通过 | `PATCH /api/comment/{id}/audit?status=1` | 状态流转 |
| 审核拒绝 | `PATCH /api/comment/{id}/audit?status=2` | 状态流转 |
| 我的评论 | `GET /api/comment/mine` | 当前用户的评论记录 |

**评论状态机:**
```
待审核(0) ──审批通过──▶ 已通过(1)
待审核(0) ──审批拒绝──▶ 已拒绝(2)
```

### 3.7 文件上传 (P2)

| 功能 | 接口 | 说明 |
|------|------|------|
| 图片上传 | `POST /api/file/upload/image` | MultipartFile; 限制5MB; jpg/png/gif/webp |
| 文件上传 | `POST /api/file/upload/file` | MultipartFile; 限制20MB |
| 文件列表 | `GET /api/file/list?bizType=&page=` | 按业务类型分页 |
| 删除文件 | `DELETE /api/file/{id}` | 同时删除物理文件 |

**存储策略:** 默认本地存储 `/uploads/{date}/`; 可切换阿里OSS

### 3.8 系统设置 (P2)

| 配置组 | Key | 说明 |
|--------|-----|------|
| site | name/description/logo/icp | 站点基本信息 |
| seo | keywords/description | SEO元信息 |
| social | github/wechat/qq | 社交链接 |
| upload | max-size/storage-type | 上传配置 |

**友情链接 CRUD:** `t_friend_link` 表独立管理

### 3.9 数据仪表盘 (P2)

| 功能 | 接口 | 数据来源 |
|------|------|----------|
| 总览卡片 | `GET /api/dashboard/overview` | COUNT各表 + SUM浏览量 |
| 趋势图 | `GET /api/dashboard/trend?days=7&metric=article/view` | 按天GROUP BY |
| 热门文章TOP10 | `GET /api/dashboard/hot-articles` | ORDER BY view_count DESC |
| 最新评论 | `GET /api/dashboard/latest-comments` | ORDER BY create_time DESC |
| 分类统计 | `GET /api/dashboard/category-stats` | 各分类文章数占比 |

### 3.10 操作日志 (P2)

| 功能 | 接口 | 说明 |
|------|------|------|
| 操作日志列表 | `GET /api/log/operation?module=&operator=&dateRange=` | AOP自动记录 |
| 登录日志列表 | `GET /api/log/login?username=&status=&dateRange=` | 登录成功/失败记录 |

**AOP日志切点:** 所有 Controller 的 @PostMapping/@PutMapping/@DeleteMapping

---

## 四、非功能性需求

### 4.1 性能要求
| 指标 | 要求 |
|------|------|
| 接口响应(P99) | < 500ms (不含网关) |
| 并发支持 | 单服务 500 QPS |
| 文章列表查询 | 必须走索引, 支持 MySQL 10万+数据量 |
| 热点数据 | 分类树/热门标签/站点配置 走Redis缓存 |

### 4.2 安全要求
| 项目 | 方案 |
|------|------|
| 密码存储 | BCrypt加密 (cost=10) |
| 接口认证 | JWT Bearer Token |
| SQL注入 | MyBatis-Plus 参数化查询 |
| XSS防护 | 入参转义 + Content-Type限制 |
| 接口限流 | Sentinel 网关层限流 (100QPS/用户) |

### 4.3 数据约束
| 约束 | 说明 |
|------|------|
| 逻辑删除 | 全局 deleted 字段, 不做物理删除 |
| 乐观锁 | 文章/评论更新时用 version 字段防并发 |
| 外键替代 | 应用层校验关联关系, 不使用DB外键 |
| 字符集 | 全库 utf8mb4, 支持emoji |

---

## 五、数据库设计概要

详见 `data/sql/blog_schema.sql`, 共16张核心表:

```
用户域:  t_user, t_role, t_menu, t_user_role, t_role_menu
内容域:  t_article, t_article_tag, t_tag, t_category
交互域:  t_comment, t_article_like
系统域:  t_file, t_system_config, t_friend_link
日志域:  t_operation_log, t_login_log
```

---

## 六、验收标准 (Definition of Done)

每个功能开发完成需满足:

- [ ] 接口可正常调用, 返回符合 Result<T> 规范
- [ ] 参数校验完整 (@Valid + ErrorCode)
- [ ] 异常处理覆盖 (GlobalExceptionHandler 兜底)
- [ ] DDD 分层清晰 (domain 层不依赖 framework)
- [ ] 有对应的单元测试 (ApplicationService 层)
- [ ] Swagger/Knife4j 文档可访问
