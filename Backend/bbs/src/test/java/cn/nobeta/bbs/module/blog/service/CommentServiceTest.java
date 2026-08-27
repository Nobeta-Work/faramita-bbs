package cn.nobeta.bbs.module.blog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.nobeta.bbs.module.blog.dto.CommentSaveDTO;
import cn.nobeta.bbs.module.blog.entity.Blog;
import cn.nobeta.bbs.module.blog.entity.Comment;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.mapper.CommentMapper;
import cn.nobeta.bbs.module.blog.service.impl.CommentServiceImpl;
import cn.nobeta.bbs.module.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    void whenReplyingToReply_useSameRootComment() {
        CommentSaveDTO dto = new CommentSaveDTO();
        dto.setBlogId(10L);
        dto.setParentId(30L);
        dto.setContent(" reply ");

        when(blogMapper.selectBlogById(10L)).thenReturn(
                Blog.builder().id(10L).isPublished(1).build());
        when(commentMapper.selectCommentById(30L)).thenReturn(
                Comment.builder()
                        .id(30L)
                        .blogId(10L)
                        .userId(2L)
                        .parentId(20L)
                        .rootId(20L)
                        .status(1)
                        .build());

        commentService.addComment(1L, dto);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insertComment(captor.capture());
        assertEquals(20L, captor.getValue().getRootId());
        assertEquals(30L, captor.getValue().getParentId());
        assertEquals("reply", captor.getValue().getContent());
        verify(blogMapper).incrementCommentsCount(10L);
    }

    @Test
    void whenDeletingOwnComment_decrementCommentCount() {
        when(commentMapper.selectCommentById(30L)).thenReturn(
                Comment.builder().id(30L).blogId(10L).userId(1L).status(1).build());
        when(commentMapper.softDeleteCommentByIdAndUserId(30L, 1L)).thenReturn(1);

        commentService.deleteComment(1L, 30L);

        verify(commentMapper).softDeleteCommentByIdAndUserId(30L, 1L);
        verify(blogMapper).decrementCommentsCount(10L);
    }
}
