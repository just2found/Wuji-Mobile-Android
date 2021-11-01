package net.sdvn.nascommon

import android.widget.ImageView
import android.widget.TextView
import androidx.arch.core.util.Function
import io.weline.repo.data.model.DataDevIntroduction
import net.sdvn.common.vo.BriefModel

/**
 * @author Raleigh.Luo
 * date：21/3/4 17
 * describe：
 */
interface BriefDelegete {
    fun loadDeviceBrief(devId: String, brief: BriefModel?, ivImage: ImageView? = null, tvContent: TextView? = null, defalutImage: Int = 0,
                        ivBackgroud: ImageView? = null, defalutBgImage: Int = 0)
}