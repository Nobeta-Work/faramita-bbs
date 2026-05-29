package online.faramita.bbs.module.tag.service;

import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.module.tag.dto.TagPageQuery;
import online.faramita.bbs.module.tag.dto.TagSaveDTO;
import online.faramita.bbs.module.tag.vo.TagBriefVO;

public interface TagService {

    TagBriefVO createTag(TagSaveDTO dto);

    PageResult<TagBriefVO> queryTagPage(TagPageQuery query);

}
