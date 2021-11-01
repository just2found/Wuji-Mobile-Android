package net.linkmate.app.poster.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_pup_list_layout.view.*
import net.linkmate.app.R
import net.linkmate.app.view.ViewHolder

/**
 * Create by Admin on 2021-07-15-9:55
 */
class PupRvAdapter(private val data:ArrayList<String>):RecyclerView.Adapter<ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val rootView =
      LayoutInflater.from(parent.context).inflate(R.layout.item_pup_list_layout, parent, false)
    return ViewHolder(rootView)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.itemView.tvType.text = data[position]
  }

  override fun getItemCount(): Int = data.size
}