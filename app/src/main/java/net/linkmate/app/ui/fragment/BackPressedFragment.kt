package net.linkmate.app.ui.fragment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback

/** 

Created by admin on 2020/8/12,20:14

 */
abstract class BackPressedFragment : BaseFragment() {
    private val onBackPressedCallBack = object : OnBackPressedCallback(isEnableOnBackPressed()) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallBack)
    }

    fun setEnableOnBackPressed(isEnable: Boolean) {
        onBackPressedCallBack.isEnabled = isEnable
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    open fun isEnableOnBackPressed(): Boolean {
        return false
    }

}