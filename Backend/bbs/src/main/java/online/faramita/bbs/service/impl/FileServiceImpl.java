package online.faramita.bbs.service.impl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public String uploadAvatar(MultipartFile file) {
        // 1.文件校验
        validateAvatar(file);

        // 2.拼接 avatarKey
        // 目标格式：`year/month/day/uuid.extension`
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.lastIndexOf(".") < 0) {
            throw new FileException(MessageConstant.FILE_TYPE_ERROR);
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        LocalDate now = LocalDate.now();
        String datePath = now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth() + "/";
        String avatarKey = datePath + fileName;

        // 3.写入磁盘
        // 目标路径：`${rootpath}/avatar/year/month/day/uuid.extension`
        File dest = new File(fileConfig.getAvatar().getRootPath(), avatarKey);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new FileException(MessageConstant.FILE_ERROR);
        }
        // 4.构筑 avatarInfo 写入数据库
        AvatarInfo fileInfo = AvatarInfo.builder()
                .fileUuid(avatarKey)
                .isReferenced(0)
                .expireTime(LocalDateTime.now().plusHours(fileConfig.getAvatar().getExpire()))
                .build();
        fileMapper.insertAvatar(fileInfo);

        return avatarKey;
    }

    @Override
    public int cleanExpiredUnreferencedAvatars() {
        log.info("Start cleaning expired avatars");
        LocalDateTime currentTime = LocalDateTime.now();

        List<AvatarInfo> expiredAvatars = fileMapper.selectExpiredUnreferencedAvatars(currentTime);
        if (expiredAvatars.isEmpty()) {
            log.info("No expired avatars to clean");
            return 0;
        }

        int successCount = 0;
        List<Long> successIds = new ArrayList<>();

        for (AvatarInfo avatar : expiredAvatars) {
            File file = new File(fileConfig.getAvatar().getRootPath(), avatar.getFileUuid());
            if (file.exists() && file.delete()) {
                successCount++;
                successIds.add(avatar.getId());
            } else {
                log.warn("Failed to delete avatar file: {}", avatar.getFileUuid());
            }
        }

        if (!successIds.isEmpty()) {
            fileMapper.batchDeleteByIds(successIds);
            log.info("Deleted {} avatar records from database", successIds.size());
        }

        log.info("Avatar cleanup finished, found {}, deleted {}", expiredAvatars.size(), successCount);
        return successCount;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        validateImage(file);

        try {
            return githubFileUtil.upload(file);
        } catch (IOException e) {
            log.error("Image upload failed", e);
            throw new FileException(MessageConstant.FILE_ERROR);
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileException(MessageConstant.FILE_IS_NULL);
        }
        if (file.getSize() > fileConfig.getAvatar().getMaxSize()) {
            throw new FileException(MessageConstant.FILE_OUT_SIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !fileConfig.getImage().getAcceptTypes().contains(contentType)) {
            throw new FileException(MessageConstant.FILE_TYPE_ERROR);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileException(MessageConstant.FILE_IS_NULL);
        }
        if (file.getSize() > fileConfig.getImage().getMaxSize()) {
            throw new FileException(MessageConstant.FILE_OUT_SIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !fileConfig.getImage().getAcceptTypes().contains(contentType)) {
            throw new FileException(MessageConstant.FILE_TYPE_ERROR);
        }
    }
}
