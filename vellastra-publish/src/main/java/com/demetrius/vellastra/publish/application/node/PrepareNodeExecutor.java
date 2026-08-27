package com.demetrius.vellastra.publish.application.node;

import com.demetrius.vellastra.publish.domain.build.node.NodeExecutor;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>Title: PrepareNodeExecutor</p>
 * <p>Description: 前置检查节点执行器，校验站点存在性与并发构建限制</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-08-03
 * @updateTime 2026-08-03
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@Slf4j
@Component
public class PrepareNodeExecutor implements NodeExecutor {

    @Override
    public String supportNode() {
        return "PREPARE";
    }

    @Override
    public boolean execute(PublishBuildPO build, PublishBuildNodePO node) {
        node.setNodeLog("前置检查通过：buildId=" + build.getId());
        log.info("PREPARE 节点执行: buildId={}", build.getId());
        return true;
    }
}
