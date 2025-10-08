package online.faramita.bbs.mapper;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;

import online.faramita.bbs.dto.BlogDTO;
import online.faramita.bbs.dto.BlogQueryDTO;
import online.faramita.bbs.entity.Blog;
import online.faramita.bbs.entity.BlogCategory;

@Mapper
public interface BlogMapper {
    
    /**
     * 动态查询博客列表
     * @param blogQueryDTO
     * @return
     */
    Page<Blog> findBlogs(BlogQueryDTO blogQueryDTO);

    /**
     * 创建blog小类
     * @param blogDTO
     */
    void createCategoryByDTO(BlogDTO blogDTO);
    
    /**
     * 创建blog小类
     * @param category
     */
    void createCategory(BlogCategory category);

    /**
     * 创建blog博客
     * @param blog
     */
    void insertBlog(Blog blog);

    /**
     * 根据uid, big_category_id, name查询小类是否存在
     * @param blogDTO
     * @return
     */
    BlogCategory findCategoryByBigIdAndName(
        @Param("authorId") Long authorId,
        @Param("bigCategoryId") Long bigCategoryId,
        @Param("littleCategoryName") String littleCategoryName
    );

    /**
     * 根据queryDTO获取Blog
     * @param queryDTO
     * @return
     */
    Blog getBlogByDTO(BlogQueryDTO queryDTO);

    /**
     * 根据bloguid删除博客
     * @param bloguid
     */
    @Delete("delete from blog where bloguid = #{bloguid}")
    void deleteBlogByBloguid(String bloguid);

    /**
     * 更新博客
     * @param blog
     */
    void updateBlog(Blog blog);    

}
