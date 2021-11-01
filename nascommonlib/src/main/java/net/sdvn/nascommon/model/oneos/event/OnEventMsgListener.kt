package net.sdvn.nascommon.model.oneos.event

import org.json.JSONObject

interface OnEventMsgListener {
    fun onEventMsg(json: JSONObject)
}
