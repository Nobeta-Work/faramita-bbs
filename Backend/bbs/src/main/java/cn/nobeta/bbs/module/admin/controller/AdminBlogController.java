package cn.nobeta.bbs.module.admin.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.admin.dto.AdminBlogPageQuery;
import cn.nobeta.bbs.module.admin.dto.AdminBlogStatusDTO;
import cn.nobeta.bbs.module.admin.service.AdminBlogService;
import cn.nobeta.bbs.module.admin.vo.AdminBlogVO;

/**
 * 后台博客管理接口
 */
@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final AdminBlogService adminBlogService;

    /**
     * 博客列表（可根据用户查询）
     * @param query
     * @return
     */
    @AuditLog(message = "后台查询博客列表",
        data = "{'pageNum': #p0.pageNum, 'pageSize': #p0.pageSize, 'keyword': #p0.keyword, 'authorId': #p0.authorId, 'isPublished': #p0.isPublished}"
    )
    @GetMapping("/page")
    public Result<PageResult<AdminBlogVO>> getBlogPage(@ModelAttribute AdminBlogPageQuery query) {

        PageResult<AdminBlogVO> vo = adminBlogService.queryBlogPage(query);

        return Result.success(vo);
    }

    /**
     * 修改博客状态（公开 / 私有）
     * @param dto
     * @return
     */
    @AuditLog(message = "后台修改博客状态",
        data = "{'id': #p0.id, 'isPublished': #p0.isPublished}"
    )
    @PutMapping
    public Result<Void> updateBlogStatus(@Valid @RequestBody AdminBlogStatusDTO dto) {

        adminBlogService.updateBlogStatus(dto);

        return Result.success();
    }

    /**
     * 删除博客
     * @param blogId
     * @return
     */
    @AuditLog(message = "后台删除博客", data = "{'blogId': #p0}")
    @DeleteMapping
    public Result<Void> deleteBlog(@RequestParam Long blogId) {

        adminBlogService.deleteBlog(blogId);

        return Result.success();
    }

}
