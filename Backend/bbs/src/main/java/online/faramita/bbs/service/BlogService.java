package online.faramita.bbs.service;

import online.faramita.bbs.dto.BlogDTO;
import online.faramita.bbs.dto.BlogQueryDTO;
import online.faramita.bbs.dto.BlogUpdateDTO;
import online.faramita.bbs.dto.PageQueryDTO;
import online.faramita.bbs.entity.Blog;
import online.faramita.bbs.result.PageResult;

public interface BlogService {

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    PageResult<Blog> pageQuery(PageQueryDTO<BlogQueryDTO> pageQueryDTO);

    /**
     * 创建博客
     * @param uid
     * @param blogDTO
     * @return
     */
    String createBlog(Long uid, BlogDTO blogDTO);

    /**
     * 博文查询
     * @param uid
     * @param bloguid
     * @return
     */
    Blog getBlogByBloguid(Long uid, String bloguid);

    /**
     * 根据bloguid删除博客
     * @param bloguid
     */
    void deleteBlogByBloguid(String bloguid);

    
    /**
     * 更新博客
     * @param uid
     * @param bloguid
     * @param updateDTO
     */
    void updateBlog(Long uid, String bloguid, BlogUpdateDTO updateDTO);

    

}
