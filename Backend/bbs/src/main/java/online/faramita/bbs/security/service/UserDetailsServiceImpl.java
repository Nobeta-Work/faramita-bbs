package online.faramita.bbs.security.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.constant.MessageConstant;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;
import online.faramita.bbs.module.auth.mapper.AuthMapper;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{

    
    private final AuthMapper authMapper;
    

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       
        UserAuthInfo userAuthInfo = authMapper.getUserAuthInfoByUsername(username);
        if (userAuthInfo == null) {
            throw new UsernameNotFoundException(MessageConstant.USER_NOT_EXISTS);
        }
        
        return userAuthInfo;
    }

}
