package com.demetrius.vellastra.publish.application.node;

import com.demetrius.vellastra.publish.domain.build.node.NodeExecutor;
import com.demetrius.vellastra.publish.domain.site.entity.PublishSite;
import com.demetrius.vellastra.publish.domain.site.repository.PublishSiteRepository;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildPO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: DeployNodeExecutor</p>
 * <p>Description: 部署节点执行器，通过 GitHub repository_dispatch 触发静态站构建部署</p>
 *
 * <p>调用 GitHub API：POST /repos/{owner}/{repo}/dispatches，event_type 由配置决定，
 * GitHub Actions 中 repository_dispatch workflow 的 types 需与此匹配。
 * 若未配置 GitHub token/owner/repo，降级为模拟成功（便于本地测试）。</p>
 *
 * @author wanqiu
 * @since 2.0
 */
@Slf4j
@Component
public class DeployNodeExecutor implements NodeExecutor {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("github\\.com[/:]([^/]+)/([^/.]+)");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final PublishSiteRepository siteRepository;

    @Value("${publish.github.token:}")
    private String githubToken;

    @Value("${publish.github.owner:}")
    private String defaultOwner;

    @Value("${publish.github.repo:}")
    private String defaultRepo;

    @Value("${publish.github.event-type:publish}")
    private String eventType;

    @Value("${publish.github.api-base:https://api.github.com}")
    private String apiBase;

    public DeployNodeExecutor(PublishSiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public String supportNode() {
        return "DEPLOY";
    }

    @Override
    public boolean execute(PublishBuildPO build, PublishBuildNodePO node) {
        // 从站点配置解析 GitHub owner/repo
        PublishSite site = siteRepository.findById(build.getSiteId());
        String owner = defaultOwner;
        String repo = defaultRepo;

        if (site != null && site.getRepoUrl() != null && !site.getRepoUrl().isBlank()) {
            String[] parsed = parseGithubOwnerRepo(site.getRepoUrl());
            if (parsed != null) {
                owner = parsed[0];
                repo = parsed[1];
            }
        }

        // 未配置 GitHub 凭证，降级模拟成功
        if (githubToken == null || githubToken.isBlank() || owner.isBlank() || repo.isBlank()) {
            node.setNodeLog("未配置 GitHub token/owner/repo，跳过真实部署（模拟成功）");
            log.info("DEPLOY 节点降级: buildId={}, 原因=未配置 GitHub 凭证", build.getId());
            return true;
        }

        try {
            String dispatchUrl = String.format("%s/repos/%s/%s/dispatches", apiBase, owner, repo);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event_type", eventType);
            Map<String, Object> clientPayload = new LinkedHashMap<>();
            clientPayload.put("build_id", build.getId());
            clientPayload.put("site_id", build.getSiteId());
            clientPayload.put("version", build.getVersionTag());
            clientPayload.put("environment", build.getEnvironment());
            clientPayload.put("rollback", build.getRollbacked() != null && build.getRollbacked());
            payload.put("client_payload", clientPayload);

            String body = JSON.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dispatchUrl))
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;

            node.setNodeLog(String.format("GitHub repository_dispatch %s：HTTP %d，仓库=%s/%s",
                    ok ? "成功" : "失败", response.statusCode(), owner, repo));
            log.info("DEPLOY 节点执行: buildId={}, owner={}, repo={}, status={}",
                    build.getId(), owner, repo, response.statusCode());
            return ok;
        } catch (Exception e) {
            node.setNodeLog("部署异常：" + e.getMessage());
            log.warn("DEPLOY 节点异常: buildId={}, error={}", build.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从 GitHub 仓库 URL 解析 owner/repo。
     * 支持 https://github.com/owner/repo 和 git@github.com:owner/repo.git 两种格式。
     *
     * @return ["owner", "repo"] 或 null（无法解析时）
     */
    private String[] parseGithubOwnerRepo(String repoUrl) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(repoUrl);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
    }
}
