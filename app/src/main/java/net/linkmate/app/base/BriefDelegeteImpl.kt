package net.linkmate.app.base

import android.widget.ImageView
import android.widget.TextView
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.BriefDelegete

/**
 * @author Raleigh.Luo
 * date：21/3/4 17
 * describe：
 */
class BriefDelegeteImpl : BriefDelegete {
    private val viewModel = BriefCacheViewModel()
    override fun loadDeviceBrief(devId: String, brief: BriefModel?, ivImage: ImageView?, tvContent: TextView?, defalutImage: Int, ivBackgroud: ImageView?, defalutBgImage: Int) {
        viewModel.loadBrief(devId, brief, ivImage, tvContent, defalutImage, ivBackgroud, defalutBgImage)
    }
}