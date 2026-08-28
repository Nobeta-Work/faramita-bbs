package cn.nobeta.bbs.module.box.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.nobeta.bbs.module.box.entity.OutboxEvent;

@Mapper
public interface OutboxMapper {

    void insertEvent(OutboxEvent event);

    List<OutboxEvent> selectPendingEvents(
        @Param("limit") int limit
    );

    int claimEvent(
        @Param("id") Long id,
        @Param("waitTime") Integer waitTime
    );

    void markPublished(
        @Param("id") Long id
    );

    void markRetry(
        @Param("id") Long id,
        @Param("nextRetryTime") LocalDateTime nextRetryTime
    );

    void markFailed(
        @Param("id") Long id
    );

    int deletePublishedBefore(
        @Param("before") LocalDateTime before,
        @Param("limit") int limit
    );
}
