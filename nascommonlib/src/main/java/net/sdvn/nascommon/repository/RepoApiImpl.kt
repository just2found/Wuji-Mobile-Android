package net.sdvn.nascommon.repository

import io.weline.repo.RepoApi
import io.weline.repo.SessionCache
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/2
 */
class RepoApiImpl : RepoApi {
    override fun getAddress(devId: String): String? {
        return SessionManager.getInstance().getDeviceVipById(devId)
    }

    override fun getUserId(): String {
        return SessionManager.getInstance().userId
    }

    override fun isV5(devId: String): Boolean {
        return SessionCache.instance.isV5(devId)
    }

    override fun getToken(): String? {
        return LoginTokenUtil.getToken()
    }

    override fun onTokenError(code: Int, msg: String?) {

    }
}