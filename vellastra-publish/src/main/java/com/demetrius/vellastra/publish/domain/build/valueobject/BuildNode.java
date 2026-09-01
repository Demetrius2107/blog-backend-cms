package com.demetrius.vellastra.publish.domain.build.valueobject;

/**
 * <p>Title: BuildNode</p>
 * <p>Description: 构建节点枚举，定义构建流水线的可编排节点</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-08-03
 * @updateTime 2026-08-03
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
public enum BuildNode {

    /** 前置检查 */
    PREPARE("PREPARE", "前置检查", 1),
    /** 拉取代码 */
    GIT_CLONE("GIT_CLONE", "拉取代码", 2),
    /** 执行构建 */
    BUILD("BUILD", "执行构建", 3),
    /** 部署 */
    DEPLOY("DEPLOY", "部署", 4),
    /** 完成 */
    COMPLETE("COMPLETE", "完成", 5);

    private final String code;
    private final String name;
    private final int order;

    BuildNode(String code, String name, int order) {
        this.code = code;
        this.name = name;
        this.order = order;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public int getOrder() { return order; }
}
