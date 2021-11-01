package io.weline.repo.torrent

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun encrypt() {
        val encrypt = BTHelper.aesEncrypt("123456")
        println(encrypt)
//        assertEquals("d5CUwyXvkWZLjkpeQvp6ClRfte+gXxVrlkAPjPcSNM0=", encrypt)

        BTHelper.map2Encrypt(mapOf("token" to "CN-AG-1063_f537a66e75131e303ab093d0528d83e1"))
    }
}
