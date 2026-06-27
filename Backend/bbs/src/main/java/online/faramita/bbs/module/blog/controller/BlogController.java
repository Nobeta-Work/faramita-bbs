package online.faramita.bbs.module.blog.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.common.annotation.AuditLog;
import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.common.result.Result;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;
import online.faramita.bbs.module.blog.dto.BlogEditDTO;
import online.faramita.bbs.module.blog.dto.BlogPageQuery;
import online.faramita.bbs.module.blog.dto.BlogSaveDTO;
import online.faramita.bbs.module.blog.service.BlogService;
import online.faramita.bbs.module.blog.vo.BlogPrivateDetailVO;
import online.faramita.bbs.module.blog.vo.BlogPublicBriefVO;
import online.faramita.bbs.module.blog.vo.BlogPublicDetailVO;

/**
 * 博客相关接口
 */
@RestController
@RequestMapping("/api/blogs")
@Slf4j
@Tag(name = "博客相关接口", description = "博客相关接口")
@Validated
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /**
     * 分页查询公开 Blog (简要)
     * @param blogPageQuery
     * @return
     */
    @AuditLog(message = "请求获取博客公开页列表", 
        data = "{'pageNum': #p1.pageNum, 'pageSize': #p1.pageSize, 'keyword': #p1.keyword, 'authorId': #p1.authorId}"
    )
    @GetMapping("/page")
    public Result<PageResult<BlogPublicBriefVO>> getPublicBlogPage(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @ModelAttribute BlogPageQuery blogPageQuery
    ) {

        if (loginUser != null) {
            blogPageQuery.setUserId(loginUser.getUser().getId());
        } else {
            blogPageQuery.setUserId(null);
        }

        PageResult<BlogPublicBriefVO> vo = blogService.queryPublicBlogPage(blogPageQuery);

        return Result.success(vo);
        
    }

    /**
     * 创建博客接口
     * @param loginUser
     * @param blogSaveDTO
     * @return
     */
    @AuditLog(message = "创建博客", data = "{'title': #p1.title}")
    @PostMapping("/me")
    public Result<Long> saveBlog(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @Valid @RequestBody BlogSaveDTO blogSaveDTO
    ) {

        Long userId = loginUser.getUser().getId();

        Long blogId = blogService.addBlogByUserId(userId, blogSaveDTO);

        return Result.success(blogId);
    }

    /**
     * 查询指定公开博客详情
     * @param id
     * @return
     */
    @AuditLog(message = "访问公开博客详情", data = "{'blogId': #p0}")
    @GetMapping("/{id}")
    public Result<BlogPublicDetailVO> getPublicBlog(@PathVariable Long id) {

        BlogPublicDetailVO vo = blogService.getPublicBlogById(id);

        return Result.success(vo);
    }

    /**
     * 查询个人博客详情
     * @param id
     * @return
     */
    @AuditLog(message = "访问个人博客编辑详情", data = "{'blogId': #p0}")
    @GetMapping("/me/{id}")
    public Result<BlogPrivateDetailVO> getPrivateBlog(@PathVariable Long id) {

        BlogPrivateDetailVO vo = blogService.getPrivateBlogById(id);

        return Result.success(vo);
    }

    /**
     * 修改个人博客
     * @param blogId
     * @param blogEditDTO
     * @return
     */
    @AuditLog(message = "修改个人博客", 
        data = "{'blogId': #p0, 'folderId': #p1.folderId, 'isPublished': #p1.isPublished, 'title': #p1.title}"
    )
    @PutMapping("/me/{id}")
    public Result<Void> editPrivateBlog(
        @PathVariable(value = "id") Long blogId,
        @Valid @RequestBody BlogEditDTO blogEditDTO
    ) {

        blogService.editBlogById(blogId, blogEditDTO);        

        return Result.success();
    }

    /**
     * 删除个人博客
     * @param id
     * @return
     */
    @AuditLog(message = "删除个人博客", data = "{'blogId': #p0}")
    @DeleteMapping("/me/{id}")
    public Result<Void> deletePrivateBlog(@PathVariable Long id) {

        blogService.deleteBlogById(id);

        return Result.success();
    }
}
