package net.linkmate.app.poster.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_product.view.*
import net.linkmate.app.R
import net.linkmate.app.poster.model.ProductResult


class ProductAdapter : BaseQuickAdapter<ProductResult, BaseViewHolder>(R.layout.item_product) {


    override fun convert(holder: BaseViewHolder, item: ProductResult) {

        holder.itemView.title.text = item.title
        holder.itemView.excerpt.text = item.post_excerpt
        holder.itemView.price.text = "￥ ${item.price}"
        val unit =
                when(item.expire_unit){
                    "1" -> "日"
                    "2" -> "月"
                    "3" -> "年"
                    else -> ""
                }
        holder.itemView.time.text = "有效期:${item.expire_num}${unit}"

    }

}