package net.linkmate.app.repository

import android.app.Application
import libs.source.common.utils.SP
import net.linkmate.app.base.MyConstants

/**
 *
 * @Description: global app sp
 * @Author: todo2088
 * @CreateDate: 2021/3/2 16:55
 */
object SPRepo {
    var showHomeAD by SP(MyConstants.SP_SHOW_HOME_AD, MyConstants.SP_SHOW_HOME_AD_DEFAULT_VALUE)

    fun init(app: Application) {
        SP.init(app, "global_app_sp")
    }

}