package net.linkmate.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskNode

/**
create by: 86136
create time: 2021/1/21 9:44
Function description:
 */

class DiskNodeAdapter(val list: List<DiskNode>, val context: Context) : RecyclerView.Adapter<DiskNodeAdapter.DiskNodeViewHold>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskNodeViewHold {
        val rootView = LayoutInflater.from(context).inflate(R.layout.item_disk_node_layout, parent, false);
        return DiskNodeViewHold(rootView)
    }

    override fun onBindViewHolder(holder: DiskNodeViewHold, position: Int) {
        val diskNode = list[position]
        val pre=context.getString(R.string.disk_name_pre)
        holder.nodeNameTv.text = "$pre${diskNode.slot}"
        holder.nodeSizeTv.text = diskNode.size
        holder.nodeStatusTv.text = diskNode.status
        when (diskNode.main) {
            1 -> {
                holder.nodeStatusTv.text = context.getString(R.string.disk_in_use)
                holder.nodeCl.setBackgroundResource(R.drawable.bg_disk_node_wait)
                holder.nodeNameTv.setTextColor(ContextCompat.getColor(context, R.color.color_0C81FB))
                holder.nodeStatusTv.setTextColor(ContextCompat.getColor(context, R.color.color_0C81FB))
                holder.nodeSizeTv.setTextColor(ContextCompat.getColor(context, R.color.color_0C81FB))
                holder.nodeColorV.setBackgroundResource(R.color.color_0C81FB)
            }
            0 -> {
                holder.nodeStatusTv.text = context.getString(R.string.disk_configured)
                holder.nodeCl.setBackgroundResource(R.drawable.bg_disk_node_pass)
                holder.nodeNameTv.setTextColor(ContextCompat.getColor(context, R.color.text333))
                holder.nodeStatusTv.setTextColor(ContextCompat.getColor(context, R.color.text333))
                holder.nodeSizeTv.setTextColor(ContextCompat.getColor(context, R.color.text_gray))
                holder.nodeColorV.setBackgroundResource(R.color.color_7DB9FA)
            }
            else -> {
                holder.nodeStatusTv.text = ""
                holder.nodeCl.setBackgroundResource(R.drawable.bg_disk_node_unknown)
                holder.nodeNameTv.setTextColor(ContextCompat.getColor(context, R.color.text_gray))
                holder.nodeStatusTv.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.nodeColorV.setBackgroundResource(R.color.gray)
            }

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class DiskNodeViewHold(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nodeCl = itemView.findViewById<View>(R.id.node_cl)
        val nodeColorV = itemView.findViewById<View>(R.id.node_color_view)
        val nodeNameTv = itemView.findViewById<TextView>(R.id.node_name_tv)
        val nodeSizeTv = itemView.findViewById<TextView>(R.id.node_size_tv)
        val nodeStatusTv = itemView.findViewById<TextView>(R.id.node_status_tv)
    }


}