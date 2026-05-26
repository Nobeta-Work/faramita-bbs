package online.faramita.bbs.module.auth.mapper;

import org.apache.ibatis.annotations.Mapper;

import online.faramita.bbs.module.auth.dto.RegisterDTO;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;

@Mapper
public interface AuthMapper {

    /**
     * 根据 username 多表联查，查询用户认证信息
     * sys_user, sys_role, sys_perm, sys_user_role, sys_role_perm
     * @param username
     * @return {user, roles, permissions}
     */
    UserAuthInfo getUserAuthInfoByUsername(String username);

    /**
     * 根据注册信息创建用户并赋予用户权限
     * sys_user, sys_user_role
     * @param registerDTO
     */
    void registerUser(RegisterDTO registerDTO);

}
