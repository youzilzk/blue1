package com.youzi.blue.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    /**
     * 正则化验证手机号码
     */
    public static boolean isTelPhoneNumber(String mobile) {
        if (mobile != null && mobile.length() == 11) {
            Pattern pattern = Pattern.compile("^1[3|4|5|6|7|8|9][0-9]\\d{8}$");
            Matcher matcher = pattern.matcher(mobile);
            return matcher.matches();
        } else {
            return false;
        }
    }

    /**
     * 正则化验证验证码
     */
    public static boolean isToken(String token) {
        if (token != null && token.length() == 6) {
            Pattern pattern = Pattern.compile("^\\d{6}$");
            Matcher matcher = pattern.matcher(token);
            return matcher.matches();
        } else {
            return false;
        }
    }
}
