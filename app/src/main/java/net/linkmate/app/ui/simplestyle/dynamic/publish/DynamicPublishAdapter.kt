package net.linkmate.app.ui.simplestyle.dynamic.publish

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_dynamic_publish_simplestyle.view.*
import net.linkmate.app.R
import net.linkmate.app.util.GlideEngine
import net.linkmate.app.view.ViewHolder

/**发布动态 图片
 * @author Raleigh.Luo
 * date：20/12/21 13
 * describe：
 */
class DynamicPublishAdapter(val context: Context, val viewModel: DynamicPublishViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
                LayoutInflater.from(context).inflate(R.layout.item_dynamic_publish_simplestyle, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return viewModel.selectedPictures.value?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder.itemView) {
            val minPadding = context.resources.getDimensionPixelSize(R.dimen.common_half)
            val spannCount = 3
            val surplus = position % spannCount//0:左边 1:中间 2: 右边
            var paddingStart = surplus * minPadding
            var paddingEnd = (spannCount - surplus - 1) * minPadding
            val paddingBottom = minPadding * 3
            root.setPaddingRelative(paddingStart, 0, paddingEnd, paddingBottom)
            val photo = viewModel.selectedPictures.value?.get(position)
            GlideEngine.INSTANCE().loadPhoto(context,photo?.path,ivImage)
            ivDelete.setOnClickListener {//删除
                viewModel.removeSelectedPicture(position)
            }
            ivImage.setOnClickListener {
                //查看图片
                LocalPreviewActivity.start(context as Activity,viewModel.selectedPictures.value!!,position,viewModel.PICTURE_REQUEST_CODE)
            }
        }
    }
}