package online.faramita.bbs.module.like.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import online.faramita.bbs.module.like.entity.LikeBlogChangelog;

@Mapper
public interface LikeMapper {

    /**
     * 根据 blogId 查找所有点赞用户 id
     * @param blogId
     * @return
     */
    List<Long> selectLikerIdsByBlogId(@Param("blogId") Long blogId);

    /**
     * 根据日志批量新增点赞数据
     * @param likeList
     */
    void batchInsertBlogLikeByChanges(@Param("logs") List<LikeBlogChangelog> logs);

    /**
     * 根据日志批量删除点赞数据
     * @param likeList
     */
    void batchDeleteBlogLikeByChanges(@Param("logs") List<LikeBlogChangelog> logs);

    /**
     * 刷新 ids 指定博客的点赞数
     * @param blogIds
     */
    void refreshBlogLikeCountByBlogIds(@Param("blogIds") List<Long> blogIds);

}
