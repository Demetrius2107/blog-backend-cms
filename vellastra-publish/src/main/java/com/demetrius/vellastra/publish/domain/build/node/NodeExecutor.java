package com.demetrius.vellastra.publish.domain.build.node;

import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;

/**
 * <p>Title: NodeExecutor</p>
 * <p>Description: 构建节点执行器接口，定义单个节点的执行契约</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-08-03
 * @updateTime 2026-08-03
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
public interface NodeExecutor {

    /** 支持的节点编码（对应 BuildNode.code） */
    String supportNode();

    /**
     * 执行节点
     *
     * @param build 构建任务
     * @param node  节点记录
     * @return true=成功，false=失败
     */
    boolean execute(PublishBuildPO build, PublishBuildNodePO node);
}
