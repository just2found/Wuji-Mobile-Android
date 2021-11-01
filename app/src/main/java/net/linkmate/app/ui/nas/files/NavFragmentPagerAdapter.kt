package net.linkmate.app.ui.nas.files

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import libs.source.common.adapter.AbsNavFragmentPagerAdapter
import net.linkmate.app.ui.nas.helper.FilePlaceHolderFragment

class NavFragmentPagerAdapter(mFragmentManager: FragmentManager) : AbsNavFragmentPagerAdapter(mFragmentManager) {

    override fun getItem(tag: String): Fragment {
        return FilePlaceHolderFragment.newInstance(tag)
    }
}