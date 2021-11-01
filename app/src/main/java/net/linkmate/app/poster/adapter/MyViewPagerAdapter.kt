package net.linkmate.app.poster.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.linkmate.app.poster.TabFragment


/**
 * Create by Admin on 2021-07-14-10:18
 */
class MyViewPagerAdapter(private val fragments: ArrayList<TabFragment>, fm: FragmentManager) :
  FragmentPagerAdapter(fm) {

  override fun getCount(): Int = fragments.size
  override fun getItem(position: Int): Fragment = fragments[position]

}