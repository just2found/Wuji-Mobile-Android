package net.linkmate.app.ui.nas.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.R

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/8
 */
class HeaderAdapter : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
   private var headerInfo: String? = null

    fun updateHeader(info:String?){
        headerInfo = info
        notifyDataSetChanged()
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(headerInfo: String?) {
            itemView.findViewById<TextView>(R.id.tvTitle).text = headerInfo
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_title, parent, false)
        return HeaderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(headerInfo)
    }

}