package net.sdvn.common

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import net.sdvn.common.internet.R

/** 

Created by admin on 2020/10/20,17:33

 */
object Local {

    private lateinit var app: Application
    private val componentCallbacks = object : ComponentCallbacks {
        override fun onLowMemory() {
        }

        override fun onConfigurationChanged(newConfig: Configuration?) {
            localLanguage = app.getString(R.string.api_params_language)
        }
    }
    private lateinit var localLanguage: String

    @JvmStatic
    fun init(app: Application) {
        this.app = app
        localLanguage = app.getString(R.string.api_params_language)
        app.registerComponentCallbacks(componentCallbacks)
    }

    fun onTerminate() {
        app.unregisterComponentCallbacks(componentCallbacks)
    }

    @JvmStatic
    fun getApiLanguage(): String {
        return localLanguage
    }

    const val lang_en: String = "en"
    const val lang_ar: String = "ar"
    const val lang_de: String = "de"
    const val lang_es: String = "es"
    const val lang_fr: String = "fr"
    const val lang_it: String = "it"
    const val lang_ja: String = "ja"
    const val lang_ko: String = "ko"
    const val lang_ru: String = "ru"
    const val lang_th: String = "th"
    const val lang_vi: String = "vi"
    const val lang_zh: String = "zh_cn"
    const val lang_tw: String = "zh_tw"

    @JvmStatic
    fun getLocalName(lastName: String?, firstName: String?): String {
        val language = getApiLanguage()
        return if (language == lang_zh
                || language == lang_tw
                || language == lang_ja
                || language == lang_ko) {
            if (lastName.isNullOrEmpty()) {
                firstName ?: ""
            } else {
                if (firstName.isNullOrEmpty()) {
                    lastName
                } else {
                    "$lastName $firstName"
                }
            }
        } else {
            if (lastName.isNullOrEmpty()) {
                firstName ?: ""
            } else {
                if (firstName.isNullOrEmpty()) {
                    lastName
                } else {
                    "$firstName $lastName"
                }
            }
        }
    }

    @JvmStatic
    fun isHans(): Boolean {
        return getApiLanguage() == lang_zh
    }

    @JvmStatic
    fun isHant(): Boolean {
        return getApiLanguage() == lang_tw
    }

}