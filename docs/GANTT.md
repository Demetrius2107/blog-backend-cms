# 博客管理后台 - 开发甘特图

> **总工期**: 9个工作日 | **总任务数**: 37个 | **技术栈**: DDD + Spring Cloud

```mermaid
gantt
    title 博客管理后台开发甘特图 (9-Day Sprint)
    dateFormat  YYYY-MM-DD
    axisFormat  %m/%d

    section Phase 0 环境搭建
    T0.1 基础环境准备          :done,    p0_1, 2026-04-06, 1d
    T0.2 编译验证              :done,    p0_2, after p0_1, 1d
    T0.3 服务启动验证          :active,  p0_3, after p0_2, 2d

    section Phase 1 认证鉴权核心 (P0)
    T1.1 BCrypt密码加密        :         p1_1, after p0_3, 1d
    T1.2 JWT完整链路           :         p1_2, after p1_1, 2d
    T1.3 网关鉴权过滤器增强     :crit,    p1_3, after p1_2, 2d
    T1.4 RBAC数据模型实现      :         p1_4, after p0_3, 3d
    T1.5 用户-角色绑定         :         p1_5, after p1_4, 2d
    T1.6 接口权限注解(AOP)     :         p1_6, after p1_5, 2d

    section Phase 2 用户管理 (P0)
    T2.1 用户分页列表          :         p2_1, after p1_3, 1d
    T2.2 用户增删改            :crit,    p2_2, after p2_1, 2d
    T2.3 重置密码 & 修改密码   :         p2_3, after p2_2, 1d
    T2.4 当前用户信息接口      :         p2_4, after p1_3, 1d
    T2.5 Feign远程调用 auth→user:       p2_5, after p2_1, 2d

    section Phase 3 文章核心·上 (P0)
    T3.1 文章CRUD基础          :crit,    p3_1, after p1_3, 3d
    T3.2 文章状态机            :         p3_2, after p3_1, 2d
    T3.3 文章分页搜索          :         p3_3, after p3_1, 2d
    T3.4 文章置顶功能          :         p3_4, after p3_2, 1d
    T3.5 浏览计数(防刷)        :         p3_5, after p3_1, 2d
    T3.6 点赞功能              :         p3_6, after p3_1, 2d

    section Phase 4 文章核心·下 (P1)
    T4.1 分类树CRUD            :         p4_1, after p0_3, 3d
    T4.2 标签管理              :         p4_2, after p0_3, 2d
    T4.3 文件上传(本地存储)    :         p4_3, after p0_3, 3d
    T4.4 文章与分类/标签联动   :         p4_4, after p4_1, 2d

    section Phase 5 评论互动 (P1)
    T5.1 发表评论              :         p5_1, after p1_3, 2d
    T5.2 楼中楼回复            :         p5_2, after p5_1, 2d
    T5.3 评论审核              :         p5_3, after p5_1, 1d
    T5.4 评论列表(树形)        :         p5_4, after p5_2, 2d

    section Phase 6 统计日志 (P2)
    T6.1 AOP操作日志           :         p6_1, after p0_3, 2d
    T6.2 登录日志              :         p6_2, after p1_2, 1d
    T6.3 数据仪表盘            :         p6_3, after p3_3, 3d
    T6.4 系统配置管理          :         p6_4, after p0_3, 1d

    section Phase 7 非功能增强 (P2)
    T7.1 Redis缓存集成         :         p7_1, after p4_1, 3d
    T7.2 Sentinel接口限流      :         p7_2, after p0_3, 2d
    T7.3 Knife4j接口文档       :         p7_3, after p3_1, 2d
    T7.4 单元测试              :         p7_4, after p3_1, 3d
    T7.5 Docker Compose部署    :         p7_5, after p7_1, 2d
```

---

## 关键里程碑 (Milestones)

| 里程碑 | 时间点 | 完成标准 | 对应任务 |
|--------|--------|----------|----------|
| **M1: 项目跑通** | Day 1 结束 | 全部服务启动成功 + 能登录返回Token | T0.1~T0.3, T1.1~T1.2 |
| **M2: 鉴权闭环** | Day 3 结束 | JWT→网关鉴权→RBAC权限控制全链路打通 | T1.3~T1.6 |
| **M3: 核心可用** | Day 5 结束 | 用户+文章两大核心模块CRUD+状态机完成 | T2.x, T3.1~T3.4 |
| **M4: 功能完整** | Day 7 结束 | 分类/标签/上传/评论全部接口就绪 | T4.x, T5.x |
| **M5: 项目交付** | Day 9 结束 | 缓存+限流+文档+测试+Docker全部完成 | T6.x, T7.x |

---

## 优先级说明

| 标记 | 含义 | 处理策略 |
|------|------|----------|
| **P0-阻塞** | 不做后面的全卡住 | 第一时间完成 |
| **P0** | 核心功能，MVP必须 | 高优先级排期 |
| **P1** | 重要功能，体验必需 | P0完成后跟进 |
| **P2** | 锦上添花，可后续迭代 | 有时间再做 |
| **crit** | 关键路径任务 | 延迟会影响整体进度 |
