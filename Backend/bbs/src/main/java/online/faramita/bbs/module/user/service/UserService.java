package online.faramita.bbs.module.user.service;

import org.springframework.web.multipart.MultipartFile;

import online.faramita.bbs.module.user.dto.PasswordEditDTO;
import online.faramita.bbs.module.user.dto.UserProfileDTO;
import online.faramita.bbs.module.user.vo.AvatarVO;
import online.faramita.bbs.module.user.vo.UserInfoVO;
import online.faramita.bbs.module.user.vo.UserProfileVO;

public interface UserService {



    UserProfileVO queryUserProfileById(Long userId);

    UserInfoVO queryUserInfoById(Long id);

    void editUserProfile(Long userId, UserProfileDTO userProfileDTO);

    void editUserPassword(Long userId, PasswordEditDTO passwordEditDTO);

    AvatarVO editUserAvatar(Long userId, MultipartFile file);

    

}
