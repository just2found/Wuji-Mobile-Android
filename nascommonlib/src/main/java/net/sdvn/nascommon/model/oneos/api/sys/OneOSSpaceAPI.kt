package net.sdvn.nascommon.model.oneos.api.sys

import androidx.annotation.Keep
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.http.OnHttpRequestListener
import net.sdvn.nascommon.model.oneos.OneOSHardDisk
import net.sdvn.nascommon.model.oneos.api.BaseAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommonlib.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * OneSpace OS Get Device Mac Address API
 *
 *
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
class OneOSSpaceAPI(loginSession: LoginSession) : BaseAPI(loginSession, OneOSAPIs.SYSTEM_SYS) {

    private var listener: OnSpaceListener? = null
    private var username: String? = null
    private var uid = 0

    init {
        username = loginSession.userInfo?.name
        uid = loginSession.userInfo?.uid!!

    }

    fun setOnSpaceListener(listener: OnSpaceListener) {
        this.listener = listener
    }

    fun query(username: String) {
        this.username = username
        query(false)
    }

    fun query(isOneOSSpace: Boolean) {
        val params = HashMap<String, Any>()
        var method: String? = null
        val action: String
        if (isOneOSSpace) {
            action = OneOSAPIs.SYSTEM_SYS
            method = "hdsmart"
        } else {
            action = OneOSAPIs.USER
            params["username"] = username!!
            //params.put("method", "space");
            method = "space"
        }
        oneOsRequest.action = action
        setParams(params)
        setMethod(method)
        httpRequest.post(oneOsRequest, object : OnHttpRequestListener {
            override fun onStart(url: String) {

                if (listener != null) {
                    listener!!.onStart(url)
                }
            }

            override fun onSuccess(url: String, result: String) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:$result")
                if (listener != null) {
                    try {
                        if (isOneOSSpace) {
                            val hd1 = OneOSHardDisk()
                            val hd2 = OneOSHardDisk()
                            getHDInfo(result, hd1, hd2)
                            listener!!.onSuccess(url, isOneOSSpace, hd1, hd2)
                        } else {
                            val datajson = JSONObject(result)
                            val hd1 = OneOSHardDisk()
                            val space = datajson.getLong("space") * 1024 * 1024 * 1024
                            val used = datajson.getLong("used")
                            hd1.total = space
                            hd1.used = used
                            hd1.free = space - used
                            listener!!.onSuccess(url, isOneOSSpace, hd1, null)
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        listener!!.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.resources.getString(R.string.error_json_exception))
                    }

                }
            }

            override fun onFailure(url: String, httpCode: Int, errorNo: Int, strMsg: String) {
                if (listener != null) {
                    listener!!.onFailure(url, errorNo, strMsg)
                }
            }
        })

    }

    fun getHDInfo(result: String, hd1: OneOSHardDisk, hd2: OneOSHardDisk) {
        OneOSSpaceAPI.getHDInfo(result, hd1, hd2)
    }

    @Keep
    interface OnSpaceListener {
        fun onStart(url: String)

        fun onSuccess(url: String, isOneOSSpace: Boolean, hd1: OneOSHardDisk, hd2: OneOSHardDisk?)

        fun onFailure(url: String, errorNo: Int, errorMsg: String)
    }

    companion object {
        private val TAG = OneOSSpaceAPI::class.java.simpleName

        fun getHDInfo(result: String, hd1: OneOSHardDisk, hd2: OneOSHardDisk) {
            val datajson = JSONObject(result)
            var spaceStr = datajson.getString("vfs")
            var spaceStr2: String? = null
            var smart1: String? = null
            var smart2: String? = null
            var name1: String? = null
            var name2: String? = null
            if (datajson.has("hds")) {
                val hds = datajson.getString("hds")
                if (!EmptyUtils.isEmpty(hds)) {
                    val jsonArray = JSONArray(hds)
                    for (i in 0 until jsonArray.length()) {
                        val hdsJSON = jsonArray.getJSONObject(i)
                        if (hdsJSON.has("vfs")) {
                            val vfs = hdsJSON.getString("vfs")
                            if (i == 0) {
                                spaceStr = vfs
                            } else {
                                spaceStr2 = vfs
                            }
                        }

                        if (hdsJSON.has("smart")) {
                            val smart = hdsJSON.getString("smart")
                            if (i == 0) {
                                smart1 = smart
                            } else {
                                smart2 = smart
                            }
                        }

                        if (hdsJSON.has("name")) {
                            val name = hdsJSON.getString("name")
                            if (i == 0) {
                                name1 = name
                            } else {
                                name2 = name
                            }
                        }
                    }
                }
            }


            hd1.name = name1
            var json: JSONObject
            if (!EmptyUtils.isEmpty(spaceStr) && spaceStr != "{}") {
                // parse space
                json = JSONObject(spaceStr)
                val bavail = json.getLong("bavail")
                val blocks = json.getLong("blocks")
                val frsize = json.getLong("frsize")
                hd1.total = blocks * frsize
                hd1.free = bavail * frsize
                hd1.used = hd1.total - hd1.free
            }
            if (!EmptyUtils.isEmpty(smart1) && smart1 != "{}") {
                // parse smart
                json = JSONObject(smart1)
                if (json.has("Temperature_Celsius"))
                    hd1.tmp = json.getInt("Temperature_Celsius")
                if (json.has("Power_On_Hours"))
                    hd1.time = json.getInt("Power_On_Hours")
            }


            hd2.name = name2
            if (!EmptyUtils.isEmpty(spaceStr2) && spaceStr2 != "{}") {
                // parse space
                json = JSONObject(spaceStr2)
                val bavail = json.getLong("bavail")
                val blocks = json.getLong("blocks")
                val frsize = json.getLong("frsize")
                hd2.total = blocks * frsize
                hd2.free = bavail * frsize
                hd2.used = hd2.total - hd2.free
            }
            if (!EmptyUtils.isEmpty(smart2) && smart2 != "{}") {
                // parse smart
                json = JSONObject(smart2)
                if (json.has("Temperature_Celsius"))
                    hd2.tmp = json.getInt("Temperature_Celsius")
                if (json.has("Power_On_Hours"))
                    hd2.time = json.getInt("Power_On_Hours")
            }

        }
    }
}
