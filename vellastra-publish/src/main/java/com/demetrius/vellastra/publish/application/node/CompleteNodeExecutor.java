package com.demetrius.vellastra.publish.application.node;

import com.demetrius.vellastra.publish.domain.build.node.NodeExecutor;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>Title: CompleteNodeExecutor</p>
 * <p>Description: 完成节点执行器，标记构建整体成功</p>
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
public class CompleteNodeExecutor implements NodeExecutor {

    @Override
    public String supportNode() {
        return "COMPLETE";
    }

    @Override
    public boolean execute(PublishBuildPO build, PublishBuildNodePO node) {
        node.setNodeLog("构建发布完成：version=" + build.getVersionTag());
        log.info("COMPLETE 节点执行: buildId={}", build.getId());
        return true;
    }
}
