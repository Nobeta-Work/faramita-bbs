package online.faramita.bbs.module.file.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.common.result.Result;
import online.faramita.bbs.module.file.service.FileService;

/**
 * 文件相关接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/")
@Slf4j
@Tag(name = "文件传输相关接口", description = "处理文件上传、下载接口")
public class FileController {

    private final FileService fileService;
    
    /**
     * 全局头像上传接口(无需身份校验=>无关联用户)
     * @param file
     * @return
     */
    @PostMapping("/uploadAvatar")
    @Operation(summary = "全局头像上传接口")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info(">头像文件上传<");
        String avatarKey = fileService.uploadAvatar(file);
        return Result.success(avatarKey);
    }
    /**
     * 2026-4-18
     * v0.2.1 版本迭代，图床实现，删除下载接口
     */
    // /**
    //  * 全局头像下载接口
    //  * @param fileUuid
    //  * @param response
    //  * @throws IOException
    //  */
    // @Operation(summary = "全局头像下载接口")
    // @GetMapping("/downloadAvatar")
    // public void downloadAvatar(@RequestParam("avatar") String fileUuid, HttpServletResponse response) throws IOException {
    //     log.info(">头像文件下载<");
    //     fileService.downloadAvatar(fileUuid, response);
    // }

    /**
     * 图片上传接口
     * @param file
     * @return
     */
    @Operation(summary = "图片上传接口")
    @PostMapping("/uploadImage")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info(">图片文件上传<");
        String url = fileService.uploadImage(file);
        return Result.success(url);
    }
}
