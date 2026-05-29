package online.faramita.bbs.module.folder.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.dto.PageQuery;
import online.faramita.bbs.common.enums.ResultCode;
import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.common.result.Result;
import online.faramita.bbs.module.blog.vo.BlogPrivateBriefVO;
import online.faramita.bbs.module.folder.dto.FolderBlogsMoveDTO;
import online.faramita.bbs.module.folder.dto.FolderMoveDTO;
import online.faramita.bbs.module.folder.dto.FolderRenameDTO;
import online.faramita.bbs.module.folder.dto.FolderSaveDTO;
import online.faramita.bbs.module.folder.service.FolderService;
import online.faramita.bbs.module.folder.vo.FolderTree;
import online.faramita.bbs.security.util.SecurityUtil;

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
    @PostMapping
    public Result<Void> saveFolder(@RequestBody FolderSaveDTO folderSaveDTO) {

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
    @PutMapping("/{id}")
    public Result<Void> renameFolder(
        @PathVariable Long id,
        @RequestBody FolderRenameDTO folderRenameDTO
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
    @PutMapping("/{id}/move")
    public Result<Void> moveFolder(
        @PathVariable Long id,
        @RequestBody FolderMoveDTO folderMoveDTO
    ) {

        folderService.moveFolderById(id, folderMoveDTO.getTargetParentId());
        
        return Result.success();
    }

    /**
     * 删除目录
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFolder(@PathVariable Long id) {

        folderService.deleteFolder(id);

        return Result.success();
    }

    /**
     * 获得当前用户的 FolderTree
     * @return
     */
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
    @PutMapping("/blogs/move")
    public Result<Void> moveBlogsToFolder(@RequestBody FolderBlogsMoveDTO folderBlogsMoveDTO) {

        folderService.moveBlogsToFolder(folderBlogsMoveDTO);
        
        return Result.success();

    }

    
}
