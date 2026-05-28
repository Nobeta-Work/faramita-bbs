package online.faramita.bbs.module.folder.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Folder {

    private Long id;        // 主键 id: 0:根目录
    private Long authorId;  // 作者 id
    private Long parentId;  // 父目录 id
    private String name;    // 目录名
    private String path;    // 维护目录路径
    private Integer level;  // 层级深度 0:根目录
    private Integer sortOrder; // 排序等级，v0.3.0 引入但无效
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 修改时间
    
}
