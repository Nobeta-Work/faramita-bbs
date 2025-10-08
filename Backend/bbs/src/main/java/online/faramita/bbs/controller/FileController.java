package online.faramita.bbs.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.result.Result;
import online.faramita.bbs.service.FileService;

/**
 * 文件相关接口
 */
@RestController
@RequestMapping("api/")
@Slf4j
@Tag(name = "文件传输相关接口", description = "处理文件上传、下载接口")
public class FileController {

    @Autowired
    private FileService fileService;
    
    /**
     * 全局头像上传接口(无需身份校验=>无关联用户)
     */
    @PostMapping("/uploadAvatar")
    @Operation(summary = "全局头像上传接口")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info(">头像文件上传<");
        String fileUuid = fileService.uploadAvatar(file);
        return Result.success(fileUuid);
    }

    /**
     * 全局头像下载接口
     * @param fileUuid
     * @param response
     * @throws IOException
     */
    @Operation(summary = "全局头像下载接口")
    @GetMapping("/downloadAvatar")
    public void downloadAvatar(@RequestParam("avatar") String fileUuid, HttpServletResponse response) throws IOException {
        fileService.downloadAvatar(fileUuid, response);
    }
}
