package cn.nobeta.bbs.module.blog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;

import cn.nobeta.bbs.module.blog.entity.Comment;

@Mapper
public interface CommentMapper {

    void insertComment(Comment comment);

    Comment selectCommentById(@Param("id") Long id);

    Page<Comment> selectRootCommentPage(
            @Param("blogId") Long blogId,
            @Param("sortOrder") String sortOrder);

    List<Comment> selectRepliesByRootIds(
            @Param("blogId") Long blogId,
            @Param("rootIds") List<Long> rootIds);

    int softDeleteCommentByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId);

    void deleteCommentsByBlogId(@Param("blogId") Long blogId);
}
