package net.sdvn.common.internet.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

/**
 *  
 * <p>
 * Created by admin on 2020/7/30,19:29
 */
public class Utils {
    public static String getLanguage(Context ctx) {
        Locale curLocale = ctx.getResources().getConfiguration().locale;
        String language = curLocale.getLanguage();
        String country = curLocale.getCountry();//"CN""TW"
        String script = curLocale.getScript();
        Timber.d("language : " + language + " country : " + country + " script : " + script);
        if ("zh".equals(language)) {
            if ("cn".equals(country.toLowerCase()) && !"hant".equals(script.toLowerCase())) {
                return "zh";
            } else {
                return "tw";
            }
        }
        return language;
    }

    @NonNull
    public static String sha256(@NonNull String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] result = md.digest(source.getBytes());
            return bytes2HexStr(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return "";
        }
    }

    @NonNull
    public static String md5(@NonNull String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] result = md.digest(source.getBytes(StandardCharsets.UTF_8));
            return bytes2HexStr(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return "";
        }
    }

    @NonNull
    public static String bytes2HexStr(@NonNull byte[] result) {
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            int number = (b & 0xff);
            String str = Integer.toHexString(number);
            if (str.length() == 1) {
                sb.append("0");
            }
            sb.append(str);
        }
        return sb.toString();
    }

    @NonNull
    public static String genRandomNum(int length) {
        int maxNum = 10;
        int i;
        int count = 0;
        String str = "qwertyupasdfghjkzxcvbnmQWERTYUPASDFGHJKLZXCVBNM0123456789";
        StringBuilder builder = new StringBuilder();
        Random r = new Random();
        while (count < length) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length()) {
                builder.append(str.charAt(i));
                count++;
            }
        }
        return builder.toString();
    }
}
