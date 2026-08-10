package cn.nobeta.bbs.module.folder.service;

import cn.nobeta.bbs.common.dto.PageQuery;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.module.blog.vo.BlogPrivateBriefVO;
import cn.nobeta.bbs.module.folder.dto.FolderBlogsMoveDTO;
import cn.nobeta.bbs.module.folder.dto.FolderSaveDTO;
import cn.nobeta.bbs.module.folder.vo.FolderTree;

public interface FolderService {

    void createFolder(Long userId, FolderSaveDTO folderSaveDTO);

    void renameFolder(Long id, String name);

    void moveFolderById(Long id, Long targetParentId);

    void deleteFolder(Long id);

    FolderTree queryFolderTreeByUserId(Long userId);

    PageResult<BlogPrivateBriefVO> queryBlogPageByFolderId(Long id, PageQuery pageQuery);

    void moveBlogsToFolder(FolderBlogsMoveDTO folderBlogsMoveDTO);

}
