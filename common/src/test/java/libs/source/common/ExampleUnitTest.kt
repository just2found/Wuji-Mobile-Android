package libs.source.common

import libs.source.common.utils.CryptAES
import libs.source.common.utils.Md5Utils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
//        算法： aes ZeroPadding
//        iv: md5(验证码)
//        key: md5(验证码) + md5(md5(验证码))
//        615895
//                314823c413798a429276fa499288eef9
//                314823c413798a429276fa499288eef96afff280597795d0864f84f9e04574db
//                1234567a.
        val authCode = "563214"
        val iv = Md5Utils.md5Byte(authCode)
        val bytesToHexString = Md5Utils.bytesToHexString(iv)
        println(bytesToHexString)
        val md5md5 = Md5Utils.md5md5(iv)
        val key = CryptAES.concat(iv, md5md5)
        val bytesToHexString2 = Md5Utils.bytesToHexString(key)
        println(bytesToHexString2)
        val encode = CryptAES.encodeZeroPadding(key, iv, "1234567a.".toByteArray())
        println(Md5Utils.bytesToHexString(encode))
    }

    @Test
    fun testEncrypt() {
        assertEquals(4, 2 + 2)
//        算法： aes ZeroPadding
//        iv: md5(验证码)
//        key: md5(验证码) + md5(md5(验证码))
//        615895
//                314823c413798a429276fa499288eef9
//                314823c413798a429276fa499288eef96afff280597795d0864f84f9e04574db
//                1234567a.
        val key111 = "96e79218965eb72c92a549dd5a330112d89267ba6e888426c8f798a04f2fb874"
        val message = Md5Utils.parseHex(key111).toString()
        println(message)
        val newpassword = "ce0c56d3f3b5cd099aeaa7255a6f04a55a47932db25c1d169c9e6bf550648cb8d8a57f34d698d030c3c0ae67b0e07bf2"
        val authCode = "1234567a."
        val password = Md5Utils.bytesToHexString(Md5Utils.md5Byte("123456a."))
        println("spwb2: $password")
        val iv = Md5Utils.md5Byte(authCode)
        val bytesToHexString = Md5Utils.bytesToHexString(iv)
        println("iv: $bytesToHexString")
        val md5md5 = Md5Utils.md5md5(iv)
        val key = CryptAES.concat(iv, md5md5)
        val bytesToHexString2 = Md5Utils.bytesToHexString(key)
        println("key: $bytesToHexString2")
        val encode3 = CryptAES.encode(key, iv, password.toByteArray())
        println("CryptAES PKCS5Padding: " + Md5Utils.bytesToHexString(encode3))
        val encode2 = CryptAES.encodeNoPadding(key, iv, password.toByteArray())
        println("CryptAES encodeNoPadding: " + Md5Utils.bytesToHexString(encode2))
        val encode = CryptAES.encodeZeroPadding(key, iv, password.toByteArray())
        println("CryptAES encodeZeroPadding: " + Md5Utils.bytesToHexString(encode))
        val decode = CryptAES.decodeZeroPadding(key, iv, encode)
        println("CryptAES decode: " + Md5Utils.bytesToHexString(decode))
        println(encrypt(key, iv, password))
    }

    fun encrypt(input: ByteArray, ivKey: ByteArray, password: String): ByteArray? {

        val iv = IvParameterSpec(ivKey)

        val passwordChars = CharArray(password.length)
        password.toCharArray(passwordChars, 0, 0, password.length)

        val factory = SecretKeyFactory.getInstance("PBEwithHmacSHA256AndAES_256")
        val spec: KeySpec = PBEKeySpec(passwordChars, ivKey, 2000, 256)

        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")


        val cipher = Cipher.getInstance("AES/CBC/NOPADDING")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)

        return try {
            cipher.doFinal(input).also {
                val message = Md5Utils.bytesToHexString(it)
                println("encrypt bytesToHexString $message")
            }
        } catch (e: Exception) {
            null
        }

    }



}
