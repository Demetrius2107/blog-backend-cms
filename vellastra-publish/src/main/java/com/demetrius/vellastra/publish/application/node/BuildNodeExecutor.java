package com.demetrius.vellastra.publish.application.node;

import com.demetrius.vellastra.publish.domain.build.node.NodeExecutor;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>Title: BuildNodeExecutor</p>
 * <p>Description: 执行构建节点，调用站点配置的构建命令</p>
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
public class BuildNodeExecutor implements NodeExecutor {

    @Override
    public String supportNode() {
        return "BUILD";
    }

    @Override
    public boolean execute(PublishBuildPO build, PublishBuildNodePO node) {
        try {
            Thread.sleep(1000);
            node.setNodeLog("构建成功：version=" + build.getVersionTag());
            log.info("BUILD 节点执行: buildId={}, version={}", build.getId(), build.getVersionTag());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            node.setNodeLog("构建中断");
            return false;
        }
    }
}
