package com.example.errorBook.util;

public class RegExpUtil {
    /**
     * 正则匹配手机号码
     *
     * @param tel
     * @return
     */
    public static boolean RegTel(String tel) {
        //匹配所有号段的手机号码
        String regex = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$";
        
        if (tel.matches(regex)) {
            return true;
        }
        return false;
    }
    
}
