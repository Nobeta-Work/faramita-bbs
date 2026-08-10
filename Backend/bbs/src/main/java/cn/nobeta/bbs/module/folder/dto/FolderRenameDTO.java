package cn.nobeta.bbs.module.folder.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderRenameDTO {

    @Size(min = 1, max = 20, message = "标题字符限制 1-20 字")
    private String name;

}
