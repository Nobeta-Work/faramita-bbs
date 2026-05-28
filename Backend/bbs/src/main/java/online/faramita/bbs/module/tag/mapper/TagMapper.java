package online.faramita.bbs.module.tag.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import online.faramita.bbs.module.blog.dto.BlogTagBriefRelations;
import online.faramita.bbs.module.tag.vo.TagBriefVO;

@Mapper
public interface TagMapper {

    /**
     * 根据 blogIds 批量查询 blog-tag 关系
     * @param blogIds
     * @return
     */
    Collection<BlogTagBriefRelations> selectBlogTagBriefRelationsByBlogIds(List<Long> blogIds);

    /**
     * 根据 blogId 联表查询 标签简要
     * @param id
     * @return
     */
    List<TagBriefVO> selectTagBriefByBlogId(Long id);

    /**
     * 根据 blogId 联表更新关系表
     * @param blogId
     * @param tagIds
     */
    void updateBlogTagByTagIds(Long blogId, List<Long> tagIds);

}
