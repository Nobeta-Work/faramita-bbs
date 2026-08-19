package cn.nobeta.bbs.module.admin.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.module.admin.dto.RoleEditDTO;
import cn.nobeta.bbs.module.admin.dto.RolePageQuery;
import cn.nobeta.bbs.module.admin.dto.RolePermBrief;
import cn.nobeta.bbs.module.admin.dto.RoleSaveDTO;
import cn.nobeta.bbs.module.admin.entity.SysRole;
import cn.nobeta.bbs.module.admin.mapper.AdminMapper;
import cn.nobeta.bbs.module.admin.vo.PermVO;
import cn.nobeta.bbs.module.admin.vo.RoleVO;

/**
 * 后台角色管理服务
 */
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminMapper adminMapper;

    /**
     * 分页查询角色（携带权限列表）
     * @param query
     * @return
     */
    public PageResult<RoleVO> queryRolePage(RolePageQuery query) {

        // 0. 默认分页参数
        if (query.getPageNum() == null) query.setPageNum(1);
        if (query.getPageSize() == null) query.setPageSize(10);

        // 1. 处理 keyword
        String keyword = query.getKeyword();
        query.setKeyword(keyword == null ? null : keyword.trim());

        // 2. 分页查询角色
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();
        PageHelper.startPage(pageNum, pageSize);

        Page<SysRole> roles = adminMapper.selectRolePage(query);
        if (roles.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 3. 批量查询权限关系
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        Collection<RolePermBrief> briefs = adminMapper.selectRolePermBriefByRoleIds(roleIds);
        Map<Long, List<PermVO>> permMap = briefs.stream().collect(Collectors.groupingBy(
                RolePermBrief::getRoleId,
                Collectors.mapping(b -> PermVO.builder()
                        .id(b.getPermId())
                        .permCode(b.getPermCode())
                        .permName(b.getPermName())
                        .build(), Collectors.toList())
        ));

        // 4. 组装返回体
        List<RoleVO> records = roles.stream()
                .map(r -> RoleVO.builder()
                        .id(r.getId())
                        .roleCode(r.getRoleCode())
                        .roleName(r.getRoleName())
                        .description(r.getDescription())
                        .createTime(r.getCreateTime())
                        .updateTime(r.getUpdateTime())
                        .perms(permMap.getOrDefault(r.getId(), Collections.emptyList()))
                        .build()
                ).toList();

        return PageResult.<RoleVO>builder()
                .total(roles.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(roles.getPages())
                .records(records)
                .build();
    }

    /**
     * 创建角色
     * @param dto
     */
    @Transactional
    public void createRole(RoleSaveDTO dto) {

        // 1. 处理参数
        dto.setRoleCode(dto.getRoleCode().trim());
        dto.setRoleName(dto.getRoleName().trim());
        String description = dto.getDescription() == null ? "" : dto.getDescription().trim();

        // 2. 唯一性检查
        if (adminMapper.selectRoleByCode(dto.getRoleCode()) != null) {
            throw new BusinessException(ResultCode.ROLE_CODE_DUPLICATE);
        }
        if (adminMapper.selectRoleByName(dto.getRoleName()) != null) {
            throw new BusinessException(ResultCode.ROLE_NAME_DUPLICATE);
        }

        // 3. 插入角色
        Long id = SnowflakeUtil.nextId();
        SysRole role = SysRole.builder()
                .id(id)
                .roleCode(dto.getRoleCode())
                .roleName(dto.getRoleName())
                .description(description)
                .build();

        try {
            adminMapper.insertRole(role);
        } catch (DuplicateKeyException e) {
            // 并发场景唯一键兜底
            throw new BusinessException(ResultCode.ROLE_CODE_DUPLICATE);
        }

        // 4. 绑定权限
        bindPerms(id, dto.getPermIds());
    }

    /**
     * 修改角色信息、权限列表
     * @param dto
     */
    @Transactional
    public void editRole(RoleEditDTO dto) {

        // 1. 存在性检查
        SysRole exist = adminMapper.selectRoleById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }

        // 2. 唯一性检查（排除自身）
        dto.setRoleCode(dto.getRoleCode().trim());
        dto.setRoleName(dto.getRoleName().trim());
        String description = dto.getDescription() == null ? "" : dto.getDescription().trim();

        SysRole sameCode = adminMapper.selectRoleByCode(dto.getRoleCode());
        if (sameCode != null && !sameCode.getId().equals(dto.getId())) {
            throw new BusinessException(ResultCode.ROLE_CODE_DUPLICATE);
        }
        SysRole sameName = adminMapper.selectRoleByName(dto.getRoleName());
        if (sameName != null && !sameName.getId().equals(dto.getId())) {
            throw new BusinessException(ResultCode.ROLE_NAME_DUPLICATE);
        }

        // 3. 更新角色信息
        SysRole role = SysRole.builder()
                .id(dto.getId())
                .roleCode(dto.getRoleCode())
                .roleName(dto.getRoleName())
                .description(description)
                .build();

        try {
            adminMapper.updateRole(role);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.ROLE_CODE_DUPLICATE);
        }

        // 4. 覆盖权限列表
        adminMapper.deleteRolePermsByRoleId(dto.getId());
        bindPerms(dto.getId(), dto.getPermIds());
    }

    /**
     * 绑定角色权限（空列表则跳过）
     * @param roleId
     * @param permIds
     */
    private void bindPerms(Long roleId, List<Long> permIds) {
        if (permIds == null || permIds.isEmpty()) {
            return;
        }
        adminMapper.batchInsertRolePerms(roleId, permIds);
    }

}
