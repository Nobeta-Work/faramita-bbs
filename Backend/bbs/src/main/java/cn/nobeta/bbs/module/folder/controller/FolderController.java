package cn.nobeta.bbs.module.folder.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.dto.PageQuery;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.blog.vo.BlogPrivateBriefVO;
import cn.nobeta.bbs.module.folder.dto.FolderBlogsMoveDTO;
import cn.nobeta.bbs.module.folder.dto.FolderMoveDTO;
import cn.nobeta.bbs.module.folder.dto.FolderRenameDTO;
import cn.nobeta.bbs.module.folder.dto.FolderSaveDTO;
import cn.nobeta.bbs.module.folder.service.FolderService;
import cn.nobeta.bbs.module.folder.vo.FolderTree;
import cn.nobeta.bbs.security.util.SecurityUtil;

@RequestMapping("/api/folders")
@RestController
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    /**
     * 保存目录
     * @param folderSaveDTO
     * @return
     */
    @AuditLog(message = "保存目录", data = "{'parentId': #p0.parentId, 'folderName': #p0.name}")
    @PostMapping
    public Result<Void> saveFolder(@Valid @RequestBody FolderSaveDTO folderSaveDTO) {

        Long userId = SecurityUtil.getLoginUserId();
        if (userId == null) return Result.fail(ResultCode.UNAUTHORIZED);

        folderService.createFolder(userId, folderSaveDTO);

        return Result.success();
    }

    /**
     * 重命名目录
     * @param id
     * @param name
     * @return
     */
    @AuditLog(message = "重命名目录", data = "{'folderId': #p0, 'folderName': #p1.name}")
    @PutMapping("/{id}")
    public Result<Void> renameFolder(
        @PathVariable Long id,
        @Valid @RequestBody FolderRenameDTO folderRenameDTO
    ) {

        folderService.renameFolder(id, folderRenameDTO.getName());

        return Result.success();
    }

    /**
     * 移动目录
     * @param id
     * @param targetId
     * @return
     */
    @AuditLog(message = "移动目录", data = "{'folderId': #p0, 'targetParentId': #p1.targetParentId}")
    @PutMapping("/{id}/move")
    public Result<Void> moveFolder(
        @PathVariable Long id,
        @Valid @RequestBody FolderMoveDTO folderMoveDTO
    ) {

        folderService.moveFolderById(id, folderMoveDTO.getTargetParentId());
        
        return Result.success();
    }

    /**
     * 删除目录
     * @param id
     * @return
     */
    @AuditLog(message = "删除目录", data = "{'folderId': #p0}")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFolder(@PathVariable Long id) {

        folderService.deleteFolder(id);

        return Result.success();
    }

    /**
     * 获得当前用户的 FolderTree
     * @return
     */
    @AuditLog(message = "获取个人目录树")
    @GetMapping("/me")
    public Result<FolderTree> getCurrentUserFolderTree() {

        Long id = SecurityUtil.getLoginUserId();
        if (id == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        FolderTree root = folderService.queryFolderTreeByUserId(id);

        return Result.success(root);

    }

    /**
     * 查询指定 id 目录下的所有博客
     * @param id
     * @return
     */
    @AuditLog(message = "查询目录下博客列表", data = "{'folderId': #p0, 'pageNum': #p1.pageNum, 'pageSize': #p1.pageSize}")
    @GetMapping("/{id}/blogs")
    public Result<PageResult<BlogPrivateBriefVO>> getBlogPageInFolder(
        @PathVariable Long id,
        @ModelAttribute PageQuery pageQuery
    ) {

        PageResult<BlogPrivateBriefVO> vo = folderService.queryBlogPageByFolderId(id, pageQuery);

        return Result.success(vo);
    }

    /**
     * 批量移动博客切换目录
     * @param folderBlogsMoveDTO
     * @return
     */
    @AuditLog(message = "批量移动博客到目录", data = "{'targetId': #p0.targetId, 'blogIds': #p0.blogIds}")
    @PutMapping("/blogs/move")
    public Result<Void> moveBlogsToFolder(@Valid @RequestBody FolderBlogsMoveDTO folderBlogsMoveDTO) {

        folderService.moveBlogsToFolder(folderBlogsMoveDTO);
        
        return Result.success();

    }

    
}
