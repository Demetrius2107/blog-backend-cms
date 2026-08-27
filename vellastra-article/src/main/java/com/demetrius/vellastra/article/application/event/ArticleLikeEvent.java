package com.demetrius.vellastra.article.application.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>Title: ArticleLikeEvent</p>
 * <p>Description: 文章点赞变更事件</p>
 *
 * <p>点赞是高频写操作，Redis 热数据（Set）先响应，DB 落库通过事件异步完成，
 * 保证「读快 + 最终一致」：Redis 保证读时的一致性，DB 通过异步对账保证最终一致。</p>
 *
 * @author wanqiu
 * @since 2.0
 */
@Getter
@AllArgsConstructor
public class ArticleLikeEvent {

    /** 文章ID */
    private final Long articleId;

    /** 用户ID */
    private final Long userId;

    /** true=点赞，false=取消 */
    private final boolean liked;
}
