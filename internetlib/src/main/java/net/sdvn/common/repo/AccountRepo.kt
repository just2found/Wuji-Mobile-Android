package net.sdvn.common.repo

import net.sdvn.cmapi.CMAPI

/** 

Created by admin on 2020/10/20,16:37

 */
object AccountRepo {
    fun getUserId(): String {
        return CMAPI.getInstance().baseInfo.userId ?: ""
    }

    fun getUsername(): String {
        return CMAPI.getInstance().baseInfo.account ?: ""
    }

    fun getTicket(): String {
        return CMAPI.getInstance().baseInfo.ticket ?: ""
    }
}