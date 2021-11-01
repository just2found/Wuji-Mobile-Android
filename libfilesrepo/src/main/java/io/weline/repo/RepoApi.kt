package io.weline.repo

/** 

Created by admin on 2020/8/1,13:54

 */
interface RepoApi {
    fun getAddress(devId: String): String?
    fun getUserId(): String
    fun isV5(devId: String): Boolean
    fun getToken(): String?
    fun onTokenError(code: Int, msg: String?)
}