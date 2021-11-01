package net.linkmate.app.poster.utils

import net.sdvn.nascommon.utils.Utils
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class HttpDownloaderUtil {

    fun downFile(urlStr: String?, path: String, fileName: String): Int {
        try {
            var input = getInputStream(urlStr)
            if (input == null){

                return -1
            }
            else {

                try {
                    FileUtils.writeToFileFromInput(path, fileName, input) ?: return -1
                }catch (e: java.lang.Exception){
                    return downFile(urlStr,path,fileName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -2
        }
        return 0
    }

    @Throws(IOException::class)
    fun getInputStream(urlStr: String?): InputStream? {
        var inputStream: InputStream? = null
        try {
            val url = URL(urlStr)
            val urlConn: HttpURLConnection = url.openConnection() as HttpURLConnection
            urlConn.setRequestProperty("accept", "*/*")
            urlConn.setRequestProperty("connection", "Keep-Alive")
            urlConn.setRequestProperty("user-agent", Utils.getUserAgent(Utils.getApp()))
            inputStream = urlConn.inputStream
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return inputStream
    }
}