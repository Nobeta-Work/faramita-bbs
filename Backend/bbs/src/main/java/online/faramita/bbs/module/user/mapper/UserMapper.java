package online.faramita.bbs.module.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import online.faramita.bbs.module.auth.dto.RegisterDTO;
import online.faramita.bbs.module.user.entity.User;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询账号
     * @param username
     * @return
     */
    User getByUsername(String username);

    /**
     * 根据用户 id 更新用户
     * @param user
     */
    void updateById(User user);

    /**
     * 更新指定用户 id 的密码
     * @param userId
     * @param password
     */
    void updatePassword(Long userId, String password);

    /**
     * 创建用户实体
     * @param registerDTO
     */
    void register(RegisterDTO registerDTO);


}
