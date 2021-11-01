package net.linkmate.app.ui.simplestyle.device.remove_duplicate

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_rd_by_file.*

import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdByFileAdapter

//add_target
class RdByFileFragment : TipsBaseFragment() {

    private val viewModel by viewModels<RemoveDuplicateModel>({ requireParentFragment() })

    override fun getLayoutResId(): Int {
        return R.layout.fragment_rd_by_file
    }

    override fun getTopView(): View? {
        return title_bar
    }

    override fun initView(view: View) {
        title_bar.setBackListener {
            findNavController().navigateUp()
        }

        mTipsBar = tipsBar
        title_bar.addRightTextButton(getString(R.string.add_target)) {
            findNavController().navigate(R.id.action_by_to_add, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
        }

        add_folder_btn.setOnClickListener {
            findNavController().navigate(R.id.action_by_to_add, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
        }

        start_scan_btn.setOnClickListener {
            findNavController().navigate(R.id.action_by_to_select, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
        }
    }


    override fun onResume() {
        super.onResume()
        if (viewModel.selectedFolderList.isNullOrEmpty()) {
            recycle_view.visibility = View.GONE
            start_scan_btn.visibility = View.GONE
            center_bg_img.visibility = View.VISIBLE
            add_folder_btn.visibility = View.VISIBLE
        } else {
            recycle_view.visibility = View.VISIBLE
            start_scan_btn.visibility = View.VISIBLE
            center_bg_img.visibility = View.GONE
            add_folder_btn.visibility = View.GONE
            val adapter = RdByFileAdapter()
            adapter.setNewData(viewModel.selectedFolderList)
            recycle_view.layoutManager = LinearLayoutManager(requireActivity())
            recycle_view.adapter = adapter
        }
    }

}