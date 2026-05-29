package online.faramita.bbs.module.tag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.common.result.Result;
import online.faramita.bbs.module.tag.dto.TagPageQuery;
import online.faramita.bbs.module.tag.dto.TagSaveDTO;
import online.faramita.bbs.module.tag.service.TagService;
import online.faramita.bbs.module.tag.vo.TagBriefVO;

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
    @PostMapping
    public Result<TagBriefVO> saveTag(@RequestBody TagSaveDTO dto) {

        TagBriefVO vo = tagService.createTag(dto);

        return Result.success(vo);
    }

    /**
     * 关键字搜索标签
     * @param query
     * @return
     */
    @GetMapping
    public Result<PageResult<TagBriefVO>> getTagPage(
        @ModelAttribute TagPageQuery query
    ) {

        PageResult<TagBriefVO> vo = tagService.queryTagPage(query);

        return Result.success(vo);

    }
}
