package com.demetrius.vellastra.publish.domain.build.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishBuild {
    private Long id;
    private Long siteId;
    private String versionTag;
    private String environment;
    private String buildNumber;
    private String status;
    private Integer retryCount;
    private Integer maxRetries;
    private String triggeredBy;
    private String commitSha;
    private String commitMessage;
    private String branch;
    private String errorMessage;
    private Long durationMs;
    private Boolean rollbacked;
    private Long rolledBackFromId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
