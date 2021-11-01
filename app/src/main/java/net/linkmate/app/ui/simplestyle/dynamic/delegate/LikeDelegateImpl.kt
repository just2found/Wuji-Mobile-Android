package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.view.View
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.view.CusTextView
import net.sdvn.common.vo.DynamicLike

/**
 * @author Raleigh.Luo
 * date：20/12/28 13
 * describe：
 */
class LikeDelegateImpl(val viewModel: DynamicBaseViewModel, private val tvLike: CusTextView) : LikeDelegate<List<DynamicLike>>() {
    /**
     * 点赞数量显示真实数量，点赞标记优先本地记录
     */
    override fun show(dynamicId: Long?, likes: List<DynamicLike>?) {
        if (dynamicId == null || dynamicId == -1L) {//未发布的动态不显示点赞按钮
            tvLike.visibility = View.GONE
            return
        }

        tvLike.visibility = View.VISIBLE
        /**
         * 获取网络数据真实点赞的数量
         * id必须不为－1，－1表示为本地数据
         */
        val realyLikeCount = likes?.filter {
            it.id != -1L
        }?.size ?: 0

        tvLike.setText(realyLikeCount.toString())
        val matchLike = likes?.filter {
            //点过赞，且本地没有被移除（即没有进行过取消点赞操作）
            it.uid == DynamicQueue.mLastUserId && it.isDeleted == false
        }
        matchLike?.let {//点过赞
            tvLike.setStartDrawable(MyApplication.getContext().getDrawable(if (matchLike.size > 0) R.drawable.icon_like else R.drawable.icon_unlike))
        } ?: let {//没点过赞
            tvLike.setStartDrawable(MyApplication.getContext().getDrawable(R.drawable.icon_unlike))
        }
    }

    override fun setDefaultListener(context: Context, dynamicId: Long?, dynamicAutoIncreaseId: Long?, likes: List<DynamicLike>?) {
        tvLike.setOnClickListener {
            dynamicId?.let {
                val matchLike = likes?.filter {
                    it.uid == DynamicQueue.mLastUserId && it.isDeleted == false
                }
                matchLike?.let {
                    if (it.size > 0) {//点过赞
                        viewModel.startUnLikeDynamic(dynamicId, matchLike.get(0).autoIncreaseId)
                    } else {
                        viewModel.startLikeDynamic(dynamicId, dynamicAutoIncreaseId ?: 0)
                    }
                } ?: let {//没点过赞
                    viewModel.startLikeDynamic(dynamicId, dynamicAutoIncreaseId ?: 0)
                }
            }
        }
    }

}