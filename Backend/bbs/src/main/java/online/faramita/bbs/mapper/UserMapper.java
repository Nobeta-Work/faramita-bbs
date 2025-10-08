package online.faramita.bbs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import online.faramita.bbs.entity.User;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询账号
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    /**
     * 创建用户
     * @param user
     * @return 返回uuid
     */
    void insert(User user);

    /**
     * 更新用户数据
     * @param user
     */
    void update(User user);

    /**
     * 根据id查询用户
     * @param uid
     * @return
     */
    @Select("select * from user where id = ${id}")
    User getById(Long id);

    /**
     * 根据nickname查询用户
     * @param nickname
     * @return
     */
    @Select("select * from user where nickname = #{nickname}")
    User getByNickname(String nickname);

}
