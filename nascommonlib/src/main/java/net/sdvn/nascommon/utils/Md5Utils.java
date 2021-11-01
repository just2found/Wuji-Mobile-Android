package net.sdvn.nascommon.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
    private final static char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    //以十六进制字符串形式返回
    public static String encode(String in) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(in.getBytes());
            return hexString(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return "";
        }
    }

    @NotNull
    public static String hexString(byte[] result) {
        //将结果转换成十六进制字符串
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

    //直接返回byte[]
    public static byte[] encodebytes(String in) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] result = md.digest(in.getBytes());
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return null;
        }
    }
    public static String md5(String stringToEncode) {
        return digest(stringToEncode, "MD5", "UTF-8");
    }

    /**
     * Generates the SHA-1 digest (40 hex characters) for a given String based on UTF-8.
     * The SHA-1 algorithm produces less collisions than MD5.
     *
     * @return SHA-1 digest .
     */
    public static String sha1(String stringToEncode) {
        return digest(stringToEncode, "SHA-1", "UTF-8");
    }

    /**
     * Generates a digest (hex string) for the given string
     */
    public static String digest(String string, String digestAlgo, String encoding) {
        try {
            MessageDigest digester = MessageDigest.getInstance(digestAlgo);
            digester.update(string.getBytes(encoding));
            return hexString(digester.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    public static String hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[value >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[value & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @throws IllegalArgumentException if the given string is invalid hex
     */
    public static byte[] parseHex(String hex) {
        int length = hex.length();
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Illegal string length: " + length);
        }
        int bytesLength = length / 2;
        byte[] bytes = new byte[bytesLength];
        int idxChar = 0;
        for (int i = 0; i < bytesLength; i++) {
            int value = parseHexDigit(hex.charAt(idxChar++)) << 4;
            value |= parseHexDigit(hex.charAt(idxChar++));
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    /**
     * @throws IllegalArgumentException if the given char is invalid hex
     */
    public static int parseHexDigit(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('A' <= c && c <= 'F') {
            return c - 'A' + 10;
        } else if ('a' <= c && c <= 'f') {
            return c - 'a' + 10;
        }
        throw new IllegalArgumentException("Illegal hex digit: " + c);
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
