package net.sdvn.common.internet

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import okhttp3.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("net.sdvn.common.internet.test", appContext.packageName)
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
