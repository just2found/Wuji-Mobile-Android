package io.weline.repo.files.data

import java.io.Serializable

/**
 * User Login information
 *
 *
 * Created by admin on 2020/8/1,13:54
 */
class LoginSession(var devId: String, var address: String, var session: String?) : Serializable {
    var isV5: Boolean = false
    var uid: Int = 0
    var userInfo:UserInfo?=null
    companion object {
        private const val serialVersionUID = 3391671502123128628L
    }
}



