package com.demetrius.vellastra.publish.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.vellastra.publish.domain.site.entity.PublishSite;
import com.demetrius.vellastra.publish.domain.site.repository.PublishSiteRepository;
import com.demetrius.vellastra.publish.infrastructure.persistence.mapper.PublishBuildMapper;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.mapper.PublishBuildNodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class PublishBuildService {

    private final PublishBuildMapper buildMapper;
    private final PublishBuildNodeMapper buildNodeMapper;
    private final PublishSiteRepository siteRepository;
    private final PublishNotificationService notificationService;
    private final PublishBuildExecutor buildExecutor;
    private final java.util.concurrent.atomic.AtomicInteger buildCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    @Value("${publish.max-retries:3}")
    private int maxRetries;

    @Value("${publish.version-prefix:v}")
    private String versionPrefix;

    public PublishBuildService(PublishBuildMapper buildMapper,
                               PublishBuildNodeMapper buildNodeMapper,
                               PublishSiteRepository siteRepository,
                               PublishNotificationService notificationService,
                               PublishBuildExecutor buildExecutor) {
        this.buildMapper = buildMapper;
        this.buildNodeMapper = buildNodeMapper;
        this.siteRepository = siteRepository;
        this.notificationService = notificationService;
        this.buildExecutor = buildExecutor;
    }

    // ===================== 构建管理 =====================

    /** 创建并启动构建 */
    @Transactional
    public Long startBuild(Long siteId, String environment, String triggeredBy,
                           String commitSha, String commitMessage, String branch) {
        PublishSite site = siteRepository.findById(siteId);
        if (site == null) throw new RuntimeException("站点不存在");

        // 并发构建检查
        if (Boolean.FALSE.equals(site.getConcurrentBuild())) {
            long running = buildMapper.selectCount(new LambdaQueryWrapper<PublishBuildPO>()
                    .eq(PublishBuildPO::getSiteId, siteId)
                    .in(PublishBuildPO::getStatus, "queued", "building"));
            if (running > 0) throw new RuntimeException("该站点有正在进行的构建，请等待完成");
        }

        // 生成版本号
        String versionTag = versionPrefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "." + buildCounter.incrementAndGet();
        String buildNumber = String.valueOf(System.currentTimeMillis());

        PublishBuildPO po = new PublishBuildPO();
        po.setSiteId(siteId);
        po.setVersionTag(versionTag);
        po.setEnvironment(environment != null ? environment : "production");
        po.setBuildNumber(buildNumber);
        po.setStatus("queued");
        po.setRetryCount(0);
        po.setMaxRetries(maxRetries);
        po.setTriggeredBy(triggeredBy);
        po.setCommitSha(commitSha != null ? commitSha : "");
        po.setCommitMessage(commitMessage != null ? commitMessage : "");
        po.setBranch(branch != null ? branch : "main");
        po.setRollbacked(false);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        buildMapper.insert(po);
        log.info("构建已创建: id={}, siteId={}, version={}, env={}",
                po.getId(), siteId, versionTag, environment);

        // 通过独立 Bean 调用，经 Spring 代理使 @Async 生效，脱离本方法的事务边界
        buildExecutor.executeBuild(po.getId());
        return po.getId();
    }

    /** 重试失败构建 */
    @Transactional
    public void retryBuild(Long buildId) {
        PublishBuildPO build = buildMapper.selectById(buildId);
        if (build == null) throw new RuntimeException("构建记录不存在");
        build.setRetryCount(0);
        build.setStatus("queued");
        build.setErrorMessage(null);
        build.setUpdateTime(LocalDateTime.now());
        buildMapper.updateById(build);
        log.info("构建已重新加入队列: buildId={}", buildId);
        buildExecutor.executeBuild(buildId);
    }

    /** 回滚到指定版本 */
    @Transactional
    public void rollback(Long buildId, Long targetBuildId, String triggeredBy) {
        PublishBuildPO target = buildMapper.selectById(targetBuildId);
        if (target == null) throw new RuntimeException("目标构建记录不存在");

        PublishBuildPO rollbackBuild = new PublishBuildPO();
        rollbackBuild.setSiteId(target.getSiteId());
        rollbackBuild.setVersionTag(target.getVersionTag() + "-rollback");
        rollbackBuild.setEnvironment(target.getEnvironment());
        rollbackBuild.setBuildNumber(String.valueOf(System.currentTimeMillis()));
        rollbackBuild.setStatus("queued");
        rollbackBuild.setRetryCount(0);
        rollbackBuild.setMaxRetries(maxRetries);
        rollbackBuild.setTriggeredBy(triggeredBy);
        rollbackBuild.setRollbacked(true);
        rollbackBuild.setRolledBackFromId(buildId);
        rollbackBuild.setCreateTime(LocalDateTime.now());
        rollbackBuild.setUpdateTime(LocalDateTime.now());
        buildMapper.insert(rollbackBuild);

        // 标记原构建为已回滚
        PublishBuildPO original = buildMapper.selectById(buildId);
        if (original != null) {
            original.setRollbacked(true);
            buildMapper.updateById(original);
        }

        // TODO(A3): 回滚也应走 NodeScheduler 节点序列，当前先复用 executeBuild
        buildExecutor.executeBuild(rollbackBuild.getId());
    }

    // ===================== 查询 =====================

    public IPage<PublishBuildPO> listBuilds(Long siteId, String status, int current, int size) {
        LambdaQueryWrapper<PublishBuildPO> wrapper = new LambdaQueryWrapper<PublishBuildPO>()
                .eq(siteId != null, PublishBuildPO::getSiteId, siteId)
                .eq(status != null, PublishBuildPO::getStatus, status)
                .orderByDesc(PublishBuildPO::getCreateTime);
        return buildMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public PublishBuildPO getBuild(Long id) { return buildMapper.selectById(id); }

    public List<PublishBuildPO> getBuildHistory(Long siteId, int limit) {
        return buildMapper.selectList(new LambdaQueryWrapper<PublishBuildPO>()
                .eq(PublishBuildPO::getSiteId, siteId)
                .orderByDesc(PublishBuildPO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /** 查询某次构建的节点执行明细（可观测性） */
    public List<PublishBuildNodePO> getBuildNodes(Long buildId) {
        return buildNodeMapper.selectList(new LambdaQueryWrapper<PublishBuildNodePO>()
                .eq(PublishBuildNodePO::getBuildId, buildId)
                .orderByAsc(PublishBuildNodePO::getSortOrder));
    }
}
