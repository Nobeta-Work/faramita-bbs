package online.faramita.bbs.module.folder.service;

import online.faramita.bbs.common.dto.PageQuery;
import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.module.blog.vo.BlogPrivateBriefVO;
import online.faramita.bbs.module.folder.dto.FolderBlogsMoveDTO;
import online.faramita.bbs.module.folder.dto.FolderSaveDTO;
import online.faramita.bbs.module.folder.vo.FolderTree;

public interface FolderService {

    void createFolder(Long userId, FolderSaveDTO folderSaveDTO);

    void renameFolder(Long id, String name);

    void moveFolderById(Long id, Long targetParentId);

    void deleteFolder(Long id);

    FolderTree queryFolderTreeByUserId(Long userId);

    PageResult<BlogPrivateBriefVO> queryBlogPageByFolderId(Long id, PageQuery pageQuery);

    void moveBlogsToFolder(FolderBlogsMoveDTO folderBlogsMoveDTO);

}
