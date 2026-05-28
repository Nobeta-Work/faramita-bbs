package online.faramita.bbs.module.folder.mapper;

import org.apache.ibatis.annotations.Mapper;

import online.faramita.bbs.module.folder.entity.Folder;

@Mapper
public interface FolderMapper {

    /**
     * 根据 id 查询目录
     * @param folderId
     * @return
     */
    Folder selectFolderById(Long folderId);

}
