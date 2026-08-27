package com.demetrius.vellastra.publish.application;

import com.demetrius.vellastra.publish.infrastructure.persistence.mapper.PublishBuildMapper;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Title: PublishBuildExecutor</p>
 * <p>Description: 构建执行器——独立 Bean，承接异步构建逻辑</p>
 *
 * <p>为什么独立成 Bean：{@code @Async} 基于 Spring AOP 代理，只有从外部 Bean 调用才会织入异步逻辑。
 * 原先 {@code executeBuild} 与 {@code startBuild} 同处 {@link PublishBuildService}，
 * {@code startBuild} 通过 {@code this.executeBuild()} 直接调用，绕过代理导致 {@code @Async} 失效，
 * 构建在 {@code startBuild} 的数据库事务内同步执行，长时间占用事务连接。
 * 拆分后 {@link PublishBuildService} 注入本 Bean 调用，代理生效，构建真正异步、脱离事务边界。</p>
 *
 * @author wanqiu
 * @since 2.0
 */
@Slf4j
@Component
public class PublishBuildExecutor {

    private final PublishBuildMapper buildMapper;
    private final NodeScheduler nodeScheduler;
    private final PublishNotificationService notificationService;

    @Value("${publish.max-retries:3}")
    private int maxRetries;

    public PublishBuildExecutor(PublishBuildMapper buildMapper,
                                NodeScheduler nodeScheduler,
                                PublishNotificationService notificationService) {
        this.buildMapper = buildMapper;
        this.nodeScheduler = nodeScheduler;
        this.notificationService = notificationService;
    }

    /**
     * 异步执行构建（节点状态机调度）。
     * 从 {@link PublishBuildService#startBuild} / {@link PublishBuildService#retryBuild} 外部调用，
     * 经 Spring 代理使 {@code @Async} 生效。
     */
    @Async
    public CompletableFuture<Void> executeBuild(Long buildId) {
        PublishBuildPO build = buildMapper.selectById(buildId);
        if (build == null) return CompletableFuture.completedFuture(null);

        long startMs = System.currentTimeMillis();

        try {
            updateStatus(build, "building");

            // 初始化节点序列（PREPARE→GIT_CLONE→BUILD→DEPLOY→COMPLETE）
            nodeScheduler.initNodes(buildId);

            // 按状态机调度执行全部节点（含失败重试与后续节点跳过）
            boolean allSuccess = nodeScheduler.executeAll(build, maxRetries);

            if (allSuccess) {
                build.setStatus("success");
                build.setCompletedAt(LocalDateTime.now());
                build.setDurationMs(System.currentTimeMillis() - startMs);
                buildMapper.updateById(build);
                notificationService.notifyBuildSuccess(build);
                log.info("构建成功: id={}, version={}, duration={}ms",
                        buildId, build.getVersionTag(), build.getDurationMs());
            } else {
                failBuild(build, "构建节点执行失败", startMs);
            }

        } catch (Exception e) {
            log.error("构建异常: buildId={}, error={}", buildId, e.getMessage(), e);
            failBuild(build, e.getMessage(), startMs);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void updateStatus(PublishBuildPO build, String status) {
        build.setStatus(status);
        if ("building".equals(status) && build.getStartedAt() == null) {
            build.setStartedAt(LocalDateTime.now());
        }
        build.setUpdateTime(LocalDateTime.now());
        buildMapper.updateById(build);
    }

    private void failBuild(PublishBuildPO build, String reason, long startMs) {
        build.setStatus("failed");
        build.setErrorMessage(reason);
        build.setCompletedAt(LocalDateTime.now());
        build.setDurationMs(System.currentTimeMillis() - startMs);
        buildMapper.updateById(build);
        notificationService.notifyBuildFailed(build, reason);
        log.warn("构建失败: buildId={}, reason={}", build.getId(), reason);
    }
}
