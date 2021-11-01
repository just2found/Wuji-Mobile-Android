package net.linkmate.app.poster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.R

/**
 * Create by Admin on 2021-07-13-18:31
 */
class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_poster, parent, false))
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    holder.itemView.findViewById<TextView>(R.id.tv).text = "test"
  }

  override fun getItemCount(): Int = 300


  class MyHolder(item: View) : RecyclerView.ViewHolder(item) {

  }
}