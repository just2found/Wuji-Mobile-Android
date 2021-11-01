package io.weline.libimageloader

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap

class ProgressInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request();
        val response = chain.proceed(request);
        val url = request.url().toString();
        val tag = getTagByUrl(url)
        val body = response.body() ?: return response;
        val listener = listeners[tag] ?: return response;
        return response.newBuilder().body(ProgressResponseBody(tag, body, listener)).build();
    }


    companion object {
        private val listeners: ConcurrentHashMap<Any, OnProgressListener> = ConcurrentHashMap()
        fun getTagByUrl(url: String): Any {
            return sGenTagWorker?.getTagByUrl(url) ?: ""
        }

        private var sGenTagWorker: GenTagWorker? = null

        fun setGenTagWorker(genTagWorker: GenTagWorker) {
            sGenTagWorker = genTagWorker
        }

        @JvmStatic
        fun addListeners(tag: Any, listener: OnProgressListener) {
            listeners.put(tag, listener)
        }

        @JvmStatic
        fun removeListeners(tag: Any) {
            listeners.remove(tag)
        }
    }
}
