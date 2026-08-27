-- ============================================================
-- 星垂野内容系统（Vellastra Content System）— 阶段二发布模块 DDL
-- 数据库: vellastra
-- 字符集: utf8mb4 / utf8mb4_unicode_ci
-- 引擎:   InnoDB
-- 版本:   2.0
-- 日期:   2026-08-26
-- 说明:   发布引擎三张表，字段与 PublishSitePO/PublishBuildPO/PublishBuildNodePO 对齐
--         依赖 map-underscore-to-camel-case=true（各模块 application.yml 已配置）
-- ============================================================

USE `vellastra`;

-- -----------------------------------------------------------
-- 1. t_publish_site（发布站点表）
-- 管理静态站点配置：仓库地址、构建命令、输出目录、域名等
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `t_publish_site`;
CREATE TABLE `t_publish_site`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`                VARCHAR(64)  NOT NULL COMMENT '站点名称',
    `slug`                VARCHAR(64)  NOT NULL COMMENT '站点标识（URL 友好）',
    `description`         VARCHAR(255) DEFAULT NULL COMMENT '站点描述',
    `repo_url`            VARCHAR(512) DEFAULT NULL COMMENT '站点仓库地址（GitHub 等）',
    `build_command`       VARCHAR(255) DEFAULT NULL COMMENT '构建命令，如 npm run build',
    `output_dir`          VARCHAR(128) DEFAULT NULL COMMENT '构建产物输出目录，如 dist',
    `domain`              VARCHAR(255) DEFAULT NULL COMMENT '站点域名',
    `notify_email`        VARCHAR(64)  DEFAULT NULL COMMENT '构建通知邮箱',
    `max_build_retention` INT          NOT NULL DEFAULT 30 COMMENT '构建记录保留天数',
    `concurrent_build`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否允许并发构建：1是 0否',
    `status`              VARCHAR(16)  NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='发布站点表';

-- -----------------------------------------------------------
-- 2. t_publish_build（构建任务表）
-- 一次发布的完整记录：版本、状态、触发人、回滚关联等
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `t_publish_build`;
CREATE TABLE `t_publish_build`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `site_id`            BIGINT       NOT NULL COMMENT '关联站点ID',
    `version_tag`        VARCHAR(64)  NOT NULL COMMENT '版本标签，如 v20260826.1',
    `environment`        VARCHAR(32)  NOT NULL DEFAULT 'production' COMMENT '部署环境：production/staging',
    `build_number`       VARCHAR(64)  DEFAULT NULL COMMENT '构建编号（时间戳生成）',
    `status`             VARCHAR(16)  NOT NULL DEFAULT 'queued' COMMENT '状态：queued/building/success/failed/rollbacked',
    `retry_count`        INT          NOT NULL DEFAULT 0 COMMENT '构建重试次数',
    `max_retries`        INT          NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `triggered_by`       VARCHAR(32)  DEFAULT NULL COMMENT '触发人（用户ID或系统）',
    `commit_sha`         VARCHAR(64)  DEFAULT NULL COMMENT '关联的 Git commit SHA',
    `commit_message`     VARCHAR(512) DEFAULT NULL COMMENT '关联的 commit message',
    `branch`             VARCHAR(64)  DEFAULT NULL COMMENT '关联的分支名',
    `error_message`      TEXT         DEFAULT NULL COMMENT '失败错误信息',
    `duration_ms`        BIGINT       DEFAULT NULL COMMENT '构建耗时（毫秒）',
    `rollbacked`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已回滚：1是 0否',
    `rolled_back_from_id` BIGINT      DEFAULT NULL COMMENT '回滚来源构建ID',
    `started_at`         DATETIME     DEFAULT NULL COMMENT '构建开始时间',
    `completed_at`       DATETIME     DEFAULT NULL COMMENT '构建完成时间',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_site_id` (`site_id`),
    KEY `idx_status` (`status`),
    KEY `idx_version_tag` (`version_tag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='构建任务表';

-- -----------------------------------------------------------
-- 3. t_publish_build_node（构建节点表）
-- 一次构建的节点级状态机记录：PREPARE→GIT_CLONE→BUILD→DEPLOY→COMPLETE
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `t_publish_build_node`;
CREATE TABLE `t_publish_build_node`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `build_id`     BIGINT       NOT NULL COMMENT '关联构建任务ID',
    `node_code`    VARCHAR(32)  NOT NULL COMMENT '节点编码：PREPARE/GIT_CLONE/BUILD/DEPLOY/COMPLETE',
    `node_name`    VARCHAR(32)  NOT NULL COMMENT '节点名称',
    `sort_order`   INT          NOT NULL COMMENT '执行顺序',
    `status`       VARCHAR(16)  NOT NULL DEFAULT 'pending' COMMENT '状态：pending/running/success/failed/skipped/retrying',
    `retry_count`  INT          NOT NULL DEFAULT 0 COMMENT '节点重试次数',
    `node_log`     TEXT         DEFAULT NULL COMMENT '节点执行日志',
    `started_at`   DATETIME     DEFAULT NULL COMMENT '节点开始时间',
    `completed_at` DATETIME     DEFAULT NULL COMMENT '节点完成时间',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_build_id` (`build_id`),
    KEY `idx_build_status` (`build_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='构建节点表';
