package online.faramita.bbs.module.file.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;


public interface FileService {

    /**
     * 上传头像文件
     * @param file
     * @return
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 2026-4-18
     * v0.2.1 版本迭代，图床实现，删除下载接口
     */
    // /**
    //  * 下载头像文件
    //  * @param fileUuid
    //  * @param response
    //  */
    // void downloadAvatar(String fileUuid, HttpServletResponse response);

    /**
     * 清理过期且未关联的头像文件
     * @return
     */
    int cleanExpiredUnreferencedAvatars();

    /**
     * 上传图片文件
     * @param file
     * @return
     * @throws IOException 
     */
    String uploadImage(MultipartFile file);

}
