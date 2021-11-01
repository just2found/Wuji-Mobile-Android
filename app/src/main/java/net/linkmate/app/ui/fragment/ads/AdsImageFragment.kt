package net.linkmate.app.ui.fragment.ads

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_ad_splash.*
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.repository.SPRepo
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.activity.mine.score.RechargeActivity
import net.sdvn.common.internet.protocol.ad.AdModel
import org.view.libwidget.singleClick
import java.util.regex.Pattern

/**
 *
 * @Description: 首页广告弹窗
 * @Author: todo2088
 * @CreateDate: 2021/3/2 13:35
 */
class AdsImageFragment : TransparentFragment() {
    private val adsViewModel by viewModels<AdsViewModel>({ requireActivity() })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adsViewModel.liveDataAdsModel.observe(this, Observer {
                it?.let {
                    container_root?.isVisible = true
                    iv_ad_view?.loadImage(it.imgurl)
                }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_ad_splash
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        iv_ad_view?.singleClick {
            adsViewModel.liveDataAdsModel.value?.let { adModel ->
                if (AdModel.TYPE_RECHARGE == adModel.type) {
                    startActivity(Intent(context, RechargeActivity::class.java))
                    dismiss()
                    return@singleClick
                }
                adModel.redirecturl?.let {
                    if (it.isNotEmpty()) {
                        if (Pattern.compile(MyConstants.INTERNET_URL).matcher(it).matches()) {
                            WebViewActivity.open(requireContext(), adModel.title, it)
                            dismiss()
                        } else {
                            if (BuildConfig.DEBUG) {
                                WebViewActivity.open(requireContext(), adModel.title, "http://weline.io")
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
        iv_cancel.singleClick {
            container_root.isVisible = false
            dismiss()
        }
    }

    fun dismiss() {
        SPRepo.showHomeAD = false
        requireActivity().finish()
    }

}

private fun ImageView.loadImage(imgurl: String?) {
    Glide.with(this)
            .load(imgurl)
            .into(this)
}
