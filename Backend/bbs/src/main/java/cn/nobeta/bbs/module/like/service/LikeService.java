package cn.nobeta.bbs.module.like.service;

import cn.nobeta.bbs.common.event.DomainEvent;

public interface LikeService {

    Integer toggleBlogLike(Long userId, Long blogId);

    Integer toggleCommentLike(Long userId, Long commentId);

    void consumeLikeEvent(DomainEvent event);
}
