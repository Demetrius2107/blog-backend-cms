package com.demetrius.vellastra.publish.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>Title: PublishBuildNodePO</p>
 * <p>Description: 构建节点持久化对象，与 t_publish_build_node 表对应</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-08-03
 * @updateTime 2026-08-03
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@Data
@TableName("t_publish_build_node")
public class PublishBuildNodePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 构建任务ID */
    private Long buildId;
    /** 节点编码 */
    private String nodeCode;
    /** 节点名称 */
    private String nodeName;
    /** 节点顺序 */
    private Integer sortOrder;
    /** 状态: pending / running / success / failed / skipped / retrying */
    private String status;
    /** 重试次数 */
    private Integer retryCount;
    /** 节点日志 */
    private String nodeLog;
    /** 开始时间 */
    private LocalDateTime startedAt;
    /** 完成时间 */
    private LocalDateTime completedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
