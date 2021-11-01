package net.linkmate.app.ui.simplestyle

import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.arch.core.util.Function
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Key
import net.linkmate.app.R
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.base.MyApplication
import net.linkmate.app.manager.BriefManager
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import java.io.File
import java.security.MessageDigest

/**  公共 处理圈子设备简介VM
 * @author Raleigh.Luo
 * date：20/11/20 09
 * describe：
 */
open class BriefCacheViewModel : BaseViewModel() {
    //保存文件夹,优先使用外部存储
    private val outputDir = if (MyApplication.getContext().externalCacheDir == null || TextUtils.isEmpty(MyApplication.getContext().externalCacheDir.absolutePath)) {//优先使用外部存储
        MyApplication.getContext().cacheDir.absolutePath + File.separator + "images" + File.separator + "brief"
    } else {
        MyApplication.getContext().externalCacheDir.absolutePath + File.separator + "images" + File.separator + "brief"
    }

    /**
     * 使用缓存加载 设备简介，因都是列表，直接更新UI
     * @param devId 设备id
     * @param defalutImage 默认／错误图片
     * @param isLoadOneDeviceBrief //是否单个设备加载所有简介页面
     * 单设备加载，相同设备，且图片已被加载过，第二次加载时，不设置占位图，增加用户体验
     * 注意不同设备，在调用加载简介时，需自行处理清空Tag
     */
    fun loadBrief(devId: String, brief: BriefModel?, ivImage: ImageView? = null, tvContent: TextView? = null, defalutImage: Int = R.drawable.icon_device_wz,
                  ivBackgroud: ImageView? = null, defalutBgImage: Int = R.color.breif_bg_defualt_color, For: String = BriefRepo.FOR_DEVICE, isLoadOneDeviceBrief: Boolean = false) {
        tvContent?.setText(if (!TextUtils.isEmpty(brief?.brief)) brief?.brief else MyApplication.getContext().getString(R.string.no_summary))
        brief?.let {
            var isBackgroudNeedRequestRemoteBrief = false
            var isPortraitNeedRequestRemoteBrief = false
            ivBackgroud?.let {
                if (!TextUtils.isEmpty(brief.backgroudPath)) {
                    if (File(brief?.backgroudPath).exists()) {//有本地图片
                        loadImage(ivBackgroud, brief?.backgroudPath, brief.portraitTimeStamp
                                ?: 0L, defalutBgImage, isLoadOneDeviceBrief)
                    } else {//本地图片丢失
                        isBackgroudNeedRequestRemoteBrief = true
                    }
                }
            }
            ivImage?.let {
                if (!TextUtils.isEmpty(brief.portraitPath)) {
                    if (File(brief?.portraitPath).exists()) {//有本地图片
                        loadImage(ivImage, brief.portraitPath, brief.portraitTimeStamp
                                ?: 0L, defalutImage, isLoadOneDeviceBrief)
                    } else {//本地图片丢失
                        isPortraitNeedRequestRemoteBrief = true
                    }
                }
            }
            if (!TextUtils.isEmpty(devId) && (isPortraitNeedRequestRemoteBrief || isBackgroudNeedRequestRemoteBrief)) {
                //哪个属性文件丢失，就请求哪个属性，且需强制获取
                val type = if (isPortraitNeedRequestRemoteBrief && isBackgroudNeedRequestRemoteBrief) BriefRepo.PORTRAIT_AND_BACKGROUD_TYPE else if (isBackgroudNeedRequestRemoteBrief) BriefRepo.BACKGROUD_TYPE else BriefRepo.PORTRAIT_TYPE
                BriefManager.requestRemoteBrief(devId, For, type, true)
            }
            true
        } ?: let {
            val loadDefualtImage = { ivImg: ImageView?, defualtImg: Int ->
                ivImg?.let {
                    //必须使用glide加载默认，否则图片加载错位
                    it.setTag(null)
                    Glide.with(it)
                            .load(defualtImg)
                            .placeholder(defualtImg)
                            .error(defualtImg)
                            .centerCrop()
                            .into(it)
                }
            }
            loadDefualtImage(ivImage, defalutImage)
            loadDefualtImage(ivBackgroud, defalutBgImage)
            if (!TextUtils.isEmpty(devId) && !requestedRemoteOneTimeWhenNoCache.contains(devId)) {
                val type = when {
                    ivImage != null && ivBackgroud != null && tvContent != null -> {//所有类型
                        BriefRepo.ALL_TYPE
                    }
                    ivBackgroud != null && tvContent != null -> {//背景＋简介内容
                        BriefRepo.BACKGROUD_AND_BRIEF_TYPE
                    }
                    ivImage != null && tvContent != null -> {//头像＋简介内容
                        BriefRepo.PORTRAIT_AND_BRIEF_TYPE
                    }
                    ivBackgroud != null -> {//背景
                        BriefRepo.BACKGROUD_TYPE
                    }
                    ivImage != null -> {//头像
                        BriefRepo.PORTRAIT_TYPE
                    }
                    tvContent != null -> {//简介内容
                        BriefRepo.BRIEF_TYPE
                    }
                    else -> {
                        BriefRepo.ALL_TYPE
                    }
                }
                BriefManager.requestRemoteBrief(devId, For, type, false, Function {
                    if (it != null && it) {
                        //无简介缓存加载只请求一次
                        requestedRemoteOneTimeWhenNoCache.add(devId)
                    }
                    null
                })
            }
        }
    }

    /**
     * 加载头像
     */
    private fun loadImage(ivImage: ImageView?, path: String?, timeStamp: Long, defualtImg: Int, isLoadOneDeviceBrief: Boolean) {
        ivImage?.let {
            if (TextUtils.isEmpty(path)) {
                ivImage.setImageResource(defualtImg)
                ivImage.setTag(null)
            } else {
                //为节省存储占用，同一个设备path相同，所有根据路径无法区分,需要结合timeStamp
                val tag = "$path-$timeStamp"
                if (ivImage.getTag() != tag) {
                    Glide.with(ivImage)
                            .load(path)
                            .centerCrop()
                            .placeholder(defualtImg)
                            .error(defualtImg)
                            .signature(object : Key {
                                //tag区分Key
                                override fun updateDiskCacheKey(messageDigest: MessageDigest) {
                                    messageDigest.update(tag.toByteArray(Key.CHARSET))
                                }
                            })
                            .into(ivImage)
                    ivImage.setTag(tag)
                }
            }
        }
    }

    /**
     * 无数据库本地缓存时，记录进行过一次远程请求的设备
     * 主要用于列表无缓存只加载一次远程数据逻辑
     */
    private val requestedRemoteOneTimeWhenNoCache = hashSetOf<String>()
}