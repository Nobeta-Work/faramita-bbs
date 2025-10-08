package online.faramita.bbs.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 */
public class PasswordEncoderUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成随机盐值
     */
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }


    /**
     * 加密密码
     * @param password
     * @return
     */
    public static String encode(String password) {
        byte[] salt = generateSalt();
        byte[] hashedPassword = hash(password, salt);

        // 将盐值和哈希值组合后进行Base64编码
        byte[] combined = new byte[salt.length + hashedPassword.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 密码校验
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            // 解码存储的密码
            byte[] combined = Base64.getDecoder().decode(encodedPassword);

            // 提取盐值
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // 提取存储的哈希值
            byte[] storedHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, storedHash, 0, storedHash.length);

            // 计算原始密码哈希值
            byte[] computedHash = hash(rawPassword, salt);

            // 比较哈希值
            return MessageDigest.isEqual(storedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 哈希计算
     * @param password
     * @param salt
     * @return
     */
    private static byte[] hash(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("哈希算法不可用", e);
        }
    }
}
