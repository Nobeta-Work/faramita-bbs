package online.faramita.bbs.service.impl;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.constant.MessageConstant;
import online.faramita.bbs.dto.BlogQueryDTO;
import online.faramita.bbs.dto.LoginDTO;
import online.faramita.bbs.dto.ProfileDTO;
import online.faramita.bbs.dto.RegisterDTO;
import online.faramita.bbs.entity.AvatarInfo;
import online.faramita.bbs.entity.Blog;
import online.faramita.bbs.entity.User;
import online.faramita.bbs.exception.AccountException;
import online.faramita.bbs.exception.ResourceNotFoundException;
import online.faramita.bbs.mapper.BlogMapper;
import online.faramita.bbs.mapper.FileMapper;
import online.faramita.bbs.mapper.UserMapper;
import online.faramita.bbs.service.FileService;
import online.faramita.bbs.service.UserService;
import online.faramita.bbs.util.PasswordEncoderUtil;
import online.faramita.bbs.vo.LoginVO;
import online.faramita.bbs.vo.ProfileVO;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired  
    private BlogMapper blogMapper;
    @Autowired
    private FileService fileService;

    /**
     * 账号登录
     * @param loginDTO
     * @return
     */
    public User login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1.根据用户名查询数据库中的数据
        User user = userMapper.getByUsername(username);

        // 2.异常处理
        // 2.1 账号不存在
        if (user == null) {
            throw new AccountException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 2.2 账号被锁定
        if (user.getIsLocked() != null && user.getIsLocked() == 1) {
            // 检查锁定时间是否过期
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(30).isAfter(LocalDateTime.now())) {
                throw new AccountException(MessageConstant.ACCOUNT_LOCKED);
            } else {
                // 锁定时间过期，重置锁定状态
                user.setIsLocked(0);
                user.setLoginFailCount(0);
                user.setLockTime(null);
                userMapper.update(user);
            }
        }

        // // 3.md5加密密码
        // password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 4.密码校验
        // 使用自定义工具类严重密码
        if (!PasswordEncoderUtil.matches(password, user.getPassword())) {
            // 4.1密码错误
            log.info("密码错误");
            // 4.2记录登录失败次数
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(failCount);
            // 4.3失败次数达到5次，锁定账户
            if (failCount >= 5) {
                user.setIsLocked(1);
                user.setLockTime(LocalDateTime.now());
                userMapper.update(user);
                throw new AccountException(MessageConstant.ACCOUNT_LOCKED);
            }

            // 4.4失败还没达到5次，记录失败数
            userMapper.update(user);

            throw new AccountException(MessageConstant.PASSWORD_ERROR);
        }

        // 5.登录成功，重置失败次数
        user.setLoginFailCount(0);
        userMapper.update(user);

        // 6.返回User
        return user;
    }

    /**
     * 账号注册
     * @param registerDTO
     */
    @Transactional
    public void register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();

        // 1.根据账号查询是否已存在账户
        User user = userMapper.getByUsername(username);
        if (user != null) {
            throw new AccountException(MessageConstant.ACCOUNT_EXIST);
        }
        // 2.根据昵称查询是否已存在账户
        user = userMapper.getByNickname(registerDTO.getNickname());
        if (user != null) {
            throw new AccountException(MessageConstant.NICKNAME_EXIST);
        }
        // // 2.md5加密密码存储
        // String password = registerDTO.getPassword();
        // password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 2.使用自定义工具类盐值加密
        String password = PasswordEncoderUtil.encode(registerDTO.getPassword());
        registerDTO.setPassword(password);
        // 3.User对象存入数据库
        user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("注册失败{}", e);
            throw e;
        }
        // 4.关联头像和用户
        AvatarInfo avatorInfo = AvatarInfo.builder()
                        .fileUuid(user.getAvatar())
                        .isReferenced(1)
                        .uid(user.getId())
                        .build();
        fileMapper.updateAvator(avatorInfo);
    }

    /**
     * 根据uid查询个人资料
     * @param uid
     * @return
     */
    public ProfileVO getProfileByUid(Long uid) {
        // 1.用户校验
        User user = validateUser(uid);
        // 1.1消除密码
        user.setPassword(null);
        // 2.根据uid查询获取至多3个Blog的数组
        // 2.1 封装BlogQueryDTO
        BlogQueryDTO blogQueryDTO = new BlogQueryDTO();
        blogQueryDTO.setAuthorId(uid);
        // 2.2 启动分页查询 - 查询第一页3条数据
        PageHelper.startPage(1, 3);
        Page<Blog> pageResult = blogMapper.findBlogs(blogQueryDTO);
        List<Blog> blogList = pageResult.getResult();
        

        // 3.封装结果
        return ProfileVO.builder()
                .user(user)
                .blogList(blogList)
                .build();
    }

    /**
     * 上传文件并更新数据库
     * @param uid
     * @param file
     * @return
     */
    @Transactional
    public String updateAvatar(Long uid, MultipartFile file) {
        // 用户校验
        validateUser(uid);
        User user = new User();
        // 文件上传磁盘，存入avatar_info数据库，无关联
        String fileUuid = fileService.uploadAvatar(file);
        // 更新关联
        AvatarInfo avatar = AvatarInfo.builder()
                        .fileUuid(fileUuid)
                        .uid(uid)
                        .isReferenced(1)
                        .build();
        fileMapper.updateAvator(avatar);
        // 更新数据库
        user.setId(uid);
        user.setAvatar(fileUuid);

        userMapper.update(user);

        return fileUuid;
    }

    /**
     * 更新个人资料
     * @param profileDTO
     */
    @Transactional
    public void updateProfile(ProfileDTO profileDTO) {
        // 用户存在校验
        User user = validateUser(profileDTO.getId());
        BeanUtils.copyProperties(profileDTO, user);
        // 密码修改处理
        if (profileDTO.getPassword() != null) {
            if (PasswordEncoderUtil.matches(profileDTO.getPassword(), user.getPassword())) {
                throw new AccountException(MessageConstant.PASSWORD_EXIST);
            }
            String password = PasswordEncoderUtil.encode(user.getPassword());
            user.setPassword(password);
        }
        
        // 数据库更新
        userMapper.update(user);
    }

    /**
     * 根据id查询用户详情
     * @param id
     * @return
     */
    public LoginVO getCurrentUserInfo(Long id) {
        // 1.根据id查询用户
        User user = userMapper.getById(id);
        // 2.若用户不存在，抛出异常
        if (user == null) {
            throw new ResourceNotFoundException(MessageConstant.USER_NOT_EXISTS);
        }
        // 3.用户存在，封装LoginVO
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);

        return loginVO;
    }
    
    // ? 功能方法
    // 验证查询用户存在与否
    private User validateUser(Long uid) {
        User user = userMapper.getById(uid);
        // 判断是否存在
        if (user == null) {
            throw new ResourceNotFoundException(MessageConstant.USER_NOT_EXISTS);
        }

        return user;
    }
}
