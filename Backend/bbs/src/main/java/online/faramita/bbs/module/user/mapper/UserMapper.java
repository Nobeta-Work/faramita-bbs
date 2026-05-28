package online.faramita.bbs.module.user.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;

import online.faramita.bbs.module.blog.vo.AuthorBriefVO;
import online.faramita.bbs.module.user.dto.UserProfileDTO;
import online.faramita.bbs.module.user.entity.User;

@Mapper
public interface UserMapper {

    /**
     * 根据 id 查询用户
     * @param userId
     * @return
     */
    User selectUserById(Long userId);

    /**
     * 更新指定用户的密码
     * @param userId
     * @param password
     */
    void updatePassword(Long userId, String password);

    /**
     * 根据 id 修改用户个人资料
     * @param userId
     * @param profileDTO
     */
    void updateUserProfileById(Long userId, UserProfileDTO profileDTO);

    /**
     * 更新指定用户的头像字段
     * @param userId
     * @param avatarKey
     */
    void updateUserAvatar(Long userId, String avatarKey);

    /**
     * 根据 id 批量查询用户简要信息
     * @param userIds
     * @return
     */
    Collection<AuthorBriefVO> selectAuthorBriefByIds(Set<Long> userIds);

    /**
     * 根据 id 查询用户简要信息
     * @param authorId
     * @return
     */
    AuthorBriefVO selectAuthorBriefById(Long authorId);

    

    


}
