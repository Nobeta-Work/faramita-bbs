package online.faramita.bbs.module.like.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper {

    /**
     * 根据 blogId 查找所有点赞用户 id
     * @param blogId
     * @return
     */
    List<Long> selectLikerIdsByBlogId(Long blogId);

}
