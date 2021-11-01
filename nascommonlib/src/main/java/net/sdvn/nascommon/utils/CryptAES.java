package net.sdvn.nascommon.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptAES {
    static final String cipher_type = "AES/CBC/PKCS5Padding";
    static final String cipher_type2 = "AES/CBC/NoPadding";


    public static byte[] encode(String skey, String iv, byte[] data) {
        byte[] skey_b_arr = skey.getBytes(StandardCharsets.UTF_8);
        byte[] iv_arr = iv.getBytes(StandardCharsets.UTF_8);
        return encode( skey_b_arr, iv_arr,data);
    }

    public static byte[] encode(byte[] skey_b_arr, byte[] iv_arr, byte[] data) {
        return process(Cipher.ENCRYPT_MODE, skey_b_arr, iv_arr, data);
    }

    public static byte[] decode(String skey, String iv, byte[] data) {
        byte[] skey_b_arr = skey.getBytes(StandardCharsets.UTF_8);
        byte[] iv_arr = iv.getBytes(StandardCharsets.UTF_8);
        return decode(skey_b_arr, iv_arr, data);
    }


    public static byte[] decode(byte[] skey, byte[] iv, byte[] data) {
        return process(Cipher.DECRYPT_MODE, skey, iv, data);
    }

    private static byte[] process(int mode, byte[] skey, byte[] iv, byte[] data) {
        try {
            SecretKeySpec key = new SecretKeySpec(skey, "AES");
            Cipher cipher = Cipher.getInstance(cipher_type);
            cipher.init(mode, key, new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}