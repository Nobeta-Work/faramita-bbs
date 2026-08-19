package cn.nobeta.bbs.module.admin.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.module.admin.dto.AdminTagEditDTO;
import cn.nobeta.bbs.module.admin.dto.AdminTagPageQuery;
import cn.nobeta.bbs.module.admin.dto.AdminTagSaveDTO;
import cn.nobeta.bbs.module.admin.mapper.AdminMapper;
import cn.nobeta.bbs.module.admin.vo.AdminTagVO;
import cn.nobeta.bbs.module.tag.entity.Tag;

/**
 * 后台标签管理服务
 */
@Service
@RequiredArgsConstructor
public class AdminTagService {

    private final AdminMapper adminMapper;

    /**
     * 分页查询标签
     * @param query
     * @return
     */
    public PageResult<AdminTagVO> queryTagPage(AdminTagPageQuery query) {

        // 0. 默认分页参数
        if (query.getPageNum() == null) query.setPageNum(1);
        if (query.getPageSize() == null) query.setPageSize(10);

        // 1. 处理 keyword
        String keyword = query.getKeyword();
        query.setKeyword(keyword == null ? null : keyword.trim());

        // 2. 分页查询标签
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();
        PageHelper.startPage(pageNum, pageSize);

        Page<AdminTagVO> page = adminMapper.selectAdminTagPage(query);
        if (page.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 3. 组装返回体
        return PageResult.<AdminTagVO>builder()
                .total(page.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(page.getPages())
                .records(page)
                .build();
    }

    /**
     * 创建标签
     * @param dto
     */
    public void createTag(AdminTagSaveDTO dto) {

        // 1. 处理参数
        dto.setName(dto.getName().trim());
        String description = dto.getDescription() == null ? "" : dto.getDescription().trim();

        // 2. 插入标签
        Tag tag = Tag.builder()
                .id(SnowflakeUtil.nextId())
                .name(dto.getName())
                .description(description)
                .build();

        try {
            adminMapper.insertAdminTag(tag);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.TAG_NAME_DUPLICATE);
        }
    }

    /**
     * 修改标签（含标签详情）
     * @param dto
     */
    public void editTag(AdminTagEditDTO dto) {

        // 1. 存在性检查
        Tag exist = adminMapper.selectTagById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        // 2. 更新标签
        dto.setName(dto.getName().trim());
        String description = dto.getDescription() == null ? "" : dto.getDescription().trim();

        Tag tag = Tag.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(description)
                .build();

        try {
            adminMapper.updateAdminTag(tag);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.TAG_NAME_DUPLICATE);
        }
    }

}
