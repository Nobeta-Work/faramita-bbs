package online.faramita.bbs.module.auth.service;

import online.faramita.bbs.module.auth.dto.LoginDTO;
import online.faramita.bbs.module.auth.dto.RegisterDTO;
import online.faramita.bbs.module.auth.vo.TokenVO;

public interface AuthService {

    TokenVO login(LoginDTO loginUser);

    void register(RegisterDTO registerDTO);

    TokenVO refresh(String refreshToken);

    void logout(Long userId, String accessToken);

}
