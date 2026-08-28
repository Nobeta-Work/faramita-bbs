package cn.nobeta.bbs.module.like.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.annotation.RateLimit;
import cn.nobeta.bbs.common.enums.Scene;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;
import cn.nobeta.bbs.module.like.service.LikeService;

@RequestMapping("/api/like")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 点赞博客 (toggle 设计)
     * @param loginUser
     * @param id
     * @return
     */
    @RateLimit(scene = Scene.WRITE)
    @AuditLog(message = "切换博客点赞状态", data = "{'blogId': #p1}")
    @PostMapping("/blogs/{id}")
    public Result<Integer> toggleBlogLike(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @PathVariable Long id
    ) {

        Long userId = loginUser.getUser().getId();

        Integer likeCount = likeService.toggleBlogLike(userId, id);

        return Result.success(likeCount);

    }

    /**
     * 点赞评论 (toggle 设计)
     */
    @RateLimit(scene = Scene.WRITE)
    @AuditLog(message = "切换评论点赞状态", data = "{'commentId': #p1}")
    @PostMapping("/comments/{id}")
    public Result<Integer> toggleCommentLike(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @PathVariable Long id
    ) {
        Long userId = loginUser.getUser().getId();
        Integer likeCount = likeService.toggleCommentLike(userId, id);
        return Result.success(likeCount);
    }
}
