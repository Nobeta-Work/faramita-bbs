package online.faramita.bbs.service;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

public interface FileService {

    /**
     * 上传头像文件
     * @param file
     * @return
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 下载头像文件
     * @param fileUuid
     * @param response
     */
    void downloadAvatar(String fileUuid, HttpServletResponse response);

    /**
     * 清理过期且未关联的头像文件
     * @return
     */
    int cleanExpiredUnreferencedAvatars();

    
}
