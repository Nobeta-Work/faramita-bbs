package online.faramita.bbs.module.folder.vo;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FolderTree {

    private Long id;                   // 目录主键id
    private String name;               // 目录名
    private Integer level;             // 层级
    private List<FolderTree> children; // 子目录
    
    @Builder.Default
    private Integer sortOrder = 0;         // 排序等级

}
