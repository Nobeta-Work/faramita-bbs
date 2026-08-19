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
import cn.nobeta.bbs.module.admin.dto.PermEditDTO;
import cn.nobeta.bbs.module.admin.dto.PermPageQuery;
import cn.nobeta.bbs.module.admin.dto.PermSaveDTO;
import cn.nobeta.bbs.module.admin.service.AdminPermService;
import cn.nobeta.bbs.module.admin.vo.PermVO;

/**
 * 后台权限管理接口
 */
@RestController
@RequestMapping("/api/admin/perm")
@RequiredArgsConstructor
public class AdminPermController {

    private final AdminPermService adminPermService;

    /**
     * 权限列表
     * @param query
     * @return
     */
    @AuditLog(message = "后台查询权限列表",
        data = "{'pageNum': #p0.pageNum, 'pageSize': #p0.pageSize, 'keyword': #p0.keyword}"
    )
    @GetMapping("/page")
    public Result<PageResult<PermVO>> getPermPage(@ModelAttribute PermPageQuery query) {

        PageResult<PermVO> vo = adminPermService.queryPermPage(query);

        return Result.success(vo);
    }

    /**
     * 创建权限
     * @param dto
     * @return
     */
    @AuditLog(message = "后台创建权限",
        data = "{'permCode': #p0.permCode, 'permName': #p0.permName}"
    )
    @PostMapping
    public Result<Void> createPerm(@Valid @RequestBody PermSaveDTO dto) {

        adminPermService.createPerm(dto);

        return Result.success();
    }

    /**
     * 修改权限
     * @param dto
     * @return
     */
    @AuditLog(message = "后台修改权限",
        data = "{'id': #p0.id, 'permCode': #p0.permCode, 'permName': #p0.permName}"
    )
    @PutMapping
    public Result<Void> editPerm(@Valid @RequestBody PermEditDTO dto) {

        adminPermService.editPerm(dto);

        return Result.success();
    }

}
