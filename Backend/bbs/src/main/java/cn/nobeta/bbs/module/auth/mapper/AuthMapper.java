package cn.nobeta.bbs.module.auth.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.nobeta.bbs.module.user.entity.User;

@Mapper
public interface AuthMapper {


    /**
     * 根据用户名查询用户认证信息
     * @param username
     * @return
     */
    User selectAuthUserByUsername(@Param("username") String username);

    /**
     * 根据用户 id 查询角色码
     * @param userId
     * @return
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 id 查询拥有的权限码
     * @param userId
     * @return
     */
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    
    /**
     * 为用户关联默认用户角色
     * @param userId
     * @return
     */
    int insertUserDefaultRole(@Param("userId") Long userId);

    /**
     * 创建用户信息
     * @param user
     */
    void insertUser(User user);

}
