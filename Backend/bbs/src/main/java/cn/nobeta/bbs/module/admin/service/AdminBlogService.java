package cn.nobeta.bbs.module.admin.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.module.admin.dto.AdminBlogPageQuery;
import cn.nobeta.bbs.module.admin.dto.AdminBlogStatusDTO;
import cn.nobeta.bbs.module.admin.mapper.AdminMapper;
import cn.nobeta.bbs.module.admin.vo.AdminBlogVO;
import cn.nobeta.bbs.module.blog.entity.Blog;

/**
 * 后台博客管理服务
 */
@Service
@RequiredArgsConstructor
public class AdminBlogService {

    private final AdminMapper adminMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询博客（带作者名），可根据用户查询
     * @param query
     * @return
     */
    public PageResult<AdminBlogVO> queryBlogPage(AdminBlogPageQuery query) {

        // 0. 默认分页参数
        if (query.getPageNum() == null) query.setPageNum(1);
        if (query.getPageSize() == null) query.setPageSize(10);

        // 1. 处理 keyword
        String keyword = query.getKeyword();
        query.setKeyword(keyword == null ? null : keyword.trim());

        // 2. 分页查询博客
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();
        PageHelper.startPage(pageNum, pageSize);

        Page<AdminBlogVO> page = adminMapper.selectAdminBlogPage(query);
        if (page.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 3. 组装返回体
        return PageResult.<AdminBlogVO>builder()
                .total(page.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(page.getPages())
                .records(page)
                .build();
    }

    /**
     * 修改博客状态（公开 / 私有）
     * @param dto
     */
    public void updateBlogStatus(AdminBlogStatusDTO dto) {

        // 1. 存在性检查
        Blog blog = adminMapper.selectBlogById(dto.getId());
        if (blog == null) {
            throw new BusinessException(ResultCode.BLOG_NOT_FOUND);
        }

        // 2. 更新状态
        adminMapper.updateBlogStatus(dto.getId(), dto.getIsPublished());
    }

    /**
     * 删除博客（含标签关系、点赞缓存）
     * @param blogId
     */
    @Transactional
    public void deleteBlog(Long blogId) {

        // 1. 存在性检查
        Blog blog = adminMapper.selectBlogById(blogId);
        if (blog == null) {
            throw new BusinessException(ResultCode.BLOG_NOT_FOUND);
        }

        // 2. 删除标签关系 + 博客本体
        adminMapper.deleteBlogTagsByBlogId(blogId);
        adminMapper.deleteBlogById(blogId);

        // 3. 清理点赞缓存
        redisTemplate.delete(RedisKeys.LIKE_BLOG.getFullKey(blogId));
    }

}
