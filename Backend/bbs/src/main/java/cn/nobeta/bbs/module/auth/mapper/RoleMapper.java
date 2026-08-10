package cn.nobeta.bbs.module.auth.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper {

    /**
     * 根据用户 id 查询持有的角色列表
     * @param id
     * @return
     */
    // List<String> selectRoleCodesByUserId(Long id);

}
