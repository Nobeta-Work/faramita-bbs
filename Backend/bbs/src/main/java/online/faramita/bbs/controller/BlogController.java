package online.faramita.bbs.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.constant.MessageConstant;
import online.faramita.bbs.context.BaseContext;
import online.faramita.bbs.dto.BlogDTO;
import online.faramita.bbs.dto.BlogPageQueryDTO;
import online.faramita.bbs.dto.BlogQueryDTO;
import online.faramita.bbs.dto.BlogUpdateDTO;
import online.faramita.bbs.dto.PageQueryDTO;
import online.faramita.bbs.entity.Blog;
import online.faramita.bbs.exception.PermissionException;
import online.faramita.bbs.result.PageResult;
import online.faramita.bbs.result.Result;
import online.faramita.bbs.service.BlogService;

/**
 * 博客相关接口
 */
@RestController
@RequestMapping("api/{uid}/blog")
@Slf4j
@Tag(name = "博客相关接口", description = "博客相关接口")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 分页查询Blog(身份校验)
     * @param id
     * @param pageQueryDTO
     * @return
     */
    @Operation(summary = "分页查询接口")
    @GetMapping("")
    public Result<PageResult<Blog>> pageQuery(@PathVariable("uid") Long uid,
    @ModelAttribute BlogPageQueryDTO blogPageQueryDTO) {
        log.info("发起分页查询:bbs/{}/blog", uid);
        // 封装查询参数
        BlogQueryDTO blogQueryDTO = new BlogQueryDTO();
        BeanUtils.copyProperties(blogPageQueryDTO, blogQueryDTO);
        PageQueryDTO<BlogQueryDTO> pageQueryDTO = new PageQueryDTO<>();
        pageQueryDTO.setPage(blogPageQueryDTO.getPage());
        pageQueryDTO.setPageSize(blogPageQueryDTO.getPageSize());
        pageQueryDTO.setQuery(blogQueryDTO);

        PageResult<Blog> pageResult = blogService.pageQuery(uid, pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 创建博客(身份校验)
     * @param uid
     * @param blogDTO
     * @return
     */
    @PostMapping("create")
    @Operation(summary = "创建博客接口")
    public Result<String> createBlog(@PathVariable("uid") Long uid,
    @RequestBody BlogDTO blogDTO) {
        // 身份校验
        validatePermission(uid);
        
        log.info(">{}发起了创建博客请求<", uid);
        String bloguid =  blogService.createBlog(uid, blogDTO);

        return Result.success(bloguid);

    }

    /**
     * 博文查询接口(身份+博文发布校验)
     * @param uid
     * @param bloguid
     * @return
     */
    @GetMapping("/{bloguid}")
    @Operation(summary = "博文查询接口")
    public Result<Blog> getBlogByBloguid(@PathVariable("uid") Long uid,
    @PathVariable("bloguid") String bloguid) {

        Blog blog = blogService.getBlogByBloguid(uid, bloguid);

        return Result.success(blog);
    }

    /**
     * 删除博客(身份校验)
     * @param uid
     * @param bloguid
     * @return
     */
    @DeleteMapping("/{bloguid}")
    @Operation(summary = "删除博客接口")
    public Result<Void> deleteBlogByBloguid(@PathVariable("uid") Long uid,
    @PathVariable("bloguid") String bloguid) {
        // 身份校验
        validatePermission(uid);

        log.info(">{}发起删除博客{}的请求<", uid, bloguid);
        blogService.deleteBlogByBloguid(bloguid);

        log.info(">{}已删除博客{}<", uid, bloguid);
        return Result.success();
    }

    /**
     * 更新博客(身份校验)
     * @param uid
     * @param blog
     * @return
     */
    @PutMapping("/{bloguid}")
    public Result<Void> updateBlog(@PathVariable("uid") Long uid,
    @PathVariable("bloguid") String bloguid,
    @Valid @RequestBody BlogUpdateDTO updateDTO) {
        // 身份校验
        validatePermission(uid);
        log.info(">{}发起了更新{}博客的请求<", uid, bloguid);
        blogService.updateBlog(bloguid, updateDTO);

        log.info(">{}更新博客{}成功<", uid, bloguid);
        return Result.success();
    }

    // ? 功能方法
    // 身份校验
    private void validatePermission(Long uid) {
        Long id = BaseContext.getCurrentId();
        if (id != null && id.equals(uid)) {
            return;
        }
        log.error(">Controller拦截：身份验证失败,id:{},uid:{}<", id, uid);
        throw new PermissionException(MessageConstant.NO_PERMISSION);
    }
}
