package com.errorbook.errorbookv1.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PasswordUtil {
    // 固定随机盐
    public static final String SECRET="z3g8r3h4bS8DFG0R";
    /**
     * md5+salt 随机盐加密
     */
    public static String getMD5Encryption(String password, String salt) {
        //密码md5+随机盐加密
        return DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }
    /**
     * md5+SECRET 固定盐加密
     */
    public static String getMD5Encryption(String password){
        return DigestUtils.md5DigestAsHex((password+SECRET).getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 生成随机盐
     */
    public static String getSalt() {
        String str = "zxcvbnmasdfghjklqwertyuiopZXCVBNMASDFGHJKLQWERTYUIOP1234567890,.<>:?";
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        //循环16次，共取出16个随机字符
        for (int i = 0; i < 16; i++) {
            //每次生成一个67以内的随机数
            int number = random.nextInt(68);
            //生成的随机数作为 str 字符串的下标；从 str 中取出随机字符后追加到 stringBuffer
            stringBuffer.append(str.charAt(number));
        }
        return stringBuffer.toString();
    }
    
}
