package online.faramita.bbs.module.tag.service.impl;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.enums.ResultCode;
import online.faramita.bbs.common.exception.BusinessException;
import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.common.util.SnowflakeUtil;
import online.faramita.bbs.module.tag.dto.TagPageQuery;
import online.faramita.bbs.module.tag.dto.TagSaveDTO;
import online.faramita.bbs.module.tag.entity.Tag;
import online.faramita.bbs.module.tag.mapper.TagMapper;
import online.faramita.bbs.module.tag.service.TagService;
import online.faramita.bbs.module.tag.vo.TagBriefVO;


@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    /**
     * 创建 Tag
     * @param dto
     * @return
     */
    @Override
    public TagBriefVO createTag(TagSaveDTO dto) {
        // 1. 处理参数
        Long id = SnowflakeUtil.nextId();
        dto.setName(dto.getName().trim());

        // 2. 构造 Tag 实体
        Tag tag = Tag.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        // 3. INSERT 收集重复异常
        try {
            tagMapper.insertTag(tag);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.TAG_NAME_DUPLICATE);
        }

        // 4. TODO: 缓存预热
        

        // 5. 返回
        return TagBriefVO.builder()
                .id(id)
                .name(dto.getName())
                .build();

    }

    /**
     * 关键字搜索标签
     * @param query
     * @return
     */
    @Override
    public PageResult<TagBriefVO> queryTagPage(TagPageQuery query) {

        // 0. 限制参数
        query.setPageNum(1);
        query.setPageSize(5);


        // 1. 提取分页参数
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();

        // 2. 处理 keyword
        query.setKeyword(query.getKeyword().trim());

        // 3. 分页查询 tag
        PageHelper.startPage(pageNum, pageSize);

        Page<Tag> tags = tagMapper.selectTagPage(query);

        // 4. 构造返回体
        List<TagBriefVO> tagList = tags.stream()
                .map(t -> TagBriefVO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .build()
                ).toList();

        // 5. TODO: 缓存

        // 6. 返回
        return PageResult.<TagBriefVO>builder()
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .records(tagList)
                    .build();
    }

}
