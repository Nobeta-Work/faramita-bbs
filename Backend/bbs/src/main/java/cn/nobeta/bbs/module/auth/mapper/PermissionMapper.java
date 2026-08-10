package cn.nobeta.bbs.module.auth.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper {

    /**
     * 根据用户 id 查询其拥有的权限码
     * @param id
     * @return
     */
    // List<String> selectPermCodesByUserId(Long id);

}
