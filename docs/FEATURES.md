# 博客管理后台 - 完整功能清单

## 一、用户认证与权限模块 (Auth & RBAC)

| 功能 | 说明 | 接口 |
|------|------|------|
| 用户登录 | 用户名/密码登录，返回JWT Token | POST /auth/login |
| 用户注册 | 新用户注册，含邮箱验证 | POST /auth/register |
| 退出登录 | Token失效/黑名单处理 | POST /auth/logout |
| 刷新Token | Token续期机制 | POST /auth/refresh |
| 角色管理 | CRUD角色(管理员/编辑/普通用户) | /api/user/role/** |
| 权限管理 | 菜单权限+按钮权限配置 | /api/user/permission/** |
| 用户-角色绑定 | 分配用户角色 | PUT /api/user/{id}/roles |

## 二、用户管理模块 (User)

| 功能 | 说明 | 接口 |
|------|------|------|
| 用户列表 | 分页查询、条件筛选 | GET /api/user/list |
| 用户详情 | 查看用户完整信息 | GET /api/user/{id} |
| 新增用户 | 管理员手动添加用户 | POST /api/user |
| 编辑用户 | 修改用户信息 | PUT /api/user/{id} |
| 删除用户 | 逻辑删除用户 | DELETE /api/user/{id} |
| 启用/禁用 | 冻结/解冻账号 | PATCH /api/user/{id}/status |
| 重置密码 | 管理员重置用户密码 | PUT /api/user/{id}/reset-password |
| 修改个人信息 | 当前用户修改昵称/头像等 | PUT /api/user/info |
| 修改密码 | 当前用户修改密码 | PUT /api/user/password |

## 三、文章管理模块 (Article) — 核心领域

| 功能 | 说明 | 接口 |
|------|------|------|
| 文章列表 | 分页、按分类/状态/关键词搜索 | GET /api/article |
| 文章详情 | 查看文章详情(Markdown渲染) | GET /api/article/{id} |
| 创建文章 | 富文本/Markdown编辑器 | POST /api/article |
| 编辑文章 | 更新文章内容 | PUT /api/article/{id} |
| 删除文章 | 逻辑删除 | DELETE /api/article/{id} |
| 发布文章 | 草稿→发布状态流转 | POST /api/article/{id}/publish |
| 撤回发布 | 发布→草稿状态回退 | POST /api/article/{id}/withdraw |
| 文章置顶 | 设置/取消置顶 | PATCH /api/article/{id}/top |
| 批量操作 | 批量删除/批量发布 | POST /api/article/batch |
| 浏览计数 | 文章阅读量统计(防刷) | POST /api/article/{id}/view |
| 点赞/取消赞 | 文章点赞功能 | POST /api/article/{id}/like |

## 四、分类管理模块 (Category)

| 功能 | 说明 | 接口 |
|------|------|------|
| 分类列表 | 树形结构展示 | GET /api/category/tree |
| 分类详情 | 查看分类信息 | GET /api/category/{id} |
| 新增分类 | 支持父子层级关系 | POST /api/category |
| 编辑分类 | 修改分类名称/排序/图标 | PUT /api/category/{id} |
| 删除分类 | 校验下无子分类和文章才可删 | DELETE /api/category/{id} |
| 排序调整 | 拖拽排序 | PATCH /api/category/sort |

## 五、标签管理模块 (Tag)

| 功能 | 说明 | 接口 |
|------|------|------|
| 标签列表 | 分页查询 | GET /api/tag |
| 标签搜索 | 关键词模糊搜索 | GET /api/tag/search |
| 新增标签 | 创建标签 | POST /api/tag |
| 编辑标签 | 修改标签名 | PUT /api/tag/{id} |
| 删除标签 | 校验无关联文章才可删 | DELETE /api/tag/{id} |
| 热门标签 | 按使用频率排序 | GET /api/tag/hot |

## 六、评论管理模块 (Comment)

| 功能 | 说明 | 接口 |
|------|------|------|
| 评论列表 | 分页、按文章/状态筛选 | GET /api/comment |
| 发表评论 | 发表评论(支持回复/楼中楼) | POST /api/comment |
| 回复评论 | 评论的回复 | POST /api/comment/reply |
| 删除评论 | 删除评论及其回复 | DELETE /api/comment/{id} |
| 审核/通过 | 评论审核流程 | PATCH /api/comment/{id}/audit |
| 我的评论 | 当前用户的评论记录 | GET /api/comment/mine |

## 七、文件上传模块 (File/OSS)

| 功能 | 说明 | 接口 |
|------|------|------|
| 图片上传 | Markdown/封面图上传 | POST /api/file/upload/image |
| 文件上传 | 附件上传 | POST /api/file/upload/file |
| 文件列表 | 已上传文件管理 | GET /api/file/list |
| 删除文件 | 删除服务器文件 | DELETE /api/file/{id} |

## 八、系统设置模块 (System Config)

| 功能 | 说明 | 接口 |
|------|------|------|
| 站点基本信息 | 博客名称/描述/Logo/备案号 | GET/PUT /api/system/site |
| 社交链接配置 | GitHub/微信/QQ等 | GET/PUT /api/system/social |
| SEO设置 | 关键词/描述 | GET/PUT /api/system/seo |
| 友情链接管理 | 友链CRUD | /api/system/friend-link/** |

## 九、仪表盘/数据统计模块 (Dashboard)

| 功能 | 说明 | 接口 |
|------|------|------|
| 数据概览 | 文章数/评论数/用户数/浏览量 | GET /api/dashboard/overview |
| 趋势图表 | 近7天/30天发文/访问趋势 | GET /api/dashboard/trend |
| 热门文章TOP10 | 阅读量排行 | GET /api/dashboard/hot-articles |
| 最新评论 | 最新10条评论 | GET /api/dashboard/latest-comments |
| 分类占比 | 各分类文章数量分布 | GET /api/dashboard/category-stats |

## 十、操作日志模块 (Audit Log)

| 功能 | 说明 | 接口 |
|------|------|------|
| 操作日志列表 | 记录所有关键操作 | GET /api/log/operation |
| 登录日志 | 记录登录/登出行为 | GET /api/log/login |
| 异常日志 | 系统异常记录 | GET /api/log/error |

---

## 技术架构要点

```
┌─────────────┐    ┌──────────────┐    ┌──────────────────┐
│   Gateway   │───▶│   Nacos      │───▶│  微服务集群       │
│   :8080     │    │  注册/配置中心 │    │                  │
└─────────────┘    └──────────────┘    │  blog-auth  :8081 │
                                       │  blog-user  :8082 │
                                       │  blog-article:8083│
                                       │  blog-category:8084│
                                       │  blog-comment:8085│
                                       └──────────────────┘

DDD分层: interfaces → application → domain → infrastructure
```
