package net.sdvn.common.internet.utils

import junit.framework.TestCase
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class CryptAESTest : TestCase() {

    fun testEncode() {
        val keySrc = "563214"
        val data = "1234567a."
        val pair = CryptAES.encode(keySrc, data)
        assertEquals("105745958092b7a4be901a5bd1faabb9", pair.first)
        assertEquals("7ef60726c8c5dbffb0ef194fd19d363b", pair.second)
        val keySrc2 = "778352"
        val data2 = "weline.1"
        val pair2 = CryptAES.encode(keySrc2, data2)
        println( pair2.first)
       println( pair2.second)

    }

    @Throws(Exception::class)
    fun hmacMd5(encryptText: String, encryptKey: String): ByteArray {
        val key = encryptKey.toByteArray(charset("UTF-8"))
        val data = encryptText.toByteArray(charset("UTF-8"))
        //完成 Mac 操作
        return hmacMd5(data, key)
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