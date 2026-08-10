package cn.nobeta.bbs.module.tag.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;

import cn.nobeta.bbs.module.blog.dto.BlogTagBriefRelations;
import cn.nobeta.bbs.module.tag.dto.TagPageQuery;
import cn.nobeta.bbs.module.tag.entity.Tag;
import cn.nobeta.bbs.module.tag.vo.TagBriefVO;

@Mapper
public interface TagMapper {

    /**
     * 根据 blogIds 批量查询 blog-tag 关系
     * @param blogIds
     * @return
     */
    Collection<BlogTagBriefRelations> selectBlogTagBriefRelationsByBlogIds(@Param("blogIds") List<Long> blogIds);

    /**
     * 根据 blogId 联表查询 标签简要
     * @param id
     * @return
     */
    List<TagBriefVO> selectTagBriefByBlogId(@Param("blogId") Long blogId);

    
    /**
     * 删除指定 blogId 的标签关系表
     * @param blogId
     */
    void deleteBlogTagRelationsByBlogId(@Param("blogId") Long blogId);

    /**
     * 创建 tag
     * @param tag
     * @return
     */
    void insertTag(Tag tag);

    /**
     * 查询 tag
     * @param query
     * @return
     */
    Page<Tag> selectTagPage(TagPageQuery query);

    /**
     * 根据 blogId 和 tagIds 批量插入关系
     * @param blogId
     * @param tagIds
     */
    void batchInsertBlogTagReliations(@Param("blogId") Long blogId, @Param("tagIds") List<Long> tagIds);

}
