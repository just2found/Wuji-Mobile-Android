package net.linkmate.app.ui.simplestyle.device.download_offline

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.fragment_download_offline_retrieval.*
import libs.source.common.livedata.Status
import libs.source.common.utils.RateLimiter
import net.linkmate.app.R
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.ui.simplestyle.device.download_offline.adapter.OdRetrievalAdapter
import net.linkmate.app.util.DialogUtil
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.ProgressDialog
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.DialogUtils
import java.util.concurrent.TimeUnit


class DownloadOfflineRetrievalFragment : BaseFragment() {

    private val viewModel by viewModels<DownloadOfflineModel>({ requireParentFragment() })
    private val mRateLimiter = RateLimiter<Any>(1500, TimeUnit.MILLISECONDS) //点击加间隔
    private var mPage = 0
    private val mNum = 30

    private val mLoadingDialogFragment by lazy {
        ProgressDialog()
    }

    private val mOdRetrievalAdapter by lazy {
        val it = OdRetrievalAdapter()

        val emptyView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_empty_directory, null)
        val txtEmptyTv = emptyView.findViewById<TextView>(R.id.txt_empty_list)
        txtEmptyTv.text = getString(R.string.no_torrent_file_found)
        it.emptyView = emptyView
        it.setOnItemClickListener { baseQuickAdapter, view, postion ->
            val item = baseQuickAdapter.getItem(postion) ?: return@setOnItemClickListener
            if (mRateLimiter.shouldFetch(item)) {
                if (item is OneOSFile) {
                    showConfirmDialog(getString(R.string.add_task_confirm)) {
                        addTask(item)
                    }
                }
            }
        }
        it
    }

    private fun addTask(item: OneOSFile) {
        devId?.let { devId ->
            mLoadingDialogFragment.show(childFragmentManager, ProgressDialog::javaClass.name)
            viewModel.addTask(devId, item).observe(this, Observer { resource ->
                if (resource.status == Status.SUCCESS) {
                    resource.data?.let { code ->
                        if (code == 0)//添加任务成功了
                        {
                            ToastUtils.showToast(getString(R.string.bind_success))
                            findNavController().navigate(
                                R.id.action_retrieval_to_index, DownloadOfflineRetrievalFragmentArgs(devId!!).toBundle(),
                                null,
                                null
                            )
                        } else//添加任务失败了
                        {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(code))
                        }
                    }
                } else {
                    ToastUtils.showToast(resource.code?.let { it2 ->
                        V5HttpErrorNo.getResourcesId(
                            it2
                        )
                    })
                }
                mLoadingDialogFragment.dismiss()
            })
        }
    }


    private fun showConfirmDialog(contentStr: String, Next: () -> Unit) {
        DialogUtils.showConfirmDialog(requireContext(),
            "",
            contentStr,
            getString(R.string.ok),
            getString(R.string.cancel),
            DialogUtils.OnDialogClickListener { dialog, isPositive ->
                if (isPositive) {
                    Next.invoke()
                }
            })
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_download_offline_retrieval
    }

    override fun getTopView(): View? {
        return toolbar_layout
    }

    override fun initView(view: View) {
        mLoadingDialogFragment.show(childFragmentManager, ProgressDialog::javaClass.name)
        mOdRetrievalAdapter.setOnLoadMoreListener({
            loadData()
        }, recycle_view)
        loadData()
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun loadData() {
        devId?.let { devid ->
            viewModel.findBtFile(devid, page = mPage, num = mNum)
                .observe(this, Observer { resoucre ->
                    if (resoucre.status == Status.SUCCESS) {
                        if (resoucre.data != null && resoucre.data!!.size > 0) {
                            if (mPage == 0) {
                                mOdRetrievalAdapter.setNewData(resoucre.data!!)
                                recycle_view.adapter = mOdRetrievalAdapter
                            } else {
                                mOdRetrievalAdapter.addData(resoucre.data!!)
                            }
                            if (resoucre.data!!.size < mNum) {
                                mOdRetrievalAdapter.setEnableLoadMore(false)
                            } else {
                                mPage++
                                mOdRetrievalAdapter.setEnableLoadMore(true)
                            }
                            mOdRetrievalAdapter.loadMoreComplete()
                        } else {
                            recycle_view.adapter = mOdRetrievalAdapter
                            mOdRetrievalAdapter.setEnableLoadMore(false)
                        }
                        mLoadingDialogFragment.dismiss()
                    } else if(resoucre.status == Status.ERROR)  {
                        mOdRetrievalAdapter.loadMoreComplete()
                    }
                })
        }
    }


}