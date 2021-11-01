package net.linkmate.app.ui.simplestyle.device.disk

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.fragment_disk_load.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.linkmate.app.util.ToastUtils


class DiskLoadFragment : TipsBaseFragment() {

    private val viewModel by viewModels<DiskSpaceModel>({ requireParentFragment() })
    private val queryAdminStatus = 2021
    lateinit var handler: Handler
    private var retryTime = 3 //从试次数
    private var requestInterval = 2000L //从试次数


    override fun getLayoutResId(): Int {
        return R.layout.fragment_disk_load
    }

    override fun initView(view: View) {
        handler = Handler(Looper.getMainLooper()) { msg ->
            if (msg.what == queryAdminStatus) {
                getDiskManageStatus()
                true
            }
            false
        }
        Glide.with(view.context).asGif()
            .load(R.drawable.loading)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(load_view)
    }

    private fun getDiskManageStatus() {
        handler.removeMessages(queryAdminStatus)
        viewModel.getDiskManageStatus().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data!!) {
                        ToastUtils.showToast(getString(R.string.format_success))
                    } else {
                        ToastUtils.showToast(getString(R.string.format_fail))
                    }
                    requireActivity().setResult(SafeBoxModel.CLOSE_ACTIVITY)
                    requireActivity().finish()
                }
                Status.LOADING -> {
                    handler.sendEmptyMessageDelayed(queryAdminStatus, requestInterval)
                }
                else -> {
                    if (retryTime > 0) {
                        retryTime--
                        handler.sendEmptyMessageDelayed(queryAdminStatus, requestInterval)
                    } else {
                        it.message?.let {
                            ToastUtils.showToast(it)
                        }
                        requireActivity().setResult(SafeBoxModel.CLOSE_ACTIVITY)
                        requireActivity().finish()
                    }
                }
            }
        })
    }


    override fun onStart() {
        super.onStart()
        handler.sendEmptyMessageDelayed(queryAdminStatus, requestInterval)
    }


    override fun onStop() {
        super.onStop()
        handler.removeMessages(queryAdminStatus)
    }

}