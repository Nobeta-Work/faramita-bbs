package online.faramita.bbs.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.service.FileService;

/**
 * 定时清理冗余头像文件
 */
@Component
@Slf4j
public class AvatarCleanupTask {
    @Autowired
    private FileService fileService;

    @Scheduled(cron = "0 0 0/8 * * ?")
    public void cleanupExpiredAvatars() {
        log.info("===== 开始执行冗余头像清理任务 =====");
        fileService.cleanExpiredUnreferencedAvatars();
        log.info("===== 清理完成 =====");
    }
}
