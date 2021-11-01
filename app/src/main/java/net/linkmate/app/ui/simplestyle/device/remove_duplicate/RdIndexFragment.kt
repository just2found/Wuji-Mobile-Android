package net.linkmate.app.ui.simplestyle.device.remove_duplicate

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_rd_index.*
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.sdvn.nascommon.BaseFragment


class RdIndexFragment : TipsBaseFragment() {

    private val viewModel by viewModels<RemoveDuplicateModel>({ requireParentFragment() })
    override fun getLayoutResId(): Int {
        return R.layout.fragment_rd_index
    }


    override fun getTopView(): View? {
        return title_bar
    }

    override fun initView(view: View) {

        title_bar.setBackListener {
            requireActivity().onBackPressed()
        }
        mTipsBar = tipsBar

        appoint_folder_btn.setOnClickListener {
            findNavController().navigate(R.id.action_index_to_byFile, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
        }

        all_folders_btn.setOnClickListener {
            viewModel.selectedFolderList.clear()
            viewModel.folderList.clear()
            findNavController().navigate(R.id.action_index_to_select, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
        }

    }


}