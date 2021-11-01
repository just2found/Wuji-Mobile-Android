package net.linkmate.app.ui.simplestyle.device.download_offline

import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_download_offline_add.*
import libs.source.common.livedata.Status
import libs.source.common.utils.RateLimiter
import net.linkmate.app.R
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.ProgressDialog
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.model.oneos.OneOSFileType
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


class DownloadOfflineAddFragment : BaseFragment() {
    private val viewModel by viewModels<DownloadOfflineModel>({ requireParentFragment() })
    override fun getLayoutResId(): Int {
        return R.layout.fragment_download_offline_add
    }

    override fun getTopView(): View? {
        return toolbar_layout
    }

    private val seedType = 1
    private val httpType = 2

    private val mRateLimiter = RateLimiter<Any>(1500, TimeUnit.MILLISECONDS)
    private val mLoadingDialogFragment by lazy {
        ProgressDialog()
    }
    private var nowType = seedType
    private val mSelectColor by lazy {
        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
    }

    private val mUnSelectColor by lazy {
        ContextCompat.getColor(requireContext(), R.color.txt_item_content)
    }

    override fun initView(view: View) {
        seed_type_tv.setOnClickListener {
            if (nowType != seedType) {
                seed_type_tv.setTextColor(mSelectColor)
                center_bg_img.visibility = View.VISIBLE
                retrieval_seed_btn.visibility = View.VISIBLE
                find_seed_btn.visibility = View.VISIBLE
                left_v.visibility = View.VISIBLE

                right_v.visibility = View.GONE
                http_type_tv.setTextColor(mUnSelectColor)
                http_address_edt.visibility = View.GONE
                add_http_task_btn.visibility = View.GONE
                nowType = seedType
            }
        }
        http_type_tv.setOnClickListener {
            if (nowType != httpType) {
                seed_type_tv.setTextColor(mUnSelectColor)
                center_bg_img.visibility = View.GONE
                retrieval_seed_btn.visibility = View.GONE
                find_seed_btn.visibility = View.GONE
                left_v.visibility = View.GONE

                right_v.visibility = View.VISIBLE
                http_type_tv.setTextColor(mSelectColor)
                http_address_edt.visibility = View.VISIBLE
                add_http_task_btn.visibility = View.VISIBLE
                nowType = httpType
            }
        }

        if (nowType == seedType) {
            seed_type_tv.setTextColor(mSelectColor)
            center_bg_img.visibility = View.VISIBLE
            retrieval_seed_btn.visibility = View.VISIBLE
            find_seed_btn.visibility = View.VISIBLE
            left_v.visibility = View.VISIBLE

            right_v.visibility = View.GONE
            http_type_tv.setTextColor(mUnSelectColor)
            http_address_edt.visibility = View.GONE
            add_http_task_btn.visibility = View.GONE
        } else if (nowType == httpType) {
            seed_type_tv.setTextColor(mUnSelectColor)
            center_bg_img.visibility = View.GONE
            retrieval_seed_btn.visibility = View.GONE
            find_seed_btn.visibility = View.GONE
            left_v.visibility = View.GONE
            right_v.visibility = View.VISIBLE
            http_type_tv.setTextColor(mSelectColor)
            http_address_edt.visibility = View.VISIBLE
            add_http_task_btn.visibility = View.VISIBLE
        }

        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        add_http_task_btn.setOnClickListener {
            if (mRateLimiter.shouldFetch(it)) {
                val httpUrl = http_address_edt.text.toString().trim()
                if (TextUtils.isEmpty(httpUrl) || (!isUrl(httpUrl) && !isMagnet(httpUrl))) {
                    ToastUtils.showToast(getString(R.string.input_url))
                } else {
                    hasSetSavePath {
                        devId?.let { it1 ->
                            mLoadingDialogFragment.show(childFragmentManager, ProgressDialog::javaClass.name)
                            viewModel.addTask1(it1, httpUrl).observe(this, Observer { resource ->
                                if (resource.status == Status.SUCCESS) {
                                    resource.data?.let { code ->
                                        if (code == 0)//添加任务成功了
                                        {
                                            ToastUtils.showToast(getString(R.string.bind_success))
                                            findNavController().navigateUp()
                                        } else//添加任务失败了
                                        {
                                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(code))
                                        }
                                    }
                                } else if (resource.status == Status.ERROR) {
                                    ToastUtils.showToast(resource.code?.let { it2 -> V5HttpErrorNo.getResourcesId(it2) })
                                }
                                mLoadingDialogFragment.dismiss()
                            })
                        }
                    }
                }
            }
        }
        set_path.setOnClickListener {
            showSavePathDialog()
        }

        retrieval_seed_btn.setOnClickListener {
            hasSetSavePath {
                findNavController().navigate(R.id.action_add_to_retrieval, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
            }
        }
        find_seed_btn.setOnClickListener {
            hasSetSavePath {
                findNavController().navigate(R.id.action_add_to_find, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
            }
        }
    }


    private fun hasSetSavePath(Next: () -> Unit) {
        if (!viewModel.isSetSavePath()) {
            ToastUtils.showToast(R.string.set_default_path)
            showSavePathDialog()
        } else {
            Next.invoke()
        }
    }


    private fun isUrl(url: String): Boolean {
        val rex = "(https?|ftp|file|http)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"
        val pattern: Pattern = Pattern.compile(rex)
        val matcher: Matcher = pattern.matcher(url)
        return matcher.matches()
    }

    private fun isMagnet(url: String): Boolean {
        return url.contains("magnet:?xt=urn:btih:")
    }

    private fun showSavePathDialog() {
        val path = if (viewModel.saveSharePathType == OneOSFileType.PRIVATE.ordinal) {
            getString(R.string.root_dir_name_private) + viewModel.savePath
        } else if (viewModel.saveSharePathType == SharePathType.PUBLIC.type) {
            getString(R.string.root_dir_name_public) + viewModel.savePath
        } else {
            viewModel.savePath
        }
        val savePathDialogFragment = SavePathDialogFragment(path, View.OnClickListener { findNavController().navigate(R.id.action_add_to_set, SelectTypeFragmentArgs(devId!!).toBundle(), null, null) })
        savePathDialogFragment.show(childFragmentManager, SavePathDialogFragment::javaClass.name)
    }
}