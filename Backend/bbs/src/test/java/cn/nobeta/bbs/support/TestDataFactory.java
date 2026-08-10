package cn.nobeta.bbs.support;

import java.util.List;

import cn.nobeta.bbs.module.auth.dto.LoginDTO;
import cn.nobeta.bbs.module.auth.dto.RegisterDTO;
import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;
import cn.nobeta.bbs.module.user.entity.User;

public class TestDataFactory {

    public static User user() {
        return User.builder()
                .id(1L)
                .username("alice")
                .password("encoded-pwd")
                .status(1)
                .build();
    }

    public static UserAuthInfo loginUser() {
        return new UserAuthInfo(
            user(),
            List.of("blog:write"),
            List.of("USER")
        );
    }

    public static LoginDTO loginDTO() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("raw-pwd");
        return dto;
    }

    public static RegisterDTO registerDTO() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("alice");
        dto.setPassword("123456");
        dto.setNickname("Alice");
        dto.setSex(0);
        dto.setRace("human");
        return dto;
    }
}
