package online.faramita.bbs.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.config.FileConfig;
import online.faramita.bbs.constant.MessageConstant;
import online.faramita.bbs.entity.AvatarInfo;
import online.faramita.bbs.exception.FileException;
import online.faramita.bbs.mapper.FileMapper;
import online.faramita.bbs.service.FileService;
import online.faramita.bbs.util.GithubFileUtil;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private GithubFileUtil githubFileUtil;

    /**
     * 上传头像文件
     * @param file
     * @return
     */
    public String uploadAvatar(MultipartFile file) {
        // 1.校验文件
        validateAvatar(file);

        // 2.生成file_uuid : 时间戳 + uuid + 后缀名
        String originalFileName = file.getOriginalFilename();
        // 后缀名
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."))
                        .toLowerCase(); // 统一小写
        // uuid
        String uuid = UUID.randomUUID().toString().replace("-", "");    // 纯文本UUID
        // 秒级时间戳
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        String fileUuid = timestamp + uuid + extension;

        // 3.拼接磁盘路径
        String path = fileConfig.getAvatar().getRootPath() + fileUuid;
        File diskFile = new File(path);

        // 4.确保路径存在
        if (!diskFile.getParentFile().exists()) {
            diskFile.getParentFile().mkdirs();  // 递归创建目录
        }

        // 5.写入磁盘
        try {
            file.transferTo(diskFile);
            log.info("头像上传成功，保存路径：{}", diskFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("头像写入磁盘失败");
            throw new FileException(MessageConstant.FILE_ERROR);
        }

        // 6.存入数据库
        AvatarInfo fileInfo = AvatarInfo.builder()
                        .fileUuid(fileUuid)
                        .isReferenced(0)
                        .expireTime(LocalDateTime.now().plusHours(fileConfig.getAvatar().getExpire()))
                        .build();
        fileMapper.insertAvatar(fileInfo);

        // 7.返回file_uuid
        return fileUuid;
    }

    /**
     * 下载头像文件
     * @param fileUuid
     * @param response
     */
    public void downloadAvatar(String fileUuid, HttpServletResponse response) {
        // 1.校验文件
        AvatarInfo avatarInfo = fileMapper.selectByFileUuid(fileUuid);
        if (avatarInfo == null) {
            throw new FileException(MessageConstant.FILE_IS_NULL);
        }
        // 2.定位磁盘文件
        String path = fileConfig.getAvatar().getRootPath() + fileUuid;
        File diskFile = new File(path);
        if (!diskFile.exists()) {
            log.error("头像文件丢失, fileUuid:{}, path:{}", fileUuid, path);
            throw new FileException(MessageConstant.FILE_IS_NULL);
        }

        // 3.Content-Type
        String contentType = "application/octet-stream";

        // 4.返回图片流
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline;filename=" +
                URLEncoder.encode(fileUuid, StandardCharsets.UTF_8));

        try (InputStream in = new FileInputStream(diskFile);
            OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != - 1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            log.error("文件下载失败");
            throw new FileException(MessageConstant.FILE_ERROR);
        }
    }

    /**
     * 清理过期且未关联的头像文件
     * @return
     */
    public int cleanExpiredUnreferencedAvatars() {
        log.info(">开始清理过期头像图片<");
        LocalDateTime currentTime = LocalDateTime.now();

        // 查询所有过期未关联图片
        List<AvatarInfo> expiredAvatars = fileMapper.selectExpiredUnreferencedAvatars(currentTime);
        int totalCount = expiredAvatars.size();

        if (totalCount == 0) {
            log.info(">头像图片无需清理<");
            return 0;
        }

        int successCount = 0;
        List<Long> successIds = new ArrayList<>();

        // 遍历删除文件
        for (AvatarInfo avatar : expiredAvatars) {
            String filePath = fileConfig.getAvatar().getRootPath() + avatar.getFileUuid();
            File file = new File(filePath);

            // 删除文件
            if (file.exists() && file.delete()) {
                successCount++;
                successIds.add(avatar.getId());
            } else {
                log.warn(">删除文件失败:{}<", avatar.getFileUuid());
            }
        }

        // 删除数据库记录
        if (!successIds.isEmpty()) {
            fileMapper.batchDeleteByIds(successIds);
            log.info(">成功从数据库中清理{}条冗余数据<", successIds.size());
        }
        
        log.info(">头像图片库清理完成，共发现{}个文件，成功清理{}个文件<", totalCount, successCount);
        return successCount;
    }

    /**
     * 上传图片文件
     * @param file
     * @return
     * @throws IOException 
     */
    public String uploadImage(MultipartFile file) {
        // 1.校验图片文件
        validateImage(file);

        // 2.调用工具类上传Github图床
        String imageUrl;
        try {
            imageUrl = githubFileUtil.upload(file);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new FileException(MessageConstant.FILE_ERROR);
        }

        // 3.返回url
        return imageUrl;
    }

    // ? 功能方法
    // 校验头像文件非空、大小
    private void validateAvatar(MultipartFile file) {
        // 1.非空校验
        if (file == null || file.isEmpty()) {
            throw new FileException(MessageConstant.FILE_IS_NULL);
        }
        // 2.大小校验
        if (file.getSize() > fileConfig.getAvatar().getMaxSize()) {
            throw new FileException(MessageConstant.FILE_OUT_SIZE);
        }
    }
    // 校验图片文件非空、大小、文件类型
    private void validateImage(MultipartFile file) {
        // 1.非空校验
        if (file == null || file.isEmpty()) {
            throw new FileException(MessageConstant.FILE_IS_NULL);
        }

        // 2.大小校验
        if (file.getSize() > fileConfig.getImage().getMaxSize()) {
            throw new FileException(MessageConstant.FILE_OUT_SIZE);
        }

        // 3.文件类型校验
        String contentType = file.getContentType();
        if (!fileConfig.getImage().getAcceptTypes().contains(contentType)) {
            throw new FileException(MessageConstant.FILE_TYPE_ERROR);
        }
    }

    
}
