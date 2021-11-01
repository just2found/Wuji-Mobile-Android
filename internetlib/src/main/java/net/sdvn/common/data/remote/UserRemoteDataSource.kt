package net.sdvn.common.data.remote

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.GsonBaseProtocolV2
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.InitParamsV2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.common.internet.utils.CryptAES
import net.sdvn.common.internet.utils.Utils
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class UserRemoteDataSource {
    fun registerV2(loginname: String?, phone: String?, email: String?, password: String, firstname: String?
                   , lastname: String?, nickname: String?, verifycode: String, listener: ResultListener<GsonBaseProtocol>): HttpLoader {
        val v2AgApiHttpLoader: InitParamsV2AgApiHttpLoader = object : InitParamsV2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            override fun initParams(vararg objs: Any) {
                action = "userregisterv2"
                bodyMap = ConcurrentHashMap()
                put("loginname", loginname)
                if (!TextUtils.isEmpty(phone)) {
                    put("phone", phone)
                }
                if (!TextUtils.isEmpty(email)) {
                    put("email", email)
                }
                try {
                    val encode = CryptAES.encode(verifycode, password)
                    put("password", encode.first)
                    put("sign", encode.second)
                } catch (ignore: Exception) {
                    Timber.d("encode failed")
                }
                put("firstname", firstname)
                put("lastname", lastname)
                put("nickname", nickname)
            }
        }
        val type = object : TypeToken<GsonBaseProtocolV2<ShareUser>>() {}.type
        v2AgApiHttpLoader.executor(type, listener)
        return v2AgApiHttpLoader
    }

    fun resetPasswd(phone: String?, email: String?, auxcode: String, password: String, listener: ResultListener<GsonBaseProtocol>): HttpLoader {
        val v2AgApiHttpLoader: InitParamsV2AgApiHttpLoader = object : InitParamsV2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            override fun initParams(vararg objs: Any) {
                action = "resetpassword"
                bodyMap = ConcurrentHashMap()
                try {
                    val encode = CryptAES.encode(auxcode, CryptAES.md5HexString(password))
                    put("newpassword", encode.first)
                    put("sign", encode.second)
                } catch (ignore: Exception) {
                    Timber.d("encode failed")
                }
                put("phone", phone)
                put("email", email)
            }
        }
        val type = object : TypeToken<GsonBaseProtocolV2<Any>>() {}.type
        v2AgApiHttpLoader.executor(type, listener)
        return v2AgApiHttpLoader
    }

    fun modifyPwd(oldPwd: String, newPwd: String, listener: ResultListener<GsonBaseProtocol>): HttpLoader {

        val v2AgApiHttpLoader: InitParamsV2AgApiHttpLoader = object : InitParamsV2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            override fun initParams(vararg objs: Any) {
                bodyMap = ConcurrentHashMap()
                action = "modifypassword"
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                try {
                    put("userid", CryptAES.encode(oldPwd, (CMAPI.getInstance().baseInfo.userId
                            ?: "")).first)
                    val encode = CryptAES.encode(oldPwd, CryptAES.md5HexString(newPwd))
                    put("newpassword", encode.first)
                    put("sign", encode.second)
                } catch (ignore: Exception) {
                    Timber.d("encode failed")
                }
            }
        }
        val type = object : TypeToken<GsonBaseProtocolV2<Any>>() {}.type
        v2AgApiHttpLoader.executor(type, listener)
        return v2AgApiHttpLoader
    }

    /**
     * 密码效验
     * */
    fun passwordConfirm(data: String, listener: ResultListener<GsonBaseProtocol>): HttpLoader {
        val v2AgApiHttpLoader: InitParamsV2AgApiHttpLoader = object : InitParamsV2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            override fun initParams(vararg objs: Any) {
                bodyMap = ConcurrentHashMap()
                action = "passwordconfirm"
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                try {
                    val timestamp = System.currentTimeMillis() / 1000
                    val randomNum = Utils.genRandomNum(16)
                    val source = "password=${Utils.md5(data)}&random=${randomNum}&time=$timestamp"
                    val sha256 = Utils.sha256(source)
                    put("timestamp", timestamp)
                    put("random", randomNum)
                    put("signature", sha256)
                } catch (ignore: Exception) {
                    Timber.d("encode failed")
                }
            }
        }
        val type = object : TypeToken<GsonBaseProtocolV2<Any>>() {}.type
        v2AgApiHttpLoader.executor(type, listener)
        return v2AgApiHttpLoader
    }
}