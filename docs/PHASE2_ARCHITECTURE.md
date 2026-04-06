# 二期规划：本地写作 → 后台同步 → GitHub Pages 静态发布

> **当前阶段**: 一期博客管理后台 (Headless CMS) 开发中  
> **二期目标**: 串联 "写→管→发" 全链路，实现本地/Web双端写作 + 自动构建发布到 GitHub Pages

---

## 一、你的原始方案分析

### 1.1 你描述的流程
```
本地写 Markdown → 同步到服务器后台系统 → 推送到 GitHub Pages 静态博客
```

### 1.2 这个方案的合理之处

| 点 | 评价 |
|----|------|
| 内容与展示分离 | ✅ 后台只管内容(API)，前端静态站负责渲染，解耦干净 |
| 本地写作体验 | ✅ VS Code + Markdown = 最佳写作体验，不受网络影响 |
| GitHub Pages 免费托管 | ✅ 零成本、全球CDN、自动HTTPS |
| 复用一期成果 | ✅ 后台系统直接作为 Headless CMS 使用 |

### 1.3 存在的问题和风险

#### 问题 ①: "同步" 的定义模糊 — 最大风险点

**你说的"同步到后台"，具体是什么机制？**

| 方案 | 可行性 | 问题 |
|------|--------|------|
| 手动复制粘贴 | 能用但痛苦 | 每次都要打开网页操作 |
| Git push 触发 | 好 | 但后台怎么感知？需要 webhook |
| 文件监听自动上传 | 最好 | 但需要本地代理进程 |
| API 直接推送 | 好 | 需要本地 CLI 工具 |

**建议**: 不要只选一种，设计成**多通道写入，统一存储**。

#### 问题 ②: 图片/资源链路断裂

```
本地Markdown引用: ![img](./images/photo.jpg)
         ↓
后台收到后: 图片在哪？路径对不对？
         ↓
GitHub Pages: 怎么拿到图片？
```

**图片是整个链路最容易断的环节。**

#### 问题 ③: 谁来做 Markdown → HTML 转换？

| 选择 | 优点 | 缺点 |
|------|------|------|
| 后台 Java 服务转 | 统一管理 | Java 做这个不擅长；模板维护麻烦 |
| GitHub Actions CI 构建 | 业界标准做法 | 需要额外配置 pipeline |
| 前端浏览器渲染 | 实时预览方便 | SEO 不友好（GitHub Pages 是纯静态） |
| 本地构建后上传 | 最简单 | 失去了"后台发布"的意义 |

**推荐**: 后台存内容 → 触发 CI/CD → SSG框架构建 → 推送 GitHub Pages

#### 问题 ④: 双端编辑的数据一致性

```
场景: 你在本地VS Code改了文章第3段
      同时在Web后台也改了同一篇文章
      → 冲突怎么处理？
```

#### 问题 ⑤: 预览缺失

```
写完文章 → 同步到后台 → 构建发布 → 打开GitHub Pages看效果
                                    ↑
                              这中间可能要几分钟
                              写作体验很差
```
**缺少"发布前预览"环节。**

---

## 二、改进后的完整架构

### 2.1 最终架构图

```
┌──────────────────────────────────────────────────────────────────┐
│                         内容创作层                                │
│                                                                  │
│  ┌─────────────────────┐    ┌──────────────────────────────┐    │
│  │   A. 本地 VS Code    │    │   B. Web 后台 (Vue3 前端)     │    │
│  │                     │    │                              │    │
│  │  ┌───────────────┐  │    │  ┌────────────────────┐     │    │
│  │  │ Markdown 编辑  │  │    │  │ 富文本/MD编辑器     │     │    │
│  │  │ 图片拖拽粘贴   │  │    │  │ 实时预览           │     │    │
│  │  └───────┬───────┘  │    │  │ 图片上传(粘贴/选择)│     │    │
│  │          │          │    │  └────────┬───────────┘     │    │
│  │          ▼          │    │           │                  │    │
│  │  ┌───────────────┐  │    │           ▼                  │    │
│  │  │ blog-cli 工具  │  │    │  ┌──────────────────┐      │    │
│  │  │ (本地命令行)   │  │    │  │ blog-backend API  │      │    │
│  │  └───────┬───────┘  │    │  └────────┬─────────┘      │    │
│  └──────────┼──────────┘    └──────────┼─────────────────┘    │
│             │                          │                       │
└─────────────┼──────────────────────────┼───────────────────────┘
              │          统一写入         │
              ▼                          ▼
┌───────────────────────────────────────────────────────────────┐
│                    一期: blog-backend (Headless CMS)            │
│                                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ 文章API  │  │ 文件API  │  │ 分类API  │  │ 新增: 发布API │  │
│  │ (已有)   │  │ (已有)   │  │ (已有)   │  │ (二期新增)   │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬───────┘  │
│       │             │            │               │           │
│       ▼             ▼            ▼               ▼           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                   MySQL (内容存储)                      │  │
│  │  t_article(content=markdown原文) + t_file + t_category  │  │
│  └────────────────────────────────────────────────────────┘  │
│                               │                               │
│                    触发构建 / Webhook                         │
└───────────────────────────────┼───────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                    二期新增: 构建发布层                         │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐    │
│  │           SSG 构建引擎 (VitePress 推荐)               │    │
│  │                                                       │    │
│  │  1. 从 blog-backend API 拉取全量文章(JSON)            │    │
│  │  2. Markdown → HTML 渲染 (VitePress 内置)             │    │
│  │  3. 应用自定义主题模板                                 │    │
│  │  4. 生成纯静态文件 (HTML/CSS/JS)                      │    │
│  └──────────────────────┬───────────────────────────────┘    │
│                         │ git push                           │
│                         ▼                                     │
│  ┌──────────────────────────────────────────────────────┐    │
│  │              GitHub Pages (静态站点)                   │    │
│  │    yourname.github.io 或自定义域名                     │    │
│  │    全球 CDN + HTTPS + 自动部署                        │    │
│  └──────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────┘
```

### 2.2 三条数据流详解

#### 流程 A: 本地 VS Code 写作（主力工作流）
```bash
# 1. 本地创建/编辑 Markdown 文件
posts/hello-world.md

# 2. 使用 blog-cli 推送到后台
blog-cli push hello-world.md
# → 解析 frontmatter (title/date/tags/category)
# → 调用 POST /api/article 创建/更新文章
# → 图片自动上传到后台文件服务, 替换本地路径为远程URL

# 3. (可选) 本地预览
blog-cli preview    # 启动本地预览服务器

# 4. 在后台点击"发布"或 CLI 直接触发
blog-cli publish hello-world.md
# → 触发 GitHub Actions 构建
```

#### 流程 B: Web 后台编辑器（补充场景）
```
1. 登录后台 → 文章管理 → 新建/编辑
2. 富文本/Markdown双模式切换
3. 粘贴图片自动上传
4. 实时预览渲染效果
5. 点击"发布"按钮 → 触发构建
```

#### 流程 C: 构建与发布（自动化）
```
后台"发布"按钮 / CLI publish 命令
    ↓
调用 GitHub API 触发 repository_dispatch event
    ↓
GitHub Actions 被唤醒:
    ① checkout SSG项目代码
    ② 调用 blog-backend API 导出全部已发布文章 (JSON)
    ③ VitePress 构建: JSON → 静态HTML页面
    ④ push 到 gh-pages 分支
    ↓
GitHub Pages 自动部署新版本 (通常30秒~2分钟)
```

---

## 三、关键技术决策

### 3.1 为什么推荐 VitePress 而非 Hugo/Hexo？

| 对比项 | VitePress | Hugo | Hexo |
|--------|-----------|------|-----|
| 语言 | Vue 3 | Go | Node.js |
| 与你的技术栈契合度 | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| Markdown 渲染质量 | 优秀 | 优秀 | 良好 |
| 自定义主题难度 | 中等(Vue组件) | 高(Go模板) | 低(EJS/Swig) |
| 构建速度 | 极快(Vite) | 最快 | 中等 |
| 与 Vue3 前端复用 | 可共享组件 | 不能 | 不能 |
| 生态成熟度 | 快速增长 | 成熟 | 成熟 |

**结论**: 你的一期前端如果用 Vue3，VitePress 可以直接复用组件。且 VitePress 支持从远程数据源构建（Content API），天然适合你的架构。

### 3.2 图片存储策略（关键决策）

```
方案对比:

┌──────────┬──────────────────┬──────────────────┬──────────────────┐
│          │ 方案1: 全推OSS    │ 方案2: Git跟踪    │ 方案3: 后台统一  │
├──────────┼──────────────────┼──────────────────┼──────────────────┤
│ 存储     │ 阿里云/OSS       │ GitHub仓库       │ 后台 t_file 表  │
│ 本地体验 │ 需要先上传拿URL   │ 引用相对路径即可  │ CLI自动上传替换  │
│ 成本     │ 少量费用         │ 免费(有大小限制)  │ 取决于服务器带宽  │
│ 维护     │ 额外服务         │ Git仓库膨胀       │ 已有一期基础     │
│ 推荐     │ 生产环境首选     │ 个人博客够用      │ ★ 最省事        │
└──────────┴──────────────────┴──────────────────┴──────────────────┘
```

**推荐方案: 后台统一存储 + CLI自动转换**
- 本地 Markdown 中 `![](images/photo.jpg)` 
- `blog-cli push` 时自动上传图片到后台 → 返回 URL → 替换为 `![](https://yourserver.com/files/xxx.jpg)`
- GitHub Pages 站点只需引用远程URL，不存图片文件

### 3.3 解决双端编辑冲突

| 策略 | 说明 |
|------|------|
| **Last Write Wins** | 简单粗暴，最后保存的覆盖（个人博客够用） |
| **字段级合并** | 标题/标签以某端为准，内容做 diff merge（复杂） |
| **推荐实践** | 明确分工: **本地=主力写作**，**Web端=应急修改/手机端快速编辑**；冲突时提示用户选择 |

---

## 四、二期需要新增什么（在一期基础上）

### 4.1 后台新增模块/接口

| 模块 | 新增接口 | 说明 |
|------|----------|------|
| **blog-publish** (新建) | `POST /api/publish/{articleId}` | 触发单篇文章发布构建 |
| **blog-publish** | `POST /api/publish/batch` | 批量发布 |
| **blog-publish** | `POST /api/publish/full` | 全量重建(所有已发布文章) |
| **blog-publish** | `GET /api/export/articles?status=published` | 导出全部文章给SSG消费 |
| **blog-publish** | `GET /api/export/site-config` | 导出站点配置(名称/描述/友链等) |
| **blog-article** 扩展 | 字段增加 `source` (local/web) | 标记文章来源 |
| **blog-article** 扩展 | 字段增加 `content_hash` | 用于CLI检测文件变化 |
| **blog-file** 扩展 | `POST /api/file/upload-batch` | 批量上传(CLI推送时用) |

### 4.2 新增工具: blog-cli (Node.js/Go)

```
blog-cli/
├── src/
│   ├── push.ts        # 推送文章到后台
│   ├── pull.ts        # 从后台拉取文章到本地
│   ├── publish.ts     # 发布(触发构建)
│   ├── preview.ts     # 本地预览
│   ├── sync.ts        # 双向同步(检测差异)
│   ├── image.ts       # 图片处理(上传+URL替换)
│   └── config.ts      # 配置管理(api地址/token等)
├── package.json
└── README.md
```

### 4.3 新增项目: blog-site (VitePress 静态站)

```
blog-site/                    ← 独立Git仓库
├── .github/workflows/
│   └── build.yml            ← GitHub Actions: 自动构建部署
├── .vitepress/
│   ├── config.ts            ← 站点配置(从API动态获取)
│   ├── theme/
│   │   ├── layouts/         ← 自定义布局(你的UI重写在这里)
│   │   ├── components/      ← 自定义组件
│   │   └── styles/          ← 自定义样式
│   └── build.ts             ← 自定义构建钩子
├── pages/                   ← 动态生成(不需要手写)
├── public/                  ← 静态资源(favicon等)
└── server/
    └── fetch-content.ts     ← 构建时从blog-backend拉取数据
```

### 4.4 GitHub Actions Pipeline

```yaml
# .github/workflows/build.yml
name: Build & Deploy Blog

on:
  repository_dispatch:        # 后台API触发
    types: [publish]
  workflow_dispatch:          # 手动触发
  schedule:
    - cron: '0 */6 * * *'     # 每6小时全量重建(定时)

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20
          
      - name: Fetch content from CMS
        run: |
          # 从 blog-backend API 拉取已发布的文章和配置
          node scripts/fetch-content.js
          
      - name: Install dependencies
        run: npm ci
        
      - name: Build with VitePress
        run: npm run build
        
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./docs
```

---

## 五、完整数据流时序图

### 5.1 本地写作 → 发布 全流程

```
用户(VS Code)          blog-cli              blog-backend          GitHub Actions        GitHub Pages
   │                      │                      │                      │                    │
   │  1.编写hello.md      │                      │                      │                    │
   │  2.blog-cli push     │                      │                      │                    │
   │─────────────────────▶│                      │                      │                    │
   │                      │  3.解析frontmatter    │                      │                    │
   │                      │  4.上传图片           │                      │                    │
   │                      │────────POST─────────▶│                      │                    │
   │                      │                      │  5.保存文章(DB)       │                    │
   │                      │◀──────articleId──────│                      │                    │
   │                      │                      │                      │                    │
   │  6.blog-cli publish  │                      │                      │                    │
   │─────────────────────▶│                      │                      │                    │
   │                      │  7.POST /api/publish │                      │                    │
   │                      │─────────────────────▶│                      │                    │
   │                      │                      │  8.更新状态=已发布    │                    │
   │                      │                      │  9.触发webhook───────│                    │
   │                      │                      │─────────────────────▶│                    │
   │                      │                      │                      │ 10.拉取文章数据     │
   │                      │                      │                      │ 11.VitePress构建    │
   │                      │                      │                      │ 12.push到gh-pages──│
   │                      │                      │                      │───────────────────▶│
   │                      │                      │                      │                    │13.部署完成
   │  14.访问博客看到新文章                                                      │
   │──────────────────────────────────────────────────────────────────────────────▶│
```

### 5.2 Web后台写作 → 发布 流程

```
用户(浏览器)           blog-frontend          blog-backend          GitHub Actions
   │                       │                      │                     │
   │  1.打开编辑器          │                      │                     │
   │  2.编写内容/上传图片   │                      │                     │
   │  3.点击"保存草稿"      │                      │                     │
   │──────────────────────▶│                      │                     │
   │                       │──POST /api/article──▶│                     │
   │                       │◀────articleId────────│                     │
   │  4.实时预览(iframe)   │                      │                     │
   │──────────────────────▶│                      │                     │
   │                       │◀────预览HTML─────────│                     │
   │  5.满意,点击"发布"    │                      │                     │
   │──────────────────────▶│                      │                     │
   │                       │──POST /api/publish──▶│                     │
   │                       │                      │  6.触发webhook──────│
   │                       │                      │────────────────────▶│
   │                       │                      │                     │ 7.构建+部署...
   │  8."发布成功!"通知     │                      │                     │
   │◀──────────────────────│                      │                     │
```

---

## 六、二期开发任务清单 (在一期完成后)

### Phase 2.1: 后台发布模块 (预估 8h)

| 任务 | 说明 | 依赖 |
|------|------|------|
| T2.1-1 新建 blog-publish 模块 | DDD分层, 管理发布状态和触发逻辑 | 一期完成 |
| T2.1-2 发布状态机 | DRAFT→PUBLISHED→DEPLOYED→ONLINE | T2.1-1 |
| T2.1-3 数据导出 API | GET /api/export/* 给SSG消费 | T2.1-1 |
| T2.1-4 GitHub Webhook 触发 | 调用 GitHub API 触发 Actions | T2.1-2 |
| T2.1-5 文章来源标记 | source字段 + content_hash | blog-article扩展 |

### Phase 2.2: blog-cli 工具 (预估 12h)

| 任务 | 说明 |
|------|------|
| T2.2-1 CLI脚手架 | commander.js + 配置文件(~/.blog/config.json) |
| T2.2-2 push 命令 | 解析md → 提取frontmatter → 上传图片 → 调API创建/更新 |
| T2.2-3 pull 命令 | 从API拉取 → 生成本地md文件 |
| T2.2-4 publish 命令 | 触发发布API |
| T2.2-5 preview 命令 | 本地起dev-server预览 |
| T2.2-6 sync 命令 | 双向比对, 显示diff, 选择性同步 |
| T2.2-7 图片自动处理 | 扫描md中的本地图片路径 → 批量上传 → URL替换 |

### Phase 2.3: blog-site 静态站 (预估 16h)

| 任务 | 说明 |
|------|------|
| T2.3-1 VitePress 项目初始化 | 自定义主题骨架 |
| T2.3-2 远程数据源适配 | 构建时从API拉取,生成分页路由 |
| T2.3-3 自定义主题UI | 你的博客界面(后续持续迭代) |
| T2.3-4 页面模板 | 首页/文章页/分类页/标签页/关于页/404 |
| T2.3-5 GitHub Actions 配置 | 自动构建+部署pipeline |
| T2.3-6 SEO优化 | sitemap/robots/meta/structured-data |

### Phase 2.4: 联调与打磨 (预估 8h)

| 任务 | 说明 |
|------|------|
| T2.4-1 全链路联调 | 本地写→push→发布→Pages可见 |
| T2.4-2 Web编辑器集成 | 后台编辑→发布→Pages可见 |
| T2.4-3 错误处理 | 构建失败通知、回滚机制 |
| T2.4-4 性能优化 | 图片懒加载/分页/缓存策略 |

**二期总计: ~44h (约5-6个工作日)**

---

## 七、你可能忽略的点（重要！）

### 7.1 Markdown Frontmatter 规范

你需要约定一个统一的格式，本地和Web端都遵守：

```markdown
---
title: "Hello World"
date: 2026-04-03
category: tech
tags: [java, spring-cloud]
status: published   # draft / published
slug: hello-world   # URL别名
cover: https://...  # 封面图(远程URL)
summary: 这篇文章讲的是...
---

这里是正文内容, 标准 Markdown 语法...
```

**建议**: 这个格式作为 blog-cli 和后台双方的契约。

### 7.2 本地目录结构约定

```
~/blog-content/
├── posts/                    # 文章源文件
│   ├── tech/
│   │   ├── ddd-architecture.md
│   │   └── spring-cloud-gateway.md
│   ├── life/
│   │   └── daily-note.md
│   └── _drafts/              # 草稿(不同步)
│       └── unfinished.md
├── images/                   # 本地图片(push时自动上传)
│   └── 2026/04/
│       └── photo.jpg
├── .blog/                    # CLI配置
│   └── config.toml
└── blog-cli.toml             # 全局配置
```

### 7.3 关于"UI之后重写"

你说静态博客 UI 之后会重写——这没问题，但建议：

1. **先用 VitePress 默认主题跑通全链路**，验证数据流正确
2. **再逐步替换自定义主题**，VitePress 的主题就是 Vue 组件，替换很自然
3. **不要一开始就死磕 UI**，先把"能发布"这件事跑通

### 7.4 域名方案

| 方案 | 地址 | 成本 |
|------|------|------|
| GitHub Pages 默认 | `username.github.io` | 免费 |
| 自定义域名 | `blog.yourdomain.com` | 域名费用(~50元/年) |
| CNAME + Cloudflare | 自定义域名 + CDN加速 | 免费 |

---

## 八、总结: 推荐实施顺序

```
一期(进行中)                二期(一期完成后)
────────────               ────────────
Phase 0-7                  Phase 2.1 → 2.2 → 2.3 → 2.4
环境搭建→功能完成            发布模块→CLI工具→静态站→联调

建议二期节奏:
Day 1-2:  blog-publish 模块 + 数据导出API
Day 3-4:  blog-cli 核心(push/publish/image)
Day 5-7:  blog-site VitePress + 自定义主题
Day 8:    全链路联调 + GitHub Actions
```

**核心原则: 先跑通最短路径 (本地md → API → Pages可见)，再逐步完善。**
