package io.weline.repo.torrent

import io.weline.repo.torrent.data.BtSession

/** 

Created by admin on 2020/7/3,19:56

 */
abstract class SessionCallback {
    abstract fun onSuccess(btSession: BtSession)
    open fun onFailure(code: Int?, msg: String?) {


    }
}