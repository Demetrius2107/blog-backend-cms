package com.demetrius.vellastra.publish.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demetrius.vellastra.publish.domain.build.node.NodeExecutor;
import com.demetrius.vellastra.publish.domain.build.valueobject.BuildNode;
import com.demetrius.vellastra.publish.infrastructure.persistence.mapper.PublishBuildNodeMapper;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: NodeScheduler</p>
 * <p>Description: 构建节点调度器，按状态机规则编排节点执行、重试与状态流转</p>
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
public class NodeScheduler {

    private final PublishBuildNodeMapper nodeMapper;
    private final Map<String, NodeExecutor> executorMap = new HashMap<>();

    public NodeScheduler(PublishBuildNodeMapper nodeMapper, List<NodeExecutor> executors) {
        this.nodeMapper = nodeMapper;
        for (NodeExecutor executor : executors) {
            executorMap.put(executor.supportNode(), executor);
        }
    }

    /**
     * 初始化构建的节点序列（pending 状态入库）
     */
    public void initNodes(Long buildId) {
        for (BuildNode node : BuildNode.values()) {
            PublishBuildNodePO po = new PublishBuildNodePO();
            po.setBuildId(buildId);
            po.setNodeCode(node.getCode());
            po.setNodeName(node.getName());
            po.setSortOrder(node.getOrder());
            po.setStatus("pending");
            po.setRetryCount(0);
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            nodeMapper.insert(po);
        }
        log.info("构建节点序列已初始化: buildId={}, nodes={}", buildId, BuildNode.values().length);
    }

    /**
     * 按状态机调度执行全部节点
     *
     * @return true=全部节点成功
     */
    public boolean executeAll(PublishBuildPO build, int maxRetries) {
        List<PublishBuildNodePO> nodes = nodeMapper.selectList(
                new LambdaQueryWrapper<PublishBuildNodePO>()
                        .eq(PublishBuildNodePO::getBuildId, build.getId())
                        .orderByAsc(PublishBuildNodePO::getSortOrder));

        boolean allSuccess = true;
        for (PublishBuildNodePO node : nodes) {
            NodeExecutor executor = executorMap.get(node.getNodeCode());
            if (executor == null) {
                markNode(node, "skipped", "无对应执行器");
                continue;
            }
            boolean ok = runNodeWithRetry(build, node, executor, maxRetries);
            if (!ok) {
                allSuccess = false;
                // 状态机：失败节点后续节点标记 skipped（不继续执行）
                markSubsequentSkipped(build.getId(), node.getSortOrder());
                break;
            }
        }
        return allSuccess;
    }

    /**
     * 节点执行 + 失败重试
     */
    private boolean runNodeWithRetry(PublishBuildPO build, PublishBuildNodePO node,
                                     NodeExecutor executor, int maxRetries) {
        int attempt = 0;
        while (attempt <= maxRetries) {
            markNode(node, "running", null);
            node.setStartedAt(LocalDateTime.now());
            boolean ok = executor.execute(build, node);
            node.setCompletedAt(LocalDateTime.now());
            node.setUpdateTime(LocalDateTime.now());
            if (ok) {
                markNode(node, "success", node.getNodeLog());
                return true;
            }
            attempt++;
            node.setRetryCount(attempt);
            if (attempt <= maxRetries) {
                markNode(node, "retrying", "第 " + attempt + " 次重试");
                log.warn("节点重试: buildId={}, node={}, attempt={}/{}",
                        build.getId(), node.getNodeCode(), attempt, maxRetries);
            } else {
                markNode(node, "failed", node.getNodeLog() != null ? node.getNodeLog() : "执行失败");
                log.error("节点最终失败: buildId={}, node={}", build.getId(), node.getNodeCode());
                return false;
            }
        }
        return false;
    }

    private void markNode(PublishBuildNodePO node, String status, String logMsg) {
        node.setStatus(status);
        if (logMsg != null) node.setNodeLog(logMsg);
        node.setUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
    }

    private void markSubsequentSkipped(Long buildId, int failedOrder) {
        List<PublishBuildNodePO> rest = nodeMapper.selectList(
                new LambdaQueryWrapper<PublishBuildNodePO>()
                        .eq(PublishBuildNodePO::getBuildId, buildId)
                        .gt(PublishBuildNodePO::getSortOrder, failedOrder));
        for (PublishBuildNodePO node : rest) {
            markNode(node, "skipped", "前置节点失败，跳过");
        }
    }
}
