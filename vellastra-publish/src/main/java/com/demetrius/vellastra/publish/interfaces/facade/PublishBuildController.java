package com.demetrius.vellastra.publish.interfaces.facade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.demetrius.vellastra.common.response.Result;
import com.demetrius.vellastra.publish.application.PublishBuildService;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/publish/builds")
public class PublishBuildController {

    private final PublishBuildService buildService;

    public PublishBuildController(PublishBuildService buildService) { this.buildService = buildService; }

    @PostMapping
    public Result<Long> startBuild(@RequestParam Long siteId,
                                   @RequestParam(defaultValue = "production") String environment,
                                   @RequestHeader("X-User-Id") Long userId) {
        return Result.success(buildService.startBuild(siteId, environment, String.valueOf(userId), null, null, null));
    }

    @GetMapping
    public Result<IPage<PublishBuildPO>> list(@RequestParam(required = false) Long siteId,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(buildService.listBuilds(siteId, status, current, size));
    }

    @GetMapping("/{id}")
    public Result<PublishBuildPO> getById(@PathVariable Long id) {
        return Result.success(buildService.getBuild(id));
    }

    @GetMapping("/history/{siteId}")
    public Result<List<PublishBuildPO>> history(@PathVariable Long siteId,
                                                @RequestParam(defaultValue = "20") int limit) {
        return Result.success(buildService.getBuildHistory(siteId, limit));
    }

    /**
     * 查询构建的节点执行明细（可观测性：各阶段状态/日志/耗时）
     */
    @GetMapping("/{id}/nodes")
    public Result<List<PublishBuildNodePO>> nodes(@PathVariable Long id) {
        return Result.success(buildService.getBuildNodes(id));
    }

    @PostMapping("/{id}/retry")
    public Result<Void> retry(@PathVariable Long id) { buildService.retryBuild(id); return Result.success(); }

    @PostMapping("/{id}/rollback")
    public Result<Void> rollback(@PathVariable Long id, @RequestParam Long targetBuildId,
                                  @RequestHeader("X-User-Id") Long userId) {
        buildService.rollback(id, targetBuildId, String.valueOf(userId));
        return Result.success();
    }
}
