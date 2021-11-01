package net.linkmate.app.ui.simplestyle.device.remove_duplicate

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_rd_add_file.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdAddFileAdapter
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.model.oneos.OneOSFile

//cancel
class RdAddFileFragment : TipsBaseFragment() {

    private val viewModel by viewModels<RemoveDuplicateModel>({ requireParentFragment() })
    private var adapter: RdAddFileAdapter? = null
    override fun getLayoutResId(): Int {
        return R.layout.fragment_rd_add_file
    }

    override fun getTopView(): View? {
        return title_bar
    }

    override fun initView(view: View) {
        if (viewModel.folderList.isNullOrEmpty()) {
            viewModel.getFolderList(devId!!).observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    val data = it.data
                    if (data?.isSuccess == true) {
                        if (data.data.files != null) {
                            viewModel.folderList.clear()
                            viewModel.folderList.addAll(filterFolder(data.data.files))
                            initAdapter()
                        }
                    } else {
                        it.message?.let {
                            ToastUtils.showToast(it)
                        }
                    }
                }
            })
        } else {
            initAdapter()
        }

        title_bar.setBackListener {
            findNavController().navigateUp()
        }

        mTipsBar = tipsBar
        title_bar.addRightTextButton(getString(R.string.cancel)) {
            findNavController().navigateUp()
        }
        confirm_selection_btn.setOnClickListener {
            adapter?.let {
                viewModel.selectedFolderList.clear()
                viewModel.selectedFolderList.addAll(it.mSelectList)
            }
            requireActivity().onBackPressed()
        }

    }

    private fun filterFolder(data: MutableList<OneOSFile>): MutableList<OneOSFile> {
        val list = mutableListOf<OneOSFile>();
        for (datum in data) {
            if (datum.isDirectory()) {
                list.add(datum)
            }
        }
        return list
    }


    private fun initAdapter() {
        adapter = RdAddFileAdapter()
        adapter!!.setOnItemClickListener { baseQuickAdapter, view, position ->
            if(baseQuickAdapter is RdAddFileAdapter)
                baseQuickAdapter. changeSelect(position)
        }
        adapter?.setNewData(viewModel.folderList)
        adapter?.addSelectList(viewModel.selectedFolderList)
        recycle_view.layoutManager = LinearLayoutManager(requireActivity())
        recycle_view.adapter = adapter
    }
}