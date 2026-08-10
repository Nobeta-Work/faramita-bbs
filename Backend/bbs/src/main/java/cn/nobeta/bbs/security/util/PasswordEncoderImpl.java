package cn.nobeta.bbs.security.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;


public class PasswordEncoderImpl implements PasswordEncoder {

    /** BCRYPT 加密强度 */
    private static final int BCRYPT_ROUNDS = 10;



    @Override
    public String encode(CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        
        if (encodedPassword.startsWith("$2")) {
            return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        } else {
            return oldMatches(rawPassword.toString(), encodedPassword);
        }
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return !encodedPassword.startsWith("$2");
    }


    // =============== 旧密码加密 ==============

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * 密码校验
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    public static boolean oldMatches(String rawPassword, String encodedPassword) {
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
            byte[] computedHash = oldHash(rawPassword, salt);

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
        private static byte[] oldHash(String password, byte[] salt) {
            try {
                MessageDigest md = MessageDigest.getInstance(ALGORITHM);
                md.update(salt);
                return md.digest(password.getBytes());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("哈希算法不可用", e);
            }
        }
}
