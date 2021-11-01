package net.linkmate.app.ui.nas

import android.app.Dialog
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rxjava.rxlife.RxLife
import com.yanzhenjie.permission.runtime.Permission
import io.weline.repo.SessionCache
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_files_base.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.fragment.BackPressedFragment
import net.linkmate.app.ui.nas.cloud.findNav
import net.linkmate.app.ui.nas.helper.DevicePreViewModel
import net.linkmate.app.ui.nas.helper.FilesCommonHelper
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.ui.nas.helper.UpdateFileTypeHelper
import net.linkmate.app.ui.nas.helper.UpdateFileTypeHelper.addItem
import net.linkmate.app.ui.nas.helper.UpdateFileTypeHelper.offlineDownloadItem
import net.linkmate.app.ui.nas.images.FileManageViewModel
import net.linkmate.app.ui.nas.search.SearchFragmentArgs
import net.linkmate.app.ui.nas.transfer.TransferActivity
import net.linkmate.app.ui.nas.upload.UploadActivity
import net.linkmate.app.ui.viewmodel.TorrentsViewModel
import net.linkmate.app.ui.viewmodel.TransferCountViewModel
import net.linkmate.app.view.TipsBar
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.MangerBarInterface
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.UiUtils.isHans
import net.sdvn.nascommon.model.UiUtils.isHant
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.PermissionChecker
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel
import net.sdvn.nascommon.widget.TypePopupView
import org.view.libwidget.anim.BezierViewHolder
import org.view.libwidget.badgeview.QBadgeView
import org.view.libwidget.singleClick

/**
 *
 * @Description: 文件base
 * @Author: todo2088
 * @CreateDate: 2021/2/4 13:47
 */
abstract class FilesBaseFragment : BackPressedFragment() {
    protected val devicePreViewModel by viewModels<DevicePreViewModel>({ requireActivity() })
    protected val deviceViewModel by viewModels<DeviceViewModel>({ requireParentFragment() })
    protected val fileManageViewModel by viewModels<FileManageViewModel>({ requireParentFragment() })
    private val transferCountViewModel by viewModels<TransferCountViewModel>({ requireActivity() })
    protected val model by viewModels<TorrentsViewModel>({ requireActivity() })
    private var transferCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transferCountViewModel.getLiveDataIncompleteCount(getDevId()).observe(this, Observer {
            transferCount = it
            refreshTrans()
        })
    }

    //右上角角标工具
    private var mQBadgeView: QBadgeView? = null
    private fun refreshTrans() {
        val total = transferCount //+ messagesCount + sharesCount
        if (total > 0) {
            if (mQBadgeView == null) {
                mQBadgeView = QBadgeView(context).apply {
                    badgeGravity = Gravity.END or Gravity.TOP
                    bindTarget(ibtn_nav_title_right2)
                }
            }
            mQBadgeView?.badgeNumber = total
        } else {
            if (mQBadgeView != null) mQBadgeView!!.hide(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val frameLayout = view?.findViewById<FrameLayout>(R.id.frame_layout_place_holder)
        inflater.inflate(getSubLayoutId(), frameLayout, true)
        return view
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_files_base
    }

    override fun getTopView(): View? {
        return layout_title
    }

    override fun getHomeTipsBar(): TipsBar? {
        return tipsBar
    }

    abstract fun getSubLayoutId(): Int

    override fun initView(view: View, savedInstanceState: Bundle?) {
        initStatusBarPadding(layout_select_top_panel)
        initStatusBarPadding(layout_search_panel)
        initSubView(view, savedInstanceState)
        btn_trans.singleClick {
            onBackPressed()
        }
        btn_sort?.singleClick {
            onBackPressed()
        }
        ibtn_nav_title_right2?.singleClick {
            val clazz = TransferActivity::class.java
            startActivity(Intent(it.context, clazz).apply {
                putExtra(AppConstants.SP_FIELD_DEVICE_ID, getDevId())
            })
        }
        btn_sort?.setCompoundDrawables(null, null, null, null)

        ibtn_nav_title_right.singleClick {
            showSearchActivity(it)
        }

        val mUploadPopView = TypePopupView(requireActivity(), mUploadTypeList, R.string.please_select_file_type_upload)
        mUploadPopView.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            if (FilesCommonHelper.checkNetworkStatus()) return@OnItemClickListener
            val item = mUploadTypeList.get(position)
            when (item.flag) {
                LocalFileType.NEW_FOLDER -> {
                    getCurrentPath()?.let {
                        fileManageViewModel.showNewFolder(requireActivity(), getDevId(), it, getFileType(), view)
                    }
                    mUploadPopView.dismiss()
                    return@OnItemClickListener
                }
                LocalFileType.OFFLINE_DOWNLOAD -> {
                    getDevId().let {
                        SessionManager.getInstance().getLoginSession(it, object : GetSessionListener() {
                            override fun onSuccess(url: String?, loginSession: LoginSession) {
                                val url = OneOSAPIs.PREFIX_HTTP + loginSession.ip
                                val intent = Intent(activity, WebViewActivity::class.java)
                                var region = when {
                                    isHans() -> "zh-CN"
                                    isHant() -> "zh-TW"
                                    else -> "en"
                                }

                                intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_URL, "$url/transmission/web?lang=$region")
                                intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_TITLE, "Transmssion")
                                intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT, false)

                                startActivity(intent)
                                mUploadPopView.dismiss()
                            }
                        })
                    }
                    return@OnItemClickListener
                }
                else -> {
                    if (item.flag is LocalFileType) {
                        val curPath = getCurrentPath()
                        val finalFileType = item.flag as LocalFileType
                        val context = view.context

                        PermissionChecker.checkPermission(view.context, Callback {
                            val intent = Intent(context, UploadActivity::class.java)
                            intent.putExtra("fileType", finalFileType)
                            intent.putExtra("path", FilesCommonHelper.getUploadPath(getDevId(), context, curPath, getFileType()))
//                            if (SessionCache.instance.isV5(getDevId())) {
////                                intent.putExtra("pathType", getPathType()[0])
//                            }
                            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, getDevId())
                            startActivity(intent)
                            mUploadPopView.dismiss()
                        }, Callback {
                            UiUtils.showStorageSettings(requireContext())
                        }, Permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }


        })

        fab.singleClick { view ->
            val floatingActionButton = view as FloatingActionButton
            if (mUploadPopView.isShow == true) {
                floatingActionButton.show()
                mUploadPopView.dismiss()
            } else {
                floatingActionButton.show()
                if (!FilesCommonHelper.checkNetworkStatus()) {
                    checkUploadTypeAvailable()
                    mUploadPopView.showPopupTop2(layout_title)
                    floatingActionButton.hide()
                    mUploadPopView.setOnDismissListener { floatingActionButton.show() }
                }
            }
        }
        fab.isVisible = SessionManager.getInstance().getDeviceModel(getDevId())?.isEnableUseSpace?:false
    }

    open fun showSearchActivity(view: View) {
        val toBundle = SearchFragmentArgs(getDevId(), getFileType(), getPathType(), getCurrentPath()).toBundle()
        findNavController().navigate(R.id.action_global_searchFragment, toBundle)
    }

    open fun getPathType(): IntArray {
        return intArrayOf(SharePathType.PUBLIC.type)
    }

    open fun getFileType(): OneOSFileType {
        return OneOSFileType.ALL
    }

    open fun getCurrentPath(): String? {
        return null
    }

    private var notifyDialog: Dialog? = null
    open fun onCommonError(errNo: Int, errMsg: String? = null) {
        if (errNo == V5_ERR_DENIED_PERMISSION) {
            lifecycleScope.launchWhenResumed {
                if (notifyDialog == null || notifyDialog?.isShowing == false) {
                    notifyDialog = DialogUtils.showNotifyDialog(requireContext(), 0,
                            R.string.ec_no_permission, R.string.confirm) { _, isPositiveBtn ->
                        if (isPositiveBtn) {
                            val findNav = findNav(this@FilesBaseFragment)
                            findNav?.navigate(R.id.action_global_selectTypeFragment, SelectTypeFragmentArgs(getDevId()).toBundle())
                        }
                    }
                }
            }
        }
    }

    private fun checkUploadTypeAvailable() {
        mUploadTypeList.remove(addItem)
        if (getCurrentPath() == null) {
            mUploadTypeList.remove(addItem)
        } else {
            mUploadTypeList.add(addItem)
        }
        mUploadTypeList.remove(offlineDownloadItem)
        val deviceModel = SessionManager.getInstance().getDeviceModel(getDevId())
        if (deviceModel != null && !UiUtils.isAndroidTV(deviceModel.devClass)
                && !SessionCache.instance.isNasV3(deviceModel.devId)) {
            mUploadTypeList.add(offlineDownloadItem)
        }
    }

    //文件类型集
    private val mUploadTypeList = UpdateFileTypeHelper.getFileTypes().toMutableList()


    abstract fun getDevId(): String

    override fun onResume() {
        super.onResume()
        devicePreViewModel.getToolItems()
        refreshDevNameById(getDevId())
        refreshTrans()
    }

    protected open fun refreshDevNameById(mDevId: String?) {
        mDevId?.let {
            deviceViewModel.refreshDevNameById(it)
                    .`as`(RxLife.`as`(this))
                    .subscribe {
                        btn_sort?.text = it
                    }
        }

    }

    abstract fun initSubView(view: View, savedInstanceState: Bundle?)

    override fun onBackPressed(): Boolean {
        if (layout_search_panel != null && layout_search_panel!!.isVisible) {
            layout_search_panel!!.cancel()
            return true
        }
        return super.onBackPressed()
    }

    protected fun showSelectBar(isShown: Boolean) {
        if (isShown) {
            layout_select_top_panel?.showPanel(true)
        } else {
            layout_select_top_panel?.hidePanel(true)
        }
    }

    protected fun showManageBar(isShown: Boolean) {
        if (parentFragment != null && parentFragment is MangerBarInterface<*, *>) {
            (parentFragment as MangerBarInterface<*, *>).showManageBar(isShown)
        }
        if (isShown) {
            layout_file_manage_panel?.showPanel()
        } else {
            layout_file_manage_panel?.hidePanel()
        }
    }

    protected fun updateSelectBar(totalCount: Int, selectedCount: Int, mListener: FileSelectPanel.OnFileSelectListener) {
        layout_select_top_panel?.setOnSelectListener(mListener)
        layout_select_top_panel?.updateCount(totalCount, selectedCount)
    }

    protected fun updateManageBar(fileType: OneOSFileType, selectedList: List<DataFile>, loginSession: LoginSession, mListener: FileManagePanel.OnFileManageListener<DataFile>,isGroup: Boolean=false) {
        layout_file_manage_panel?.setOnOperateListener(mListener)
        layout_file_manage_panel?.updatePanelItems(fileType, selectedList, loginSession,isGroup)
    }

    protected fun actionDownloadAnimation(view: View, count: Int) {
        //开始view
        val outLocation = IntArray(2)
        view.getLocationInWindow(outLocation)
        val startPoint = Point(outLocation[0], outLocation[1])
        //结束view
        val outLocation2 = IntArray(2)
        ibtn_nav_title_right2.getLocationInWindow(outLocation2)
        val endPoint = Point(outLocation2[0], outLocation2[1])

        val bezierViewHolder = BezierViewHolder(view.context).apply {
            setStartPosition(startPoint)
            setEndPosition(endPoint)
            files_root.addView(this)
            text = count.toString()
        }
        bezierViewHolder.startBezierAnimation()
    }
}