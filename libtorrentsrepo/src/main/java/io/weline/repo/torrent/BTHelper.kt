package io.weline.repo.torrent

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.torrent.constants.BT_Config
import io.weline.repo.torrent.data.BtVersion
import libs.source.common.InterfaceDeps
import libs.source.common.livedata.Resource
import libs.source.common.utils.*
import okhttp3.HttpUrl
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

/** 

Created by admin on 2020/7/3,10:01

 */
object BTHelper {
    private var deps: InterfaceDeps? = null
    private val repoListRateLimit = RateLimiter<String>(3, TimeUnit.SECONDS)

    @JvmStatic
    fun checkAvailable(ip: String?, callback: Consumer<Resource<Boolean>>): Disposable {
        return Observable.create<String> {
            if (!ip.isNullOrEmpty()) {
                it.onNext(ip)
            } else {
                it.onNext("")
            }
            it.onComplete()
        }.flatMap {
            if (it.isNotEmpty() && repoListRateLimit.shouldFetch(it, 3000)) {
                return@flatMap RetrofitFactory.createRetrofit(it,
                        isUseLiveData = false, timeout = 2)
                        .create(BTServerApiService::class.java)
                        .version()

            } else {
                return@flatMap Observable.just(BtBaseResult<BtVersion>(-1, "ip is null"))
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful) {
                        callback.accept(Resource.success(true))
                    } else {
                        callback.accept(Resource.error(it.msg ?: "", false, it.status))
                    }
                }, {
                    Timber.e(it)
                    callback.accept(Resource.error(it.message ?: "$it", false, -402))
                })

    }

    @JvmStatic
    fun getHost(ip: String): String {
        return HttpUrl.Builder()
                .scheme(BT_Config.SCHEME)
                .host(ip)
                .port(if (BT_Config.isDebug) BT_Config.PORT_DEBUG else BT_Config.PORT)
                .build()
                .toString()
    }

    fun isLocal(devId: String): Boolean {
        return BT_Config.BT_LOCAL_DEVICE_ID == devId
    }

    fun init(deps: InterfaceDeps) {
        this.deps = deps;
    }

    fun getDeviceVipById(devId: String): String? {
        return deps?.getDeviceVipById(devId)
    }

    fun getWSUrl(host: String): String {
        return "ws://$host:${if (BT_Config.isDebug) BT_Config.PORT_DEBUG else BT_Config.PORT}/ws/interface"
    }

    const val appid = "m2bBRIjyI1Y17PsL"
    const val appkey = "HWccxQPgcTp7dSj3"
    const val aeskey = "PRE2Qmkxe23dKiK3"

    @Throws(Exception::class)
    fun signSrcData(body: Map<String, Any>): String {
//        - string appid = "ve1rUvvwDPFf5F3h";
//        - string appkey= "pi6kRpQe7sm6iQWW";
//        - string json_data = json_marshal(data);
//        - string sign = base64_encode(sha1_hmac(appid+json_data,appkey));
//        - 上述签名完成，然后继续做aes对称加密，假设上述最终数据为 string data(以下为伪代码);
        println("request body srcData: $body")
        val mapToJson = mapToJson(body)
        val src: ByteArray = HMACSHA1.hmacSHA1(appid + mapToJson, appkey)
        println(src)
        val sign = base64EncodeToString(Md5Utils.hexString(src).toByteArray())
        println(sign)
        val params: MutableMap<String, Any> = linkedMapOf()
        params["appid"] = appid
        params["data"] = body
        params["sign"] = sign
        val data2: String = mapToJson(params)
        println(data2)
        return data2
    }

    fun aesEncrypt(data2: String): String {
        return aesEncrypt(data2.toByteArray())
    }

    fun aesEncrypt(data2: ByteArray): String {
//        - string aeskey="TZpeJmfjvDnGWXlj"
//        - []byte random = rand(16)  //16个字符的随机向量
//        - []byte data2=  PKCS5Padding(data, 16)  //采用pkcs#5填充
//        - []byte data3 = aes.encode(data2,random,aeskey)
//        - []byte data4 = random+data3 (拼接向量+加密后数据)
//        - string data5 = base64_encode(data4)
//        - 最终把data5以post方式把数据传输过去。
        val random = SecureRandom().generateSeed(16)
//        val random = "0000000000000000".toByteArray()// byteArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
        println(Md5Utils.bytesToHexString(random))
        val data3 = CryptAES.encode(aeskey.toByteArray(), random, data2)
        println(base64EncodeToString(data3))
        val data4 = CryptAES.concat(random, data3)
        println(base64EncodeToString(data4))
        return base64EncodeToString(data4)
    }

    fun base64EncodeToString(src: ByteArray): String {
        return Base64.encode(src, Base64.DEFAULT).toString(StandardCharsets.UTF_8).trim(' ', '\n', '\r')
    }

    fun map2Encrypt(body: Map<String, Any>): String {
        return aesEncrypt(signSrcData(body))
    }

    private fun mapToJson(body: Map<String, Any>): String {
        val encodeJSON = GsonUtils.encodeJSONCatchEx(body)
        return encodeJSON
    }

}