package online.faramita.bbs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.constant.MessageConstant;
import online.faramita.bbs.context.BaseContext;
import online.faramita.bbs.dto.ProfileDTO;
import online.faramita.bbs.exception.PermissionException;
import online.faramita.bbs.result.Result;
import online.faramita.bbs.service.UserService;
import online.faramita.bbs.vo.LoginVO;
import online.faramita.bbs.vo.ProfileVO;

/**
 * 用户相关接口
 */
@RequestMapping("api/{uid}")
@RestController
@Slf4j
@Tag(name = "用户相关接口", description = "个人资料页接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查询个人资料页
     * @param uid 被查询用户id
     * @return
     */
    @GetMapping("")
    @Operation(summary = "查询个人资料页")
    public Result<ProfileVO> getProfile(@PathVariable("uid") Long uid) {
        log.info(">查询页面:{}<", uid);
        ProfileVO profileVO = userService.getProfileByUid(uid);
        return Result.success(profileVO);
    }

    /**
     * 更新用户头像(需身份校验)(直接)
     * @param uid
     * @param file
     * @return
     */
    @Operation(summary = "更新用户头像(直接)")
    @PostMapping("/upload/avatar")
    public Result<String> updateAvatar(@PathVariable("uid") Long uid,
    @RequestParam("file") MultipartFile file) {
        // 校验请求者身份
        validatePermission(uid);

        // 上传文件并更新数据库
        log.info(">头像更新:{}<",uid);
        String fileUuid = userService.updateAvatar(uid, file);

        log.info(">头像更新成功:{}<", uid);
        return Result.success(fileUuid);
    }

    /**
     * 更新个人资料(需身份校验)
     * @param uid
     * @param profileDTO
     * @return
     */
    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@PathVariable("uid") Long uid,
    @RequestBody ProfileDTO profileDTO) {
        // 身份校验
        validatePermission(uid);

        userService.updateProfile(profileDTO);

        return Result.success();
    }

    /**
     * 根据token获得用户信息
     * @return
     */
    @GetMapping("/current")
    public Result<LoginVO> getCurrentUserInfo() {
        Long id = BaseContext.getCurrentId();
        log.info(">根据token查询用户信息:{}<", id);
        LoginVO loginVO = userService.getCurrentUserInfo(id);
        return Result.success(loginVO);
    }

    // ? 功能方法
    // 身份校验
    private void validatePermission(Long uid) {
        Long id = BaseContext.getCurrentId();
        if (id != null && id.equals(uid)) {
            return;
        }
        throw new PermissionException(MessageConstant.NO_PERMISSION);
    }
}
