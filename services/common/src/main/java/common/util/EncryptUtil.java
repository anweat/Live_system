package common.util;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * 加密工具类
 * 
 * 功能：
 * - MD5 哈希
 * - Base64 编码/解码
 * - 简单的密码加密（不建议用于生产环境）
 */
public class EncryptUtil {

    /**
     * MD5 加密
     * 
     * @param text 原文本
     * @return MD5 加密后的字符串
     */
    public static String md5(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * MD5 加密（带盐值）
     * 
     * @param text 原文本
     * @param salt 盐值
     * @return MD5 加密后的字符串
     */
    public static String md5WithSalt(String text, String salt) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (salt == null || salt.isEmpty()) {
            return md5(text);
        }
        return md5(salt + text + salt);
    }

    /**
     * Base64 编码
     * 
     * @param text 原文本
     * @return Base64 编码后的字符串
     */
    public static String base64Encode(String text) {
        if (text == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    /**
     * Base64 解码
     * 
     * @param encodedText Base64 编码的字符串
     * @return 解码后的原文本
     */
    public static String base64Decode(String encodedText) {
        if (encodedText == null || encodedText.isEmpty()) {
            return null;
        }
        try {
            return new String(Base64.getDecoder().decode(encodedText));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 解码失败", e);
        }
    }

    /**
     * Base64 编码（字节数组）
     * 
     * @param bytes 字节数组
     * @return Base64 编码后的字符串
     */
    public static String base64Encode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Base64 解码（返回字节数组）
     * 
     * @param encodedText Base64 编码的字符串
     * @return 解码后的字节数组
     */
    public static byte[] base64DecodeToBytes(String encodedText) {
        if (encodedText == null || encodedText.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(encodedText);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 解码失败", e);
        }
    }

    /**
     * 简单的密码加密（MD5 + 盐值）
     * 
     * @param password 密码
     * @param salt     盐值（通常是用户 ID 或用户名）
     * @return 加密后的密码
     */
    public static String encryptPassword(String password, String salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("盐值不能为空");
        }
        // 多次加密增加安全性
        String encrypted = md5WithSalt(password, salt);
        encrypted = md5(encrypted + salt);
        return encrypted;
    }

    /**
     * 验证密码
     * 
     * @param password          用户输入的密码
     * @param salt              盐值
     * @param encryptedPassword 存储的加密密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String salt, String encryptedPassword) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        String encrypted = encryptPassword(password, salt);
        return encrypted.equals(encryptedPassword);
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) {
                hex.append('0');
            }
            hex.append(h);
        }
        return hex.toString();
    }

    /**
     * 简单的字符串加密（异或）
     * 不推荐用于敏感数据，仅用于简单的混淆
     * 
     * @param text 原文本
     * @param key  密钥
     * @return 加密后的字符串（十六进制）
     */
    public static String xorEncrypt(String text, String key) {
        if (text == null || text.isEmpty() || key == null || key.isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char k = key.charAt(i % key.length());
            result.append(Integer.toHexString(c ^ k));
        }
        return result.toString();
    }

    /**
     * 简单的字符串解密（异或）
     * 
     * @param encryptedText 加密后的字符串（十六进制）
     * @param key           密钥
     * @return 解密后的原文本
     */
    public static String xorDecrypt(String encryptedText, String key) {
        if (encryptedText == null || encryptedText.isEmpty() || key == null || key.isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < encryptedText.length(); i += 2) {
            String hex = encryptedText.substring(i, Math.min(i + 2, encryptedText.length()));
            char c = (char) Integer.parseInt(hex, 16);
            char k = key.charAt((i / 2) % key.length());
            result.append((char) (c ^ k));
        }
        return result.toString();
    }

    /**
     * SHA-256 加密
     * 
     * @param text 原文本
     * @return SHA-256 加密后的字符串
     */
    public static String sha256(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 加密失败", e);
        }
    }

    /**
     * SHA-512 加密
     * 
     * @param text 原文本
     * @return SHA-512 加密后的字符串
     */
    public static String sha512(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-512 加密失败", e);
        }
    }

    /**
     * 对敏感信息脱敏（仅保留首尾字符）
     * 
     * @param data 敏感数据
     * @return 脱敏后的数据
     */
    public static String maskData(String data) {
        if (data == null || data.length() <= 2) {
            return "***";
        }
        char[] chars = data.toCharArray();
        for (int i = 1; i < chars.length - 1; i++) {
            chars[i] = '*';
        }
        return String.valueOf(chars);
    }

    /**
     * 对手机号脱敏
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号（如：138****0000）
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 对身份证号脱敏
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号（如：110101199003076***）
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, idCard.length() - 3) + "***";
    }

    /**
     * 对邮箱脱敏
     * 
     * @param email 邮箱
     * @return 脱敏后的邮箱（如：u****@gmail.com）
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        String username = email.substring(0, 1) + "****";
        String domain = email.substring(atIndex);
        return username + domain;
    }
}
