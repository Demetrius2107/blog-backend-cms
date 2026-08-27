package com.demetrius.vellastra.publish.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demetrius.vellastra.publish.infrastructure.persistence.po.PublishBuildNodePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>Title: PublishBuildNodeMapper</p>
 * <p>Description: 构建节点 Mapper（MyBatis-Plus）</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-08-03
 * @updateTime 2026-08-03
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@Mapper
public interface PublishBuildNodeMapper extends BaseMapper<PublishBuildNodePO> {
}
