package online.faramita.bbs.service;

import org.springframework.web.multipart.MultipartFile;

import online.faramita.bbs.dto.LoginDTO;
import online.faramita.bbs.dto.ProfileDTO;
import online.faramita.bbs.dto.RegisterDTO;
import online.faramita.bbs.entity.User;
import online.faramita.bbs.vo.LoginVO;
import online.faramita.bbs.vo.ProfileVO;

public interface UserService {

    /**
     * 账号登录
     * @param loginDTO
     * @return
     */
    User login(LoginDTO loginDTO);

    /**
     * 账号注册
     * @param registerDTO
     */
    void register(RegisterDTO registerDTO);

    /**
     * 根据uid查询个人资料
     * @param uid
     * @return
     */
    ProfileVO getProfileByUid(Long uid);

    /**
     * 更新个人资料
     * @param profileDTO
     */
    void updateProfile(ProfileDTO profileDTO);

    /**
     * 上传文件并更新数据库
     * @param uid
     * @param file
     * @return
     */
    String updateAvatar(Long uid, MultipartFile file);

    /**
     * 根据id查询用户详情
     * @param id
     * @return
     */
    LoginVO getCurrentUserInfo(Long id);

    

}
