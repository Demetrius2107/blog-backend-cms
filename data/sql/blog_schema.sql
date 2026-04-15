-- ============================================================
-- 博客管理后台 数据库建表脚本
-- MySQL 8.0+
-- 字符集: utf8mb4, 排序规则: utf8mb4_general_ci
-- ============================================================

-- ============================================================
-- 博客管理后台 数据库建表脚本
-- MySQL 8.0+
-- 字符集: utf8mb4, 排序规则: utf8mb4_general_ci
-- ============================================================

CREATE DATABASE IF NOT EXISTS `blog_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `blog_db`;

-- ============================================================
-- 1. 用户表 (t_user)
-- ============================================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`        VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`        VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `email`           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `nickname`        VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`          VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `gender`          TINYINT      DEFAULT 0 COMMENT '性别:0未知 1男 2女',
    `bio`             VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. 角色表 (t_role)
-- ============================================================
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ============================================================
-- 3. 权限/菜单表 (t_menu)
-- ============================================================
DROP TABLE IF EXISTS `t_menu`;
CREATE TABLE `t_menu` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`   BIGINT        NOT NULL DEFAULT 0 COMMENT '父级菜单ID,0为顶级',
    `menu_name`   VARCHAR(50)   NOT NULL COMMENT '菜单名称',
    `menu_type`   TINYINT       NOT NULL DEFAULT 1 COMMENT '类型:1目录 2菜单 3按钮',
    `path`        VARCHAR(200)  DEFAULT NULL COMMENT '路由路径',
    `component`   VARCHAR(200)  DEFAULT NULL COMMENT '组件路径',
    `perms`       VARCHAR(100)  DEFAULT NULL COMMENT '权限标识',
    `icon`        VARCHAR(100)  DEFAULT NULL COMMENT '图标',
    `sort_order`  INT           DEFAULT 0 COMMENT '排序',
    `visible`     TINYINT       NOT NULL DEFAULT 1 COMMENT '是否显示:0隐藏 1显示',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限菜单表';

-- ============================================================
-- 4. 用户-角色关联表 (t_user_role)
-- ============================================================
DROP TABLE IF EXISTS `t_user_role`;
CREATE TABLE `t_user_role` (
    `id`      BIGINT  NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT  NOT NULL COMMENT '用户ID',
    `role_id` BIGINT  NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- ============================================================
-- 5. 角色-菜单关联表 (t_role_menu)
-- ============================================================
DROP TABLE IF EXISTS `t_role_menu`;
CREATE TABLE `t_role_menu` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ============================================================
-- 6. 文章表 (t_article) — 核心领域
-- ============================================================
DROP TABLE IF EXISTS `t_article`;
CREATE TABLE `t_article` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title`         VARCHAR(200)  NOT NULL COMMENT '文章标题',
    `summary`       VARCHAR(500)  DEFAULT NULL COMMENT '文章摘要',
    `content`       LONGTEXT      NOT NULL COMMENT '文章内容(Markdown)',
    `content_html`  LONGTEXT      DEFAULT NULL COMMENT '渲染后的HTML内容',
    `cover_image`   VARCHAR(500)  DEFAULT NULL COMMENT '封面图URL',
    `category_id`   BIGINT        DEFAULT NULL COMMENT '分类ID',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态:0草稿 1已发布 2已下架',
    `is_top`        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否置顶:0否 1是',
    `is_original`   TINYINT       NOT NULL DEFAULT 1 COMMENT '是否原创:0转载 1原创',
    `source_url`    VARCHAR(500)  DEFAULT NULL COMMENT '原文链接(转载时)',
    `tags`          VARCHAR(500)  DEFAULT NULL COMMENT '标签(逗号分隔冗余字段)',
    `author_id`     BIGINT        NOT NULL COMMENT '作者ID',
    `view_count`    BIGINT        NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `like_count`    BIGINT        NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT           NOT NULL DEFAULT 0 COMMENT '评论数',
    `word_count`    INT           DEFAULT NULL COMMENT '字数统计',
    `publish_time`  DATETIME      DEFAULT NULL COMMENT '发布时间',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_top` (`is_top`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章表';

-- ============================================================
-- 7. 分类表 (t_category) — 树形结构
-- ============================================================
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID,0为顶级',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `slug`        VARCHAR(50)  DEFAULT NULL COMMENT '分类别名(URL友好)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    `icon`        VARCHAR(100) DEFAULT NULL COMMENT '分类图标',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序(越小越前)',
    `article_count` INT        NOT NULL DEFAULT 0 COMMENT '文章数量(冗余)',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';

-- ============================================================
-- 8. 标签表 (t_tag)
-- ============================================================
DROP TABLE IF EXISTS `t_tag`;
CREATE TABLE `t_tag` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`          VARCHAR(50) NOT NULL COMMENT '标签名称',
    `slug`          VARCHAR(50) DEFAULT NULL COMMENT '标签别名',
    `article_count` INT         NOT NULL DEFAULT 0 COMMENT '使用次数(冗余)',
    `status`        TINYINT     NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    `deleted`       TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

-- ============================================================
-- 9. 文章-标签关联表 (t_article_tag)
-- ============================================================
DROP TABLE IF EXISTS `t_article_tag`;
CREATE TABLE `t_article_tag` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id`    BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章标签关联表';

-- ============================================================
-- 10. 评论表 (t_comment) — 支持楼中楼回复
-- ============================================================
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id`  BIGINT        NOT NULL COMMENT '文章ID',
    `user_id`     BIGINT        NOT NULL COMMENT '评论者ID',
    `parent_id`   BIGINT        NOT NULL DEFAULT 0 COMMENT '父评论ID,0为顶层',
    `reply_to_id` BIGINT        DEFAULT NULL COMMENT '被回复的评论ID',
    `reply_to_user_id` BIGINT   DEFAULT NULL COMMENT '被回复的用户ID',
    `content`     TEXT          NOT NULL COMMENT '评论内容',
    `ip_address`  VARCHAR(50)   DEFAULT NULL COMMENT 'IP地址',
    `user_agent`  VARCHAR(500)  DEFAULT NULL COMMENT '浏览器UA',
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态:0待审核 1已通过 2已拒绝',
    `like_count`  INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- ============================================================
-- 11. 文件上传表 (t_file)
-- ============================================================
DROP TABLE IF EXISTS `t_file`;
CREATE TABLE `t_file` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `file_name`     VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path`     VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_url`      VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_size`     BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type`     VARCHAR(50)  DEFAULT NULL COMMENT 'MIME类型',
    `file_ext`      VARCHAR(20)  DEFAULT NULL COMMENT '文件扩展名',
    `storage_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '存储类型:1本地 2OSS 3COS',
    `bucket_name`   VARCHAR(100) DEFAULT NULL COMMENT '存储桶名(OSS/COS)',
    `upload_user_id` BIGINT      DEFAULT NULL COMMENT '上传者ID',
    `biz_type`      VARCHAR(50)  DEFAULT NULL COMMENT '业务类型:article/avatar/other',
    `biz_id`        BIGINT       DEFAULT NULL COMMENT '业务关联ID',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_upload_user_id` (`upload_user_id`),
    KEY `idx_biz_type` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件上传表';

-- ============================================================
-- 12. 文章点赞记录表 (t_article_like) — 防重复点赞
-- ============================================================
DROP TABLE IF EXISTS `t_article_like`;
CREATE TABLE `t_article_like` (
    `id`        BIGINT   NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT  NOT NULL COMMENT '文章ID',
    `user_id`   BIGINT   NOT NULL COMMENT '用户ID',
    `status`    TINYINT  NOT NULL DEFAULT 1 COMMENT '0取消赞 1已点赞',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章点赞记录表';

-- ============================================================
-- 13. 系统配置表 (t_system_config) — KV配置
-- ============================================================
DROP TABLE IF EXISTS `t_system_config`;
CREATE TABLE `t_system_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`  VARCHAR(100) NOT NULL COMMENT '配置Key',
    `config_value` TEXT        NOT NULL COMMENT '配置Value(JSON或文本)',
    `config_group` VARCHAR(50) DEFAULT 'default' COMMENT '配置分组:site/seo/social/upload',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- ============================================================
-- 14. 友情链接表 (t_friend_link)
-- ============================================================
DROP TABLE IF EXISTS `t_friend_link`;
CREATE TABLE `t_friend_link` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL COMMENT '链接名称',
    `url`         VARCHAR(500) NOT NULL COMMENT '链接地址',
    `logo`        VARCHAR(500) DEFAULT NULL COMMENT 'Logo图片',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='友情链接表';

-- ============================================================
-- 15. 操作日志表 (t_operation_log) 【已修复语法错误】
-- ============================================================
DROP TABLE IF EXISTS `t_operation_log`;
CREATE TABLE `t_operation_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `module`      VARCHAR(50)  NOT NULL COMMENT '操作模块',
    `operation`   VARCHAR(50)  NOT NULL COMMENT '操作类型:CREATE/UPDATE/DELETE/LOGIN等',
    `method`      VARCHAR(200) NOT NULL COMMENT '请求方法(类名+方法名)',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法:GET/POST/PUT/DELETE',
    `request_params`  TEXT      DEFAULT NULL COMMENT '请求参数(JSON)',
    `response_result` TEXT      DEFAULT NULL COMMENT '返回结果',
    `operator_id`  BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `ip_address`   VARCHAR(50)  DEFAULT NULL COMMENT '操作IP',
    `user_agent`   VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    `duration`     BIGINT       DEFAULT NULL COMMENT '耗时(ms)',
    `success`      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否成功:0失败 1成功',
    `error_msg`    TEXT         DEFAULT NULL COMMENT '错误信息',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_module` (`module`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ============================================================
-- 16. 登录日志表 (t_login_log)
-- ============================================================
DROP TABLE IF EXISTS `t_login_log`;
CREATE TABLE `t_login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       DEFAULT NULL COMMENT '用户ID',
    `username`    VARCHAR(50)  DEFAULT NULL COMMENT '登录账号',
    `login_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '登录方式:1账号密码 2手机号 3第三方',
    `ip_address`  VARCHAR(50)  DEFAULT NULL COMMENT '登录IP',
    `location`    VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    `browser`     VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    `os`          VARCHAR(50)  DEFAULT NULL COMMENT '操作系统',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0失败 1成功',
    `message`     VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 默认管理员账号: admin / admin123
INSERT INTO `t_user` (`username`, `password`, `email`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1KO', 'admin@blog.com', '超级管理员', 1);

-- 默认角色
INSERT INTO `t_role` (`role_name`, `role_code`, `description`, `sort_order`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('博主',       'BLOGGER',    '可管理文章和评论', 2),
('普通用户',   'USER',       '只能查看和评论', 3);

-- 绑定管理员角色
INSERT INTO `t_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 默认分类
INSERT INTO `t_category` (`parent_id`, `name`, `slug`, `description`, `sort_order`) VALUES
(0, '技术',     'tech',     '技术相关文章', 1),
(0, '生活',     'life',     '生活随笔',     2),
(0, '读书笔记', 'reading',  '读书笔记',     3);

-- 默认系统配置
INSERT INTO `t_system_config` (`config_key`, `config_value`, `config_group`, `remark`) VALUES
('site.name',    'Demetrius Blog',  'site',  '站点名称'),
('site.description', '一个基于DDD架构的博客系统', 'site', '站点描述'),
('site.logo',    '',                'site',  '站点Logo'),
('site.icp',     '',                'site',  '备案号'),
('seo.keywords', '博客,技术,DDD,Spring Cloud', 'seo', 'SEO关键词'),
('seo.description', 'Demetrius个人博客，分享技术与生活', 'seo', 'SEO描述');