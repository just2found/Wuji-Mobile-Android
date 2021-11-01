package net.linkmate.app.poster.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_poster.*
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.poster.utils.BitmapUtils
import net.sdvn.nascommon.model.oneos.user.LoginSession


class ImageViewPagerAdapter(id: String, ip: String,session: String,imageUrls: List<String>, context: Activity): PagerAdapter() {

    private val mImageUrls: List<String> = imageUrls
    private val mContext: Activity = context
    private val ip: String = ip
    private val id: String = id
    private val session: String = session

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val photoView = mContext.layoutInflater.inflate(R.layout.item_stage_photo,null)
            .findViewById<ImageView>(R.id.imgStagePhoto)
        /*val url = "http://${ip}:9898/file/download?session=${session}&path=${mImageUrls[position]}"
        Glide.with(mContext)
                .load(url)
                .into(photoView)*/
        container.addView(photoView)
        BitmapUtils().loadingAndSaveImg(
                mImageUrls[position],
                photoView,
                "${MyApplication.getContext().filesDir?.path}/images/poster/${id}",
                session,ip, id, mContext,isThumbnail = false)
        return photoView
    }

    override fun getCount(): Int {
        return mImageUrls.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE;
    }
}