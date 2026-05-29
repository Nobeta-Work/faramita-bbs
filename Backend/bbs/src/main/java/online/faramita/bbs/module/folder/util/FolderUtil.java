package online.faramita.bbs.module.folder.util;

import java.util.ArrayList;

import online.faramita.bbs.module.folder.entity.Folder;
import online.faramita.bbs.module.folder.vo.FolderTree;

public class FolderUtil {

    public static FolderTree buildRootByUserId(Long userId) {
        return FolderTree.builder()
                .id(0L)
                .name("root of " + userId)
                .level(0)
                .children(new ArrayList<>())
                .build();
    }

    public static FolderTree folderToFolderTree(Folder folder) {
        return FolderTree.builder()
                .id(folder.getId())
                .name(folder.getName())
                .level(folder.getLevel())
                .children(new ArrayList<>())
                .build();
    }
}
