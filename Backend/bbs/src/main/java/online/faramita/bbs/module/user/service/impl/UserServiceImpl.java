package online.faramita.bbs.module.user.service.impl;


import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.common.enums.ResultCode;
import online.faramita.bbs.common.exception.BusinessException;
import online.faramita.bbs.module.auth.mapper.AuthMapper;
import online.faramita.bbs.module.file.entity.AvatarInfo;
import online.faramita.bbs.module.file.mapper.FileMapper;
import online.faramita.bbs.module.file.service.FileService;
import online.faramita.bbs.module.user.dto.PasswordEditDTO;
import online.faramita.bbs.module.user.dto.UserProfileDTO;
import online.faramita.bbs.module.user.entity.User;
import online.faramita.bbs.module.user.mapper.UserMapper;
import online.faramita.bbs.module.user.service.UserService;
import online.faramita.bbs.module.user.vo.AvatarVO;
import online.faramita.bbs.module.user.vo.UserInfoVO;
import online.faramita.bbs.module.user.vo.UserProfileVO;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final FileMapper fileMapper;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    // 2026-5-26 "v0.3.0 登陆认证接口切换 auth 模块"

    /**
     * 根据 userId 查询用户个人资料
     * @param userId
     * @return
     */
    @Override
    public UserProfileVO queryUserProfileById(Long userId) {
        // 1. 根据 id 查询用户
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户为空");
        }

        // 2. 根据 id 查询 roles
        List<String> roles = authMapper.selectRoleCodesByUserId(userId);

        // 3. 实体转换
        UserProfileVO vo = UserProfileVO.builder()
                .id(userId)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .sex(user.getSex())
                .race(user.getRace())
                .signature(user.getSignature())
                .roles(roles)
                .createTime(user.getCreateTime())
                .build();
        
        // 4. 返回
        return vo;
    }

    /**
     * 根据 id 查询个人信息
     * @param id
     * @return
     */
    @Override
    public UserInfoVO queryUserInfoById(Long id) {
        // 1. 根据 id 查询用户
        User user = userMapper.selectUserById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        // 2. 实体转换
        UserInfoVO vo = UserInfoVO.builder()
                .id(id)
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .sex(user.getSex())
                .race(user.getRace())
                .signature(user.getSignature())
                .createTime(user.getCreateTime())
                .build();

        // 3. 返回
        return vo;
    }

    /**
     * 更新个人资料
     * @param userId
     * @param profileDTO
     */
    @Override
    public void editUserProfile(Long userId, UserProfileDTO profileDTO) {
        // 1. 根据 id 查询用户
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        // 2. 更新数据库
        userMapper.updateUserProfileById(userId, profileDTO);
    }

    /**
     * 更新指定用户密码
     * @param userId
     * @param passwordEditDTO
     * @return
     */
    @Override
    public void editUserPassword(Long userId, PasswordEditDTO passwordEditDTO) {
        // 1. 根据 id 查询用户
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        // 2. 校验旧密码
        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        String newPassword = passwordEditDTO.getNewPassword();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 3. 更新数据库
        userMapper.updateUserPassword(userId, passwordEncoder.encode(newPassword));
        
    }

    /**
     * 更新指定用户的头像
     * @param userId
     * @param file
     * @return
     */
    @Override
    public AvatarVO editUserAvatar(Long userId, MultipartFile file) {

        // 查询用户信息
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        

        // 文件上传磁盘，存入avatar_info数据库，无关联
        String avatarKey = fileService.uploadAvatar(file);
        // 更新关联
        AvatarInfo avatarInfo = AvatarInfo.builder()
                        .fileUuid(avatarKey)
                        .uid(userId)
                        .isReferenced(1)
                        .build();
        fileMapper.updateAvator(avatarInfo);

        // 更新数据库

        userMapper.updateUserAvatar(userId, avatarKey);

        // 返回头像信息

        AvatarVO vo = AvatarVO.builder()
                .avatarKey(avatarKey)
                .build();

        return vo;
    }

    


}
