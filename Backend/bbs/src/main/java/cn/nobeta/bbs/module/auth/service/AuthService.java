package cn.nobeta.bbs.module.auth.service;

import cn.nobeta.bbs.module.auth.dto.LoginDTO;
import cn.nobeta.bbs.module.auth.dto.RegisterDTO;
import cn.nobeta.bbs.module.auth.vo.TokenVO;

public interface AuthService {

    TokenVO login(LoginDTO loginUser);

    void register(RegisterDTO registerDTO);

    TokenVO refresh(String refreshToken);

    void logout(Long userId, String accessToken);

}
