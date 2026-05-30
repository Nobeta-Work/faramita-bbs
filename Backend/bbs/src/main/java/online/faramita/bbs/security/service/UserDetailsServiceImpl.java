package online.faramita.bbs.security.service;


import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.constant.MessageConstant;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;
import online.faramita.bbs.module.auth.mapper.AuthMapper;
import online.faramita.bbs.module.user.entity.User;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{

    
    private final AuthMapper authMapper;
    

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 多单表查询 + Java 内存组装
       
        // 1. 查询 User 字段
        User user = authMapper.selectAuthUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(MessageConstant.USER_NOT_EXISTS);
        }

        Long userId = user.getId();
        
        // 2. 查询 RoleCode 字段
        List<String> roles = authMapper.selectRoleCodesByUserId(userId);

        // 3. 查询 PermCode 字段
        List<String> permissions = authMapper.selectPermCodesByUserId(userId);

        // 4. 组装
        UserAuthInfo userAuthInfo = new UserAuthInfo(user, permissions, roles);
        
        return userAuthInfo;
    }

}
