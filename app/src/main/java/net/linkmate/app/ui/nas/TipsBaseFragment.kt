package net.linkmate.app.ui.nas

import net.linkmate.app.view.TipsBar
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.receiver.NetworkStateManager

/**
 *  
 *
 *
 * Created by admin on 2020/7/22,14:22
 */
abstract class TipsBaseFragment : BaseFragment() {
    var mTipsBar: TipsBar? = null

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
        super.onNetworkChanged(isAvailable, isWifiAvailable)
        if (!isAvailable) {
            mTipsBar?.showTipWithoutNet()
        }
    }

    override fun onStatusConnection(statusCode: Int) {
        super.onStatusConnection(statusCode)
        if (statusCode == NetworkStateManager.STATUS_CODE_ESTABLISHED) {
            mTipsBar?.close()
        } else {
            if (!isNetAvailable) {
                mTipsBar?.showTipWithoutNet()
            } else {
                mTipsBar?.showTipWithoutService()
            }
        }
    }
}