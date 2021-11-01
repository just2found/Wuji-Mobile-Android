package net.linkmate.app.ui.nas.info.adpter

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.self_check.data.HdsInfo
import net.linkmate.app.view.ActivityItemLayout
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.CheckableImageButton
import timber.log.Timber
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Pattern

/**
create by: 86136
create time: 2021/4/22 11:20
Function description:
 */


//listLayoutID 列表展示时候的Id   //gridLayoutID九宫格展示时候的ID
class DiskTemperatureAdapter() : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_simple_list) {


    override fun convert(viewHolder: BaseViewHolder, p1: Int?) {
        viewHolder.setText(
            R.id.simple_title,
            " ${mContext.getString(R.string.disk)} ${viewHolder.adapterPosition+1} "
        )
        viewHolder.setText(
            R.id.simple_content,
            " $p1°C"
        )
    }


}