package net.sdvn.common.internet

import okhttp3.*
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
        val client = OkHttpClient().newBuilder()
                .build()
        val mediaType: MediaType? = MediaType.parse("application/json")
        val body: RequestBody = RequestBody.create(mediaType, "{\r\n    \"ticket\": \"563100277373226-18847_1612504255390_3875189\"\r\n}")
        val request = Request.Builder()
                .url("https://106.75.150.134:8445/v2/agapi/applylogontoken?appid=CN6SDL3H5K4UL55YP77L&partid=Y1DMATNYSMZPOKC3R8NJ")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build()
        val response: Response = client.newCall(request).execute()
        println(response.body()?.string())
    }
}
