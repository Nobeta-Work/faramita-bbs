package online.faramita.bbs.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.constant.MessageConstant;
import online.faramita.bbs.context.BaseContext;
import online.faramita.bbs.dto.BlogDTO;
import online.faramita.bbs.dto.BlogQueryDTO;
import online.faramita.bbs.dto.BlogUpdateDTO;
import online.faramita.bbs.dto.PageQueryDTO;
import online.faramita.bbs.entity.Blog;
import online.faramita.bbs.entity.BlogCategory;
import online.faramita.bbs.exception.PermissionException;
import online.faramita.bbs.exception.ResourceNotFoundException;
import online.faramita.bbs.mapper.BlogMapper;
import online.faramita.bbs.result.PageResult;
import online.faramita.bbs.service.BlogService;

@Service
@Slf4j
public class BlogServiceImpl implements BlogService{
    @Autowired
    private BlogMapper blogMapper;

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    public PageResult<Blog> pageQuery(Long uid, PageQueryDTO<BlogQueryDTO> pageQueryDTO) {
        // 1.提取参数
        Integer pageNum = pageQueryDTO.getPage();
        Integer pageSize = pageQueryDTO.getPageSize();
        BlogQueryDTO blogQueryDTO = pageQueryDTO.getQuery();
        if (blogQueryDTO == null) {
            blogQueryDTO = new BlogQueryDTO();
        }
        
        // 添加关键词去空格处理
        if (blogQueryDTO.getKeyword() != null) {
            blogQueryDTO.setKeyword(blogQueryDTO.getKeyword().trim());
        }

        blogQueryDTO.setAuthorId(uid);
        
        blogQueryDTO.setQueryUid(BaseContext.getCurrentId());
        blogQueryDTO.setNeedContent(false);
        
        log.info(">blogQueryDTO:{}", blogQueryDTO);
        // 2.开启PageHelper分页查询
        PageHelper.startPage(pageNum, pageSize);
        Page<Blog> pageResult = blogMapper.findBlogs(blogQueryDTO);

        log.info(">pageResult:{}", pageResult);

        Long total = pageResult.getTotal();
        List<Blog> blogList = pageResult.getResult();

        // 3.封装结果
        return new PageResult<Blog>(total, blogList);
    }

    /**
     * 创建博客
     * @param uid
     * @param blogDTO
     * @return
     */
    @Transactional
    public String createBlog(Long uid, BlogDTO blogDTO) {
        // 1.基于秒级时间戳+uuid生成bloguid
        String bloguid = (System.currentTimeMillis() / 1000) + UUID.randomUUID().toString().replace("-", "");

        // 2.获取或创建小类
        blogDTO.setAuthorId(uid);
        // 2.1 尝试查询
        if (blogDTO.getCategoryId() == null) {
            BlogCategory category = blogMapper.findCategoryByBigIdAndName(uid, blogDTO.getBigCategoryId(), blogDTO.getLittleCategoryName());
            if (category != null) {
                // 2.2.1 小类存在
                blogDTO.setCategoryId(category.getId());
            } else {
                // 2.2.2 小类不存在
                blogMapper.createCategoryByDTO(blogDTO);
            }
        }
        // 3.封装Blog类，存入数据库
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogDTO, blog);
        blog.setBloguid(bloguid);
        blog.setIsPublished(0);
        blog.setContent(">默认信息<");
        try {
            blogMapper.insertBlog(blog);
        } catch (Exception e) {
            log.error(">异常：{}", e);
        }
                        
        return blog.getBloguid();
    }

    /**
     * 根据bloguid查询博文
     * @param uid
     * @param bloguid
     * @return
     */
    public Blog getBlogByBloguid(Long uid, String bloguid) {
        // 1.构造QueryDTO
        BlogQueryDTO queryDTO = BlogQueryDTO.builder()
                        .queryUid(BaseContext.getCurrentId())
                        .authorId(uid)
                        .bloguid(bloguid)
                        .build();
        // 2.根据QueryDTO获取博文
        Blog blog = blogMapper.getBlogByDTO(queryDTO);
        // 1.1 异常：博文为空
        if (blog == null) {
            throw new ResourceNotFoundException(MessageConstant.BLOG_NOT_EXISTS);
        }
        // 1.2 异常：博文未发布且访问id不为uid
        if (blog.getIsPublished() < 1) {
            Long id = BaseContext.getCurrentId();
            if (id == null || !id.equals(uid)) {
                throw new PermissionException(MessageConstant.NO_PERMISSION);
            }
        }
        // 2.获取博文并返回
        return blog;
    }

    /**
     * 根据bloguid删除博客
     * @param bloguid
     */
    public void deleteBlogByBloguid(String bloguid) {

        blogMapper.deleteBlogByBloguid(bloguid);
        
    }

    /**
     * 更新博客
     * @param uid
     * @param updateDTO
     */
    @Transactional
    public void updateBlog(String bloguid, BlogUpdateDTO updateDTO) {
        // 1.根据bloguid查询原始博客信息
        Blog blog = getBlogByBloguid(BaseContext.getCurrentId(), bloguid);
        if (blog == null) {
            throw new ResourceNotFoundException(MessageConstant.BLOG_NOT_EXISTS);
        }

        // 2.根据uid,大类ID和小类名称，获取或创建小类，并返回其ID
        BlogCategory category = blogMapper.findCategoryByBigIdAndName(blog.getAuthorId(), updateDTO.getBigCategoryId(), updateDTO.getLittleCategoryName());
        if (category != null) {
            // 2.1 小类存在
            blog.setCategoryId(category.getId());
        } else {
            // 2.2.2 小类不存在
            category = new BlogCategory();
            category.setName(updateDTO.getLittleCategoryName());
            category.setBigCategoryId(updateDTO.getBigCategoryId());
            category.setUserId(BaseContext.getCurrentId());
            blogMapper.createCategory(category);

            blog.setCategoryId(category.getId());
        }

        // 3.复制DTO中的非空属性到原始blog对象
        BeanUtils.copyProperties(updateDTO, blog);
        
        // 4.调用Mapper执行更新
        blogMapper.updateBlog(blog);
    }
}
