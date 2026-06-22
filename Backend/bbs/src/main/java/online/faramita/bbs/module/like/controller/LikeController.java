package online.faramita.bbs.module.like.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.annotation.AuditLog;
import online.faramita.bbs.common.result.Result;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;
import online.faramita.bbs.module.like.service.LikeService;

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
}
