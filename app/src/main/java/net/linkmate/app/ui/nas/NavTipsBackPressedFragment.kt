package net.linkmate.app.ui.nas

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController

abstract class NavTipsBackPressedFragment : TipsBaseFragment() {
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
        return findNavController().popBackStack()
    }

    open fun isEnableOnBackPressed(): Boolean {
        return true
    }

}