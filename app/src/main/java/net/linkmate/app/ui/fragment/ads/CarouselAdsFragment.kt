package net.linkmate.app.ui.fragment.ads

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_ads_carousel.*
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.activity.mine.score.RechargeActivity
import net.linkmate.app.ui.fragment.main.HomeFragment
import net.sdvn.common.internet.protocol.ad.AdModel
import java.util.regex.Pattern

/** 

Created by admin on 2020/7/30,18:56

 */
class CarouselAdsFragment : TransparentFragment() {
    private val adsViewModel by viewModels<AdsViewModel>()
    private val adsData = mutableListOf<AdModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginManager.getInstance().loginedData.observe(this, Observer {
            if (it) {
                adsViewModel.refreshBanner()
            }
        })
        adsViewModel.liveDataAds.observe(this, Observer {
            adsData.clear()
            if (it != null) {
                adsData.addAll(it)
            }
            carousel_view.setImageList(adsData.mapIndexed { index, _ -> index })
            carousel_view_container.isVisible = adsData.isNotEmpty()
            if (adsData.isNotEmpty()) {
                if (this.parentFragment is HomeFragment) {
                    (this.parentFragment as HomeFragment).onBannerShow(-1f);
                }
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ads_carousel
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        if (LoginManager.getInstance().isLogined) {
            adsViewModel.refreshBanner()
        }
        carousel_view.initImageLoader { imageView, imageResId ->
            Glide.with(imageView).load(adsData[imageResId].imgurl).into(imageView)
        }
        carousel_view.openAutoScroll()
        carousel_view.setShowIndicator(true)
        carousel_view.setScrollInterval(6000)
        carousel_view.setOnPageClickListener { index, imageResId ->
            val adModel = adsData[imageResId]
            if (AdModel.TYPE_RECHARGE == adModel.type) {
                startActivity(Intent(context, RechargeActivity::class.java))
                return@setOnPageClickListener
            }
            adModel.redirecturl?.let {
                if (it.isNotEmpty()) {
                    if (Pattern.compile(MyConstants.INTERNET_URL).matcher(it).matches()) {
                        WebViewActivity.open(requireContext(), adModel.title, it)
                    } else {
                        if (BuildConfig.DEBUG) {
                            WebViewActivity.open(requireContext(), adModel.title, "http://weline.io")
                        }
                    }
                }
            }
        }
        iv_cancel.isVisible = false
        iv_cancel.setOnClickListener {
            if (this.parentFragment is HomeFragment) {
                (this.parentFragment as HomeFragment).onBannerClose();
            }
            carousel_view_container.isVisible = false
        }

    }
}