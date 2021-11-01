package net.linkmate.app.ui.nas.group

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_group_announcement_detail.*
import kotlinx.android.synthetic.main.fragment_group_o_s_announcement_list.tipsBar
import kotlinx.android.synthetic.main.fragment_group_o_s_announcement_list.title_bar
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objboxkt.GroupNotice
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [GroupAnnouncementDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupAnnouncementDetailFragment : NavTipsBackPressedFragment() {

    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val navArgs by navArgs<GroupAnnouncementDetailFragmentArgs>()

    override fun initView(view: View) {
        initTitle()
        refreshData(navArgs.data)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_announcement_detail
    }

    override fun getTopView(): View? {
        return title_bar
    }

    private fun initTitle() {
        title_bar.setBackListener {
            findNavController().popBackStack()
        }
        mTipsBar = tipsBar
    }

    private fun refreshData(item: GroupNotice?) {
        if (item != null) {
            group_announcement_content_tv.text = item.notice
            group_announcement_time_tv.text =
                AppConstants.sdf.format(Date(item.postTime * 1000))
            group_announcement_owner.text = item.postUsername
        }
    }

}