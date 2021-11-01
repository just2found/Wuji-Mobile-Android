package net.linkmate.app.ui.nas.devhelper

import android.os.Bundle
import androidx.navigation.NavArgs
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.widget.DeviceSelectDialog

data class SelectDeviceFragmentArgs(
    val requestKey: String,
    val filterType: FilterType = FilterType.IS_ONLINE,
    val isLocalEnable: Boolean = false,
    val filterExtIds: ArrayList<String>? = null
) : NavArgs {
    fun toBundle(): Bundle {
        val result = Bundle()
        if (filterExtIds != null)
            result.putStringArrayList("filterExtIds", this.filterExtIds)
        result.putString("requestKey", this.requestKey)
        result.putBoolean("isLocalEnable", this.isLocalEnable)
        result.putSerializable("filterType", this.filterType)
        return result
    }

    companion object {
        enum class FilterType {
            ALL,
            IS_ONLINE,
            FILE_SHARE,
            BT_DOWNLOAD
        }

        @JvmStatic
        fun fromBundle(bundle: Bundle): SelectDeviceFragmentArgs {
            bundle.setClassLoader(SelectDeviceFragmentArgs::class.java.classLoader)

            val __requestKey: String?
            if (bundle.containsKey("requestKey")) {
                __requestKey = bundle.getString("requestKey")
                if (__requestKey == null) {
                    throw java.lang.IllegalArgumentException("Argument \"requestKey\" is marked as non-null but was passed a null value.")
                }
            } else {
                throw java.lang.IllegalArgumentException("Required argument \"requestKey\" is missing and does not have an android:defaultValue")
            }

            var __action: FilterType?=FilterType.IS_ONLINE
            if (bundle.containsKey("filterType")) {
                __action = bundle.getSerializable("filterType") as FilterType?
                if (__action == null) {
                    throw IllegalArgumentException("Argument \"filterType\" is marked as non-null but was passed a null value.")
                }
            }
            var __isLocalEnable: Boolean?=false
            if (bundle.containsKey("isLocalEnable")) {
                __isLocalEnable = bundle.getBoolean("isLocalEnable")
                if (__isLocalEnable == null) {
                    throw IllegalArgumentException("Argument \"isLocalEnable\" is marked as non-null but was passed a null value.")
                }
            }
            var __filterExtIds: ArrayList<String>?=null
            if (bundle.containsKey("filterExtIds")) {
                __filterExtIds = bundle.getStringArrayList("filterExtIds")
            }
            return SelectDeviceFragmentArgs(__requestKey, __action!!, __isLocalEnable!!, __filterExtIds)
        }
    }
}
