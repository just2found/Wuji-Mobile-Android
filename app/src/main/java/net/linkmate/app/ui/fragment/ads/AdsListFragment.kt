package net.linkmate.app.ui.fragment.ads

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.fragment_ads_list.*
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.ui.activity.WebViewActivity
import net.sdvn.common.internet.protocol.ad.AdModel
import net.sdvn.nascommon.utils.Utils
import java.util.regex.Pattern

/** 

Created by admin on 2020/7/31,09:23

 */
class AdsListFragment : TransparentFragment() {
    private val adsViewModel by viewModels<AdsViewModel>({ requireActivity() })
    private val adsData = mutableListOf<AdModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginManager.getInstance().loginedData.observe(this, Observer {
            if (it) {
                adsViewModel.refreshBanner()
            }
        })
        adsViewModel.liveDataAds.observe(this, Observer { list ->
            adsData.clear()
            list?.filter { it.type == AdModel.TYPE_RECHARGE }?.let {
                adsData.addAll(it)
            }
            baseQuickAdapter.setNewData(adsData)
            ads_list_container.isVisible = adsData.isNotEmpty()
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ads_list
    }

    private val baseQuickAdapter = object : BaseQuickAdapter<AdModel, BaseViewHolder>
    (R.layout.item_ad) {
        override fun convert(holder: BaseViewHolder, adModel: AdModel?) {
            adModel?.let {
                holder.getView<ImageView>(R.id.iv_ad).let { imageView ->
                    Glide.with(imageView).load(adModel.imgurl).into(imageView)
                }
            }
        }

    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        if (LoginManager.getInstance().isLogined) {
            adsViewModel.refreshBanner()
        }
        recycle_view.layoutManager = LinearLayoutManager(view.context)
        recycle_view.adapter = baseQuickAdapter
        baseQuickAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
            if (Utils.isFastClick(view)) {
                return@setOnItemClickListener
            }
            val adModel = baseQuickAdapter.getItem(i) as? AdModel
            adModel?.redirecturl?.let {
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
    }

}