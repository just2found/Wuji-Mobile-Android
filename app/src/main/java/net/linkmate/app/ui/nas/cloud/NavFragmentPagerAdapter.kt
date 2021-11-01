package net.linkmate.app.ui.nas.cloud

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import libs.source.common.adapter.AbsNavFragmentPagerAdapter

class NavFragmentPagerAdapter(mFragmentManager: FragmentManager) : AbsNavFragmentPagerAdapter(mFragmentManager) {

    override fun getItem(tag: String): Fragment {
        return VP2QuickCloudNavFragment.newInstance(tag)
    }
}