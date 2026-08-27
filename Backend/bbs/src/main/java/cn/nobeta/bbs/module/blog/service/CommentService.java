package cn.nobeta.bbs.module.blog.service;

import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.module.blog.dto.CommentPageQuery;
import cn.nobeta.bbs.module.blog.dto.CommentSaveDTO;
import cn.nobeta.bbs.module.blog.vo.CommentVO;

public interface CommentService {

    PageResult<CommentVO> queryCommentPage(Long blogId, CommentPageQuery query);

    Long addComment(Long userId, CommentSaveDTO dto);

    void deleteComment(Long userId, Long commentId);
}
