package online.faramita.bbs.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库头像文件信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "数据库头像文件信息实体类")
public class AvatarInfo {
    @Schema(title = "id", description = "主键")
    private Long id;
    @Schema(title = "存储文件名", description = "唯一标识索引")
    private String fileUuid;
    @Schema(title = "关联判断", description = "头像是否关联用户")
    private Integer isReferenced;
    @Schema(title = "关联用户uid", description = "指向用户")
    private Long uid;
    @Schema(title = "过期时间", description = "未关联头像定期被清理的时间")
    private LocalDateTime expireTime;
}
