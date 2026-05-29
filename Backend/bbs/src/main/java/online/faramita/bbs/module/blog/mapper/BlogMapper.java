package online.faramita.bbs.module.blog.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.github.pagehelper.Page;

import online.faramita.bbs.common.dto.PageQuery;
import online.faramita.bbs.module.blog.dto.BlogEditDTO;
import online.faramita.bbs.module.blog.dto.BlogPageQuery;
import online.faramita.bbs.module.blog.entity.Blog;

@Mapper
public interface BlogMapper {
    
    

    /**
     * 插入博客
     * @param blog
     */
    void insertBlog(Blog blog);

    /**
     * 分页查询
     * @param query
     * @return
     */
    Page<Blog> selectBlogPage(BlogPageQuery query);

    /**
     * 根据 id 查找博客
     * @param id
     * @return
     */
    Blog selectBlogById(Long id);

    /**
     * 根据 id 修改博客
     * @param blogId
     * @param blogEditDTO
     */
    void updateBlogById(Long blogId, BlogEditDTO blogEditDTO);

    /**
     * 根据 id 删除博客
     * @param blogId
     */
    void deleteBlogById(Long blogId);

    /**
     * 重置博客目录到根目录
     * @param userId
     * @param ids
     */
    void batchResetBlogFolderByAuthorIdAndFolderIds(Long userId, List<Long> ids);

    /**
     * 根据目录id查询博客
     * @param authorId 用户隔离
     * @param id
     * @param query
     * @return
     */
    Page<Blog> selectBlogPageByFolderId(Long authorId, Long id, PageQuery query);

    /**
     * 批量移动指定博客到指定目录下
     * @param userId    鉴权，用户隔离
     * @param blogIds
     * @param targetId
     * @return 影响行数
     */
    int updateBlogFolderByAuthorIdAndIds(Long userId, List<Long> blogIds, Long targetId);    
}
