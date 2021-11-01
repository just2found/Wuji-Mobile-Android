package net.sdvn.common.internet.utils

import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object CryptAES {
    private const val debug = false
    const val cipher_type = "AES/CBC/PKCS5Padding"
    const val cipher_type2 = "AES/CBC/NoPadding"
    fun encode(skey: String, iv: String, data: ByteArray?): ByteArray? {
        val skey_b_arr = skey.toByteArray(StandardCharsets.UTF_8)
        val iv_arr = iv.toByteArray(StandardCharsets.UTF_8)
        return encode(skey_b_arr, iv_arr, data)
    }

    fun encode(skey_b_arr: ByteArray, iv_arr: ByteArray, data: ByteArray?): ByteArray? {
        return process(cipher_type, Cipher.ENCRYPT_MODE, skey_b_arr, iv_arr, data)
    }

    fun decode(skey: String, iv: String, data: ByteArray?): ByteArray? {
        val skey_b_arr = skey.toByteArray(StandardCharsets.UTF_8)
        val iv_arr = iv.toByteArray(StandardCharsets.UTF_8)
        return decode(skey_b_arr, iv_arr, data)
    }

    fun decode(skey: ByteArray, iv: ByteArray, data: ByteArray?): ByteArray? {
        return process(cipher_type, Cipher.DECRYPT_MODE, skey, iv, data)
    }

    private fun process(type: String, mode: Int, skey: ByteArray, iv: ByteArray, data: ByteArray?): ByteArray? {
        return try {
            val key = SecretKeySpec(skey, "AES")
            val cipher = Cipher.getInstance(type)
            cipher.init(mode, key, IvParameterSpec(iv))
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            data
        }
    }

    fun concat(first: ByteArray, second: ByteArray): ByteArray {
        val result = Arrays.copyOf(first, first.size + second.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }

    @Throws(Exception::class)
    fun encodeZeroPadding(key: ByteArray, iv: ByteArray, dataBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(cipher_type2)
        val blockSize = cipher.blockSize
        var length = dataBytes.size
        //计算需填充长度
        length += (blockSize - length % blockSize)
        val plaintext = ByteArray(length)
        //填充
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.size)
        val keySpec = SecretKeySpec(key, "AES")
        //设置偏移量参数
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plaintext)
    }

    fun bytesToHexString(src: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (i in src.indices) {
            val v: Int = src[i].toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    @Throws(NoSuchAlgorithmException::class)
    fun md5md5(input: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(input)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun md5Byte(input: String): ByteArray {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(input.toByteArray())
    }

    @Throws(Exception::class)
    @JvmStatic
    fun encode(keySrc: String, data: String): Pair<String, String> {
        val iv = md5Byte(keySrc)
        debugPrint("iv", iv)
        val md5md5 = md5md5(iv)
        val key = concat(iv, md5md5)
        debugPrint("key", key)
        val encode = encodeZeroPadding(key, iv, data.toByteArray(Charsets.UTF_8))
        val result = bytesToHexString(encode)
        val signBytes = hmacMd5(result.toByteArray(charset("UTF-8")), iv)
        val sign = bytesToHexString(signBytes)
        return Pair(result, sign)
    }

    private fun debugPrint(s: String, iv: ByteArray) {
        if (debug) {
            Timber.d("$s : ${bytesToHexString(iv)}")
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    fun md5HexString(password: String): String {
        return bytesToHexString(md5Byte(password))
    }


    @Throws(Exception::class)
    fun hmacMd5(encryptData: ByteArray, encryptKey: ByteArray): ByteArray {
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        val secretKey: SecretKey = SecretKeySpec(encryptKey, "HmacMD5")
        //生成一个指定 Mac 算法 的 Mac 对象
        val mac = Mac.getInstance("HmacMD5")
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey)
        //完成 Mac 操作
        return mac.doFinal(encryptData)
    }
}