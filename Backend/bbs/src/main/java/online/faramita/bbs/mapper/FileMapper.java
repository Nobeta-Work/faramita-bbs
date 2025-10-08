package online.faramita.bbs.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import online.faramita.bbs.entity.AvatarInfo;

@Mapper
public interface FileMapper {

    /**
     * 向头像信息表插入数据
     * @param fileInfo
     */
    void insertAvatar(AvatarInfo fileInfo);

    /**
     * 根据fileUuid查询
     * @param fileUuid
     * @return
     */
    @Select("select * from avatar_info where file_uuid = #{fileUuid}")
    AvatarInfo selectByFileUuid(String fileUuid);

    /**
     * 更新avatar
     * @param avatorInfo
     */
    void updateAvator(AvatarInfo avatorInfo);

    /**
     * 查询所有已过期且未关联的头像
     * @param currentTime 当前时间
     * @return 过期且未关联头像列表
     */
    @Select("select * from avatar_info where is_referenced = 0 and expire_time < #{currentTime}")
    List<AvatarInfo> selectExpiredUnreferencedAvatars(LocalDateTime currentTime);

    /**
     * 批量删除头像数据
     * @param ids
     */
    void batchDeleteByIds(List<Long> ids);
}
