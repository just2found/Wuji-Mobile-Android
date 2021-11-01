package net.sdvn.nascommon.utils;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Administrator on 2017/12/7.
 */

public class PhoneNumberUtils {

    public static boolean isChinaPhoneLegal(@NonNull String str) throws PatternSyntaxException {
        String regE = "^1[3-5|7-8][0-9]{9}$";
        Pattern p = Pattern.compile(regE);
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
