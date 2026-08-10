package cn.nobeta.bbs.module.folder.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.nobeta.bbs.module.folder.entity.Folder;

@Mapper
public interface FolderMapper {

    /**
     * 根据 id 查询目录
     * @param folderId
     * @return
     */
    Folder selectFolderById(@Param("folderId") Long folderId);

    /**
     * 插入目录
     * 自增主键、自适应 path
     * @param folder
     * @return 
     */
    Long insertFolder(Folder folder);

    /**
     * 重命名指定 id 的目录
     * @param id
     * @param name
     */
    int updateFolderName(@Param("id") Long id, @Param("name") String name);

    /**
     * 修改指定 id 目录的路径
     * @param id
     * @param path
     */
    int updateFolderPathById(@Param("id") Long id, @Param("path") String path);

    /**
     * 查询最大递归深度
     * @param id
     * @return
     */
    Integer selectMaxLevelInSubtree(@Param("folderId") Long folderId);

    /**
     * 递归更新子树等级和路径
     * @param authorId    用户隔离
     * @param parentId    父目录 id
     * @param parentLevel 父目录新 level
     * @param parentPath  父目录新path
     */
    int updateSubtreeFolderLevelAndPath(
            @Param("authorId") Long authorId,
            @Param("parentId") Long parentId,
            @Param("parentLevel") Integer parentLevel,
            @Param("parentPath") String parentPath);

    /**
     * 查询子树所有 id
     * @param userId
     * @param path
     * @return
     */
    List<Long> selectSubtreeFolderIdsByAuthorIdAndPath(
            @Param("userId") Long userId,
            @Param("path") String path);

    /**
     * 批量删除目录
     * @param userId
     * @param ids
     */
    void batchDeleteFolderByAuthorIdAndIds(
            @Param("userId") Long userId,
            @Param("ids") List<Long> ids);

    /**
     * 查询子树所有目录
     * @param userId
     * @param parentId
     * @return
     */
    List<Folder> selectSubtreeFoldersByAuthorIdAndParentId(
            @Param("userId") Long userId,
            @Param("parentId") Long parentId);

    /**
     * 不改变子目录，只修改当前目录的父目录
     * @param id
     * @param targetParentId
     */
    void updateFolderParentIdWithoutChildren(@Param("id") Long id, @Param("parentId") Long parentId);



}
