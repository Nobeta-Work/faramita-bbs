package cn.nobeta.bbs.module.tag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.tag.dto.TagPageQuery;
import cn.nobeta.bbs.module.tag.dto.TagSaveDTO;
import cn.nobeta.bbs.module.tag.service.TagService;
import cn.nobeta.bbs.module.tag.vo.TagBriefVO;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 创建 Tag
     * @param dto
     * @return
     */
    @AuditLog(message = "创建标签", data = "{'name': #p0.name}")
    @PostMapping
    public Result<TagBriefVO> saveTag(@Valid @RequestBody TagSaveDTO dto) {

        TagBriefVO vo = tagService.createTag(dto);

        return Result.success(vo);
    }

    /**
     * 关键字搜索标签
     * @param query
     * @return
     */
    @AuditLog(message = "查询标签列表", data = "{'pageNum': #p0.pageNum, 'pageSize': #p0.pageSize, 'keyword': #p0.keyword}")
    @GetMapping
    public Result<PageResult<TagBriefVO>> getTagPage(
        @ModelAttribute TagPageQuery query
    ) {

        PageResult<TagBriefVO> vo = tagService.queryTagPage(query);

        return Result.success(vo);

    }
}
