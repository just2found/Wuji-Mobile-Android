package net.linkmate.app.ui.nas

import android.os.Bundle
import androidx.activity.OnBackPressedCallback

abstract class TipsBackPressedFragment : TipsBaseFragment() {
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

    override fun onBackPressed(): Boolean {
        return false
    }

    open fun isEnableOnBackPressed(): Boolean {
        return false
    }

}