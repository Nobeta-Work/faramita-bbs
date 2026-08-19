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
import cn.nobeta.bbs.module.admin.dto.RoleEditDTO;
import cn.nobeta.bbs.module.admin.dto.RolePageQuery;
import cn.nobeta.bbs.module.admin.dto.RoleSaveDTO;
import cn.nobeta.bbs.module.admin.service.AdminRoleService;
import cn.nobeta.bbs.module.admin.vo.RoleVO;

/**
 * 后台角色管理接口
 */
@RestController
@RequestMapping("/api/admin/role")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 角色列表
     * @param query
     * @return
     */
    @AuditLog(message = "后台查询角色列表",
        data = "{'pageNum': #p0.pageNum, 'pageSize': #p0.pageSize, 'keyword': #p0.keyword}"
    )
    @GetMapping("/page")
    public Result<PageResult<RoleVO>> getRolePage(@ModelAttribute RolePageQuery query) {

        PageResult<RoleVO> vo = adminRoleService.queryRolePage(query);

        return Result.success(vo);
    }

    /**
     * 创建角色
     * @param dto
     * @return
     */
    @AuditLog(message = "后台创建角色",
        data = "{'roleCode': #p0.roleCode, 'roleName': #p0.roleName, 'permIds': #p0.permIds}"
    )
    @PostMapping
    public Result<Void> createRole(@Valid @RequestBody RoleSaveDTO dto) {

        adminRoleService.createRole(dto);

        return Result.success();
    }

    /**
     * 修改角色信息、权限列表
     * @param dto
     * @return
     */
    @AuditLog(message = "后台修改角色",
        data = "{'id': #p0.id, 'roleCode': #p0.roleCode, 'roleName': #p0.roleName, 'permIds': #p0.permIds}"
    )
    @PutMapping
    public Result<Void> editRole(@Valid @RequestBody RoleEditDTO dto) {

        adminRoleService.editRole(dto);

        return Result.success();
    }

}
