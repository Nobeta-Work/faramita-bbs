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
import cn.nobeta.bbs.module.admin.dto.AdminUserEditDTO;
import cn.nobeta.bbs.module.admin.dto.AdminUserPageQuery;
import cn.nobeta.bbs.module.admin.service.AdminUserService;
import cn.nobeta.bbs.module.admin.vo.AdminUserVO;

/**
 * 后台用户管理接口
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 用户列表
     * @param query
     * @return
     */
    @AuditLog(message = "后台查询用户列表",
        data = "{'pageNum': #p0.pageNum, 'pageSize': #p0.pageSize, 'keyword': #p0.keyword, 'status': #p0.status}"
    )
    @GetMapping("/page")
    public Result<PageResult<AdminUserVO>> getUserPage(@ModelAttribute AdminUserPageQuery query) {

        PageResult<AdminUserVO> vo = adminUserService.queryUserPage(query);

        return Result.success(vo);
    }

    /**
     * 修改用户状态、角色
     * @param dto
     * @return
     */
    @AuditLog(message = "后台修改用户",
        data = "{'id': #p0.id, 'status': #p0.status, 'roleIds': #p0.roleIds}"
    )
    @PutMapping
    public Result<Void> editUser(@Valid @RequestBody AdminUserEditDTO dto) {

        adminUserService.editUser(dto);

        return Result.success();
    }

    /**
     * 封禁用户
     * @param userId
     * @return
     */
    @AuditLog(message = "后台封禁用户", data = "{'userId': #p0}")
    @DeleteMapping
    public Result<Void> deleteUser(@RequestParam Long userId) {

        adminUserService.deleteUser(userId);

        return Result.success();
    }

}
