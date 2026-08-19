package cn.nobeta.bbs.module.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.admin.dto.AdminTagEditDTO;
import cn.nobeta.bbs.module.admin.dto.AdminTagPageQuery;
import cn.nobeta.bbs.module.admin.dto.AdminTagSaveDTO;
import cn.nobeta.bbs.module.admin.service.AdminTagService;
import cn.nobeta.bbs.module.admin.vo.AdminTagVO;

/**
 * 后台标签管理接口
 */
@RestController
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
public class AdminTagController {

    private final AdminTagService adminTagService;

    /**
     * 标签列表
     * @param query
     * @return
     */
    @AuditLog(message = "后台查询标签列表",
        data = "{'pageNum': #p0.pageNum, 'pageSize': #p0.pageSize, 'keyword': #p0.keyword}"
    )
    @GetMapping("/page")
    public Result<PageResult<AdminTagVO>> getTagPage(@ModelAttribute AdminTagPageQuery query) {

        PageResult<AdminTagVO> vo = adminTagService.queryTagPage(query);

        return Result.success(vo);
    }

    /**
     * 创建标签
     * @param dto
     * @return
     */
    @AuditLog(message = "后台创建标签", data = "{'name': #p0.name}")
    @PostMapping
    public Result<Void> createTag(@Valid @RequestBody AdminTagSaveDTO dto) {

        adminTagService.createTag(dto);

        return Result.success();
    }

    /**
     * 修改标签（含标签详情）
     * @param dto
     * @return
     */
    @AuditLog(message = "后台修改标签", data = "{'id': #p0.id, 'name': #p0.name}")
    @PutMapping
    public Result<Void> editTag(@Valid @RequestBody AdminTagEditDTO dto) {

        adminTagService.editTag(dto);

        return Result.success();
    }

}
