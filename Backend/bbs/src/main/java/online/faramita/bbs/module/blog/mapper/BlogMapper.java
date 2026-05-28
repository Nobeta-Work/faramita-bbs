package online.faramita.bbs.module.blog.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.github.pagehelper.Page;

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
}
