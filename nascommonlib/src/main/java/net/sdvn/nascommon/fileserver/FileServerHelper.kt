package net.sdvn.nascommon.fileserver

import com.google.gson.Gson
import libs.source.common.utils.Base64
import timber.log.Timber
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** 

Created by admin on 2020/11/16,18:03

 */
object FileServerHelper {

    @Throws(Exception::class)
    fun encrypt(map: Map<String, Any>): String? {
        val appid = "d1EJnGGF0Eyn6Wxx"
        val appkey = "6LJx909Q9KVBkVZC"
        val aeskey = "OsBhtflT0P2WaOVY"
        val jsonData = Gson().toJson(map)
        val encryptText = appid + jsonData
        val src = hmacSHA1(encryptText, appkey)
        val sign = base64EncodeToString(hexString(src).toByteArray())
        val params: MutableMap<String, Any> = HashMap()
        params["appid"] = appid
        params["sign"] = sign
        params["data"] = map
        val data2 = Gson().toJson(params)
        Timber.d(data2)
        val iv = SecureRandom().generateSeed(16)
        val encode = encode(aeskey.toByteArray(), iv, data2.toByteArray())
        return base64EncodeToString(concat(iv, encode))
    }

    fun base64EncodeToString(src: ByteArray): String {
        val encode = Base64.encodeToString(src, Base64.DEFAULT)
        return encode.replace("\n".toRegex(), "").replace("\r".toRegex(), "").trim { it <= ' ' }
    }


    fun hexString(result: ByteArray): String {
        //将结果转换成十六进制字符串
        val sb = StringBuilder()
        for (b in result) {
            val number: Int = b.toInt() and 0xff
            val str = Integer.toHexString(number)
            if (str.length == 1) {
                sb.append("0")
            }
            sb.append(str)
        }
        return sb.toString()
    }

    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun hmacSHA1(encryptText: String, encryptKey: String): ByteArray {
        val data = encryptKey.toByteArray(charset(ENCODING))
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        val secretKey: SecretKey = SecretKeySpec(data, MAC_NAME)
        //生成一个指定 Mac 算法 的 Mac 对象
        val mac = Mac.getInstance(MAC_NAME)
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey)
        val text = encryptText.toByteArray(charset(ENCODING))
        //完成 Mac 操作
        return mac.doFinal(text)
    }

    private const val MAC_NAME = "HmacSHA1"
    private const val ENCODING = "UTF-8"


    private const val cipher_type = "AES/CBC/PKCS5Padding"

    @Throws(java.lang.Exception::class)
    fun encode(skey_b_arr: ByteArray, iv_arr: ByteArray, data: ByteArray?): ByteArray {
        return process(Cipher.ENCRYPT_MODE, skey_b_arr, iv_arr, data)
    }

    @Throws(java.lang.Exception::class)
    private fun process(mode: Int, skey: ByteArray, iv: ByteArray, data: ByteArray?): ByteArray {

        val key = SecretKeySpec(skey, "AES")
        val cipher = Cipher.getInstance(cipher_type)
        cipher.init(mode, key, IvParameterSpec(iv))
        return cipher.doFinal(data)

    }

    fun concat(first: ByteArray, second: ByteArray): ByteArray {
        val result = Arrays.copyOf(first, first.size + second.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }

}