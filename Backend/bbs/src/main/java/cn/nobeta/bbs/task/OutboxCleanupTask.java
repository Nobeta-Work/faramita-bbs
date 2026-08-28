package cn.nobeta.bbs.task;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.nobeta.bbs.module.box.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxCleanupTask {

    private static final int RETENTION_DAYS = 7;
    private static final int BATCH_SIZE = 1000;

    private final OutboxMapper outboxMapper;

    @Scheduled(cron = "0 30 * * * ?")
    public void cleanPublishedEvents() {
        int deleted = outboxMapper.deletePublishedBefore(
            LocalDateTime.now().minusDays(RETENTION_DAYS),
            BATCH_SIZE
        );
        if (deleted > 0) {
            log.info("Cleaned published outbox events, count={}", deleted);
        }
    }
}
