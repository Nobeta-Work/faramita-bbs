package cn.nobeta.bbs.module.admin.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.module.admin.dto.PermEditDTO;
import cn.nobeta.bbs.module.admin.dto.PermPageQuery;
import cn.nobeta.bbs.module.admin.dto.PermSaveDTO;
import cn.nobeta.bbs.module.admin.entity.SysPerm;
import cn.nobeta.bbs.module.admin.mapper.AdminMapper;
import cn.nobeta.bbs.module.admin.vo.PermVO;

/**
 * 后台权限管理服务
 */
@Service
@RequiredArgsConstructor
public class AdminPermService {

    private final AdminMapper adminMapper;

    /**
     * 分页查询权限
     * @param query
     * @return
     */
    public PageResult<PermVO> queryPermPage(PermPageQuery query) {

        // 0. 默认分页参数
        if (query.getPageNum() == null) query.setPageNum(1);
        if (query.getPageSize() == null) query.setPageSize(10);

        // 1. 处理 keyword
        String keyword = query.getKeyword();
        query.setKeyword(keyword == null ? null : keyword.trim());

        // 2. 分页查询权限
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();
        PageHelper.startPage(pageNum, pageSize);

        Page<SysPerm> perms = adminMapper.selectPermPage(query);
        if (perms.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 3. 组装返回体
        List<PermVO> records = perms.stream()
                .map(p -> PermVO.builder()
                        .id(p.getId())
                        .permCode(p.getPermCode())
                        .permName(p.getPermName())
                        .description(p.getDescription())
                        .createTime(p.getCreateTime())
                        .build()
                ).toList();

        return PageResult.<PermVO>builder()
                .total(perms.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(perms.getPages())
                .records(records)
                .build();
    }

    /**
     * 创建权限
     * @param dto
     */
    public void createPerm(PermSaveDTO dto) {

        // 1. 处理参数
        dto.setPermCode(dto.getPermCode().trim());
        dto.setPermName(dto.getPermName().trim());
        String description = dto.getDescription() == null ? "" : dto.getDescription().trim();

        // 2. 插入权限
        SysPerm perm = SysPerm.builder()
                .id(SnowflakeUtil.nextId())
                .permCode(dto.getPermCode())
                .permName(dto.getPermName())
                .description(description)
                .build();

        try {
            adminMapper.insertPerm(perm);
        } catch (DuplicateKeyException e) {
            throw toDuplicateException(e);
        }
    }

    /**
     * 修改权限
     * @param dto
     */
    public void editPerm(PermEditDTO dto) {

        // 1. 存在性检查
        if (adminMapper.selectPermById(dto.getId()) == null) {
            throw new BusinessException(ResultCode.PERM_NOT_FOUND);
        }

        // 2. 更新权限
        dto.setPermCode(dto.getPermCode().trim());
        dto.setPermName(dto.getPermName().trim());
        String description = dto.getDescription() == null ? "" : dto.getDescription().trim();

        SysPerm perm = SysPerm.builder()
                .id(dto.getId())
                .permCode(dto.getPermCode())
                .permName(dto.getPermName())
                .description(description)
                .build();

        try {
            adminMapper.updatePerm(perm);
        } catch (DuplicateKeyException e) {
            throw toDuplicateException(e);
        }
    }

    /**
     * 唯一键冲突转换
     * @param e
     * @return
     */
    private BusinessException toDuplicateException(DuplicateKeyException e) {
        String msg = e.getMostSpecificCause().getMessage();
        if (msg != null && msg.contains("uk_perm_code")) {
            return new BusinessException(ResultCode.PERM_CODE_DUPLICATE);
        }
        return new BusinessException(ResultCode.PERM_NAME_DUPLICATE);
    }

}
