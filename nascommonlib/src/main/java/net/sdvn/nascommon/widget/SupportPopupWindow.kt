package net.sdvn.nascommon.widget

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.PopupWindow

class SupportPopupWindow(contentView: View, width: Int, height: Int) : PopupWindow(contentView, width, height) {

    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT >= 24) {
            val ew = Rect()
            anchor.getGlobalVisibleRect(ew)
            val height = anchor.resources.displayMetrics.heightPixels - ew.bottom
            setHeight(height)
        }

        super.showAsDropDown(anchor)
    }
}
