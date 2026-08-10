package cn.nobeta.bbs.module.like.service;

import java.util.List;

import cn.nobeta.bbs.module.like.entity.LikeBlogChangelog;

public interface LikeService {

    Integer toggleBlogLike(Long userId, Long blogId);

    void flushLikeBlogChangelog(List<LikeBlogChangelog> logs);

}
