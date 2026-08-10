package cn.nobeta.bbs.module.tag.service;

import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.module.tag.dto.TagPageQuery;
import cn.nobeta.bbs.module.tag.dto.TagSaveDTO;
import cn.nobeta.bbs.module.tag.vo.TagBriefVO;

public interface TagService {

    TagBriefVO createTag(TagSaveDTO dto);

    PageResult<TagBriefVO> queryTagPage(TagPageQuery query);

}
