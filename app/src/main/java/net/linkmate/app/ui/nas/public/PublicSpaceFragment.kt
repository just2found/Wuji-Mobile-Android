package net.linkmate.app.ui.nas.public

import android.view.View
import kotlinx.android.synthetic.main.fragment_public_space.*
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBackPressedFragment

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/5/28
 */
class PublicSpaceFragment: TipsBackPressedFragment() {

    override fun getTopView(): View? {
        return titleBackLayout
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_public_space
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar

    }


}