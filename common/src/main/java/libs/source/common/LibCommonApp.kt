package libs.source.common;

import android.app.Application;
import libs.source.common.utils.Utils

object LibCommonApp {
    private lateinit var sApp: Application
    @JvmStatic
    fun getApp(): Application {
        return sApp
    }

    fun onCreate(app: Application) {
        sApp = app
        Utils.init(app)
    }

    fun onTerminate() {

    }
}