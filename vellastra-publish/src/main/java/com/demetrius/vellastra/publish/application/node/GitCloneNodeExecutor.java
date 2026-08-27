package com.demetrius.vellastra.publish.application.node;

import com.demetrius.vellastra.publish.domain.build.node.NodeExecutor;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>Title: GitCloneNodeExecutor</p>
 * <p>Description: 拉取代码节点执行器</p>
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
public class GitCloneNodeExecutor implements NodeExecutor {

    @Override
    public String supportNode() {
        return "GIT_CLONE";
    }

    @Override
    public boolean execute(PublishBuildPO build, PublishBuildNodePO node) {
        try {
            Thread.sleep(800);
            node.setNodeLog("拉取代码成功：branch=" + build.getBranch());
            log.info("GIT_CLONE 节点执行: buildId={}, branch={}", build.getId(), build.getBranch());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            node.setNodeLog("拉取代码中断");
            return false;
        }
    }
}
