package net.linkmate.app.ui.activity.dev.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils

/** 

Created by admin on 2020/11/6,17:39

 */
class MediaSourcesAdapter : BaseItemDraggableAdapter<OneOSFile, BaseViewHolder>(null) {
    private lateinit var session: LoginSession

    fun refreshSession(session: LoginSession) {
        this.session = session
    }

    override fun convert(holder: BaseViewHolder, item: OneOSFile) {
        val imageView = holder.getView<ImageView>(R.id.rv_grid_iv_icon)
        showPicturePreview(session, imageView, item)
    }

    private fun showPicturePreview(mLoginSession: LoginSession, imageView: ImageView, file: OneOSFile) {
        val load = if (FileUtils.isGifFile(file.getName())) {
            val model = OneOSAPIs.genDownloadUrl(mLoginSession, file.getAllPath())
            Glide.with(imageView)
                    .asGif()
                    .load(model)
        } else {
            val url = OneOSAPIs.genThumbnailUrl(mLoginSession, file)
            Glide.with(imageView)
                    .asBitmap()
                    .centerCrop()
                    .load(EliCacheGlideUrl(url))
        }
        load.into(imageView)
    }
}