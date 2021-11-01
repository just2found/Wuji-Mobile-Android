package net.linkmate.app.ui.fragment.ads

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.repository.AdsRepo
import net.linkmate.app.repository.SPRepo
import net.sdvn.common.internet.protocol.ad.AdModel
import net.sdvn.common.internet.protocol.ad.Ads
import java.io.File

class AdsViewModel : ViewModel() {
    private val _liveDataAds = MediatorLiveData<List<AdModel>>()
    val liveDataAds: LiveData<List<AdModel>?> = _liveDataAds

    private val _liveDataAds2 = MediatorLiveData<Resource<Ads>>()
    val liveDataAds2: LiveData<Resource<Ads>> = _liveDataAds2

    private val _liveDataAdsModel = MediatorLiveData<AdModel>()
    val liveDataAdsModel: LiveData<AdModel?> = _liveDataAdsModel

    private val adsRepo: AdsRepo = AdsRepo()
    fun refreshBanner() {
        _liveDataAds.addSource(adsRepo.getAdsBanner(), Observer {
            _liveDataAds.postValue(it.data?.list)
        })
    }

    fun getBanner() {
        _liveDataAds2.addSource(adsRepo.getAdsBanner("ads"), Observer {

            _liveDataAds2.postValue(it)
        })
    }

    fun shouldShowHome(activity: AppCompatActivity) {
        if (SPRepo.showHomeAD) {
            LoginManager.getInstance().loginedData.observe(activity, Observer {
                if (it) {
                    getBanner()
                }
            })
            liveDataAds2.observe(activity, Observer {
                if (it.status == Status.SUCCESS && SPRepo.showHomeAD) {
                    it.data?.list?.getOrNull(0)?.let {
                        val applicationContext = activity.applicationContext
                        AppExecutors.instance.networkIO().execute {
                            Glide.with(applicationContext)
                                    .download(it.imgurl)
                                    .addListener(object : RequestListener<File?> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File?>?, isFirstResource: Boolean): Boolean {
                                            return false
                                        }

                                        override fun onResourceReady(resource: File?, model: Any?, target: Target<File?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            AppExecutors.instance.mainThread().execute {
                                                if (SPRepo.showHomeAD) {
                                                    activity.run {
                                                        val intent = Intent(this, HomeAdsActivity::class.java)
                                                        intent.putExtra("data", it)
                                                        startActivity(intent)
                                                        overridePendingTransition(R.anim.fragment_fade_enter, 0)
                                                    }
                                                }
                                            }
                                            return true
                                        }
                                    })
                                    .submit()

                        }
                    }
                }
            })

        }
    }

    fun parseIntent(intent: Intent?) {
        intent?.run {
            _liveDataAdsModel.postValue(getSerializableExtra("data") as? AdModel?)
        }
    }
}
