package online.faramita.bbs.module.like.service;

import java.util.List;

import online.faramita.bbs.module.like.entity.LikeBlogChangelog;

public interface LikeService {

    Integer toggleBlogLike(Long userId, Long blogId);

    void flushLikeBlogChangelog(List<LikeBlogChangelog> logs);

}
