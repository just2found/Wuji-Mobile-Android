package net.linkmate.app.ui.activity.circle.circleDetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_circle_benefits.view.*
import kotlinx.android.synthetic.main.item_circle_benefits_header.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter

/**
 * @author Raleigh.Luo
 * date：20/8/18 15
 * describe：
 */
class CircleBenefitsAdapter(val context:Context): RecyclerView.Adapter<DialogBaseAdapter.ViewHolder>() {
    private val HEADER_TYPE=0
    private val DEFAULT_TYPE=1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogBaseAdapter.ViewHolder {
        val layout=if(viewType==HEADER_TYPE) R.layout.item_circle_benefits_header else R.layout.item_circle_benefits
        val view =
                LayoutInflater.from(context).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return DialogBaseAdapter.ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position==0||position==5)HEADER_TYPE else DEFAULT_TYPE
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: DialogBaseAdapter.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            HEADER_TYPE ->{
                with(holder.itemView){
                    tvMonth
                    tvUpstreamTraffic
                    tvRelatePoints
                    tvGetBenefit
                }

            }
            DEFAULT_TYPE ->{
                with(holder.itemView){
                    tvName
                    tvDate
                    tvUpstreamTrafficValue
                    tvRelatePointsValue
                    tvGetPointsValue
                }
            }
        }
    }

}