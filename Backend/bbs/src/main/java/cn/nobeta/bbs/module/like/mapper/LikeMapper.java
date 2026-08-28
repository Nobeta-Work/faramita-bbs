package cn.nobeta.bbs.module.like.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

    /**
     * 根据 blogId 查找所有点赞用户 id
     * @param blogId
     * @return
     */
    List<Long> selectLikerIdsByBlogId(@Param("blogId") Long blogId);

    List<Long> selectLikerIdsByCommentId(@Param("commentId") Long commentId);

    int insertBlogLike(
        @Param("blogId") Long blogId,
        @Param("userId") Long userId,
        @Param("createTime") LocalDateTime createTime
    );

    int deleteBlogLike(
        @Param("blogId") Long blogId,
        @Param("userId") Long userId
    );

    void refreshBlogLikeCount(@Param("blogId") Long blogId);

    int insertCommentLike(
        @Param("commentId") Long commentId,
        @Param("userId") Long userId,
        @Param("createTime") LocalDateTime createTime
    );

    int deleteCommentLike(
        @Param("commentId") Long commentId,
        @Param("userId") Long userId
    );

    void refreshCommentLikeCount(@Param("commentId") Long commentId);

    void reconcileBlogLikeCount();

    void reconcileCommentLikeCount();
}
