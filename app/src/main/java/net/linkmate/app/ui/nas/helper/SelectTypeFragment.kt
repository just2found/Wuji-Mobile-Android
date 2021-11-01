package net.linkmate.app.ui.nas.helper

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.SectionEntity
import com.google.android.material.appbar.AppBarLayout
import com.rxjava.rxlife.RxLife
import io.cabriole.decorator.ColumnProvider
import io.cabriole.decorator.GridDividerDecoration
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.SessionCache
import kotlinx.android.synthetic.main.fragment_select_type.*
import kotlinx.android.synthetic.main.fragment_select_type.ab_iv_left
import kotlinx.android.synthetic.main.fragment_select_type.ab_iv_right
import kotlinx.android.synthetic.main.fragment_select_type.ab_tv_title
import kotlinx.android.synthetic.main.fragment_select_type.app_bar
import kotlinx.android.synthetic.main.fragment_select_type.clHeaderPanel
import kotlinx.android.synthetic.main.fragment_select_type.ivIcon
import kotlinx.android.synthetic.main.fragment_select_type.ivTitleBg
import kotlinx.android.synthetic.main.fragment_select_type.ivTitleTransparentBg
import kotlinx.android.synthetic.main.fragment_select_type.recycle_view_file_type0
import kotlinx.android.synthetic.main.fragment_select_type.tipsBar
import kotlinx.android.synthetic.main.fragment_select_type.toolbar
import kotlinx.android.synthetic.main.fragment_select_type.toolbar_layout
import kotlinx.android.synthetic.main.fragment_select_type.tvName
import kotlinx.android.synthetic.main.fragment_select_type.tvTitle
import kotlinx.android.synthetic.main.fragment_select_type_lenovo.*
import libs.source.common.exts.dpToPx
import libs.source.common.utils.RateLimiter
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.manager.DevManager
import net.linkmate.app.poster.PosterActivity
import net.linkmate.app.ui.activity.dev.DevBriefActivity
import net.linkmate.app.ui.activity.message.SystemMessageActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DevicelDetailActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.cloud.SelectToPathFragmentArgs
import net.linkmate.app.ui.nas.cloud.VP2QuickCloudNavFragmentArgs
import net.linkmate.app.ui.nas.devhelper.SelectDeviceFragmentArgs
import net.linkmate.app.ui.nas.dnla.DNLAActivity
import net.linkmate.app.ui.nas.favorites.FavoritesFragmentArgs
import net.linkmate.app.ui.nas.group.GroupOSIndexFragmentArgs
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_details
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_device_LAN_access
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_device_information
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_download_offline
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_duplicate_removal
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_file_dlna
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_file_favorites
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_file_samba
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_file_share
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_receive_score
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_safe_deposit_box
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_self_check
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_sys_msg
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_torrents_transfer
import net.linkmate.app.ui.nas.helper.DevicePreViewModel.Companion.type_transfer
import net.linkmate.app.ui.nas.images.ImagesFragmentArgs
import net.linkmate.app.ui.nas.info.NavigationContainerActivity
import net.linkmate.app.ui.nas.safe_box.SafeBoxEntrance
import net.linkmate.app.ui.nas.samba.SAMBAActivity
import net.linkmate.app.ui.nas.share.ShareActivity
import net.linkmate.app.ui.nas.torrent.TorrentActivity
import net.linkmate.app.ui.nas.transfer.TransferActivity
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.ui.simplestyle.device.download_offline.DownloadOfflineActivity
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.RemoveDuplicateActivity
import net.linkmate.app.ui.simplestyle.device.self_check.DiskSelfCheckActivity
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.ui.viewmodel.SystemMessageViewModel
import net.linkmate.app.ui.viewmodel.TorrentsViewModel
import net.linkmate.app.ui.viewmodel.TransferCountViewModel
import net.linkmate.app.util.CheckStatus
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.util.business.ReceiveScoreUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.iface.ILoadingCallback
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.FileTypeItem
import net.sdvn.nascommon.model.UiUtils.isHans
import net.sdvn.nascommon.model.UiUtils.isHant
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.viewmodel.FilesViewModel
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import net.sdvn.nascommon.widget.SELF
import net.sdvn.nascommon.widget.badgeview.QBadgeView
import org.view.libwidget.OnItemClickListener
import org.view.libwidget.log.L
import org.view.libwidget.singleClick
import java.util.concurrent.TimeUnit

/** 

Created by admin on 2020/7/31,16:29
文件服务首页
 */

class SelectTypeFragment : TipsBaseFragment() {

    private lateinit var toolsPagerAdapter: ToolsPagerAdapter
    private var device: DeviceBean? = null
    private lateinit var fileTypeItemAdapter: FileTypeItemAdapter
    private lateinit var fileTypeItemAdapter1: FileTypeItemAdapter
    private lateinit var fileTypeItemAdapter2: FileTypeItemAdapter
    private lateinit var fileTypeItemAdapterTool: FileTypeItemAdapter
    private lateinit var fileTypeItemAdapter0: FileTypeItemAdapterL

    private val viewModel by viewModels<DevicePreViewModel>({ requireParentFragment() })
    private val deviceViewModel by viewModels<DeviceViewModel>({ requireParentFragment() })
    private val commonViewModel by viewModels<DevCommonViewModel>({ requireParentFragment() })
    private val filesViewModel by viewModels<FilesViewModel>({ requireParentFragment() })
    private val messageViewModel by viewModels<SystemMessageViewModel>({ requireActivity() })
    private val transferCountViewModel by viewModels<TransferCountViewModel>({ requireActivity() })
    private val shareViewModel2 by viewModels<ShareViewModel2>({ requireActivity() })
    private val torrentsViewModel by viewModels<TorrentsViewModel>({ requireActivity() })
    private val viewModelBriefCache: BriefCacheViewModel by viewModels({ requireActivity() })
    private var messagesCount = 0
    private var sharesCount = 0
    private var transferCount = 0
    private var isToNextPage = false
    private var isComePoster = false
    private var isComeCircle = false
    private var isToPublic = false

    override fun getLayoutResId(): Int {
        return R.layout.fragment_select_type_lenovo
    }

    override fun getTopView(): View? {
        return toolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isComePoster = it.getBoolean("isComePoster",false)
            isComeCircle = it.getBoolean("isComeCircle",false)
        }
        messageViewModel.messageCountLiveData.observe(this, androidx.lifecycle.Observer {
            messagesCount = it
            notifyItemChanged(type_sys_msg, it, fileTypeItemAdapter2)
        })
        transferCountViewModel.getLiveDataIncompleteCount(devId)
                .observe(this, androidx.lifecycle.Observer {
                    transferCount = it
                    notifyItemChanged(type_transfer, it, fileTypeItemAdapter1)
                    refreshTrans()
                })
        shareViewModel2.shareElementV2sInComplete.observe(
                this,
                androidx.lifecycle.Observer<MutableList<ShareElementV2>?> { t ->
                    sharesCount = t?.size ?: 0
                    notifyItemChanged(type_file_share, sharesCount, fileTypeItemAdapter1)
                })
        devId?.let {
            //刷新简介
            deviceViewModel.startGetDeviceBrief(it)
            BriefManager.requestRemoteBrief(it, BriefRepo.FOR_DEVICE, BriefRepo.ALL_TYPE)
        }
        deviceViewModel.deviceBrief.observe(this, Observer {
            //监听简介
            var brief: BriefModel? = null
            if (it != null && it.size > 0) {
                brief = it.get(0)
            }
            devId?.let {
                viewModelBriefCache.loadBrief(it, brief,
                        tvContent = tvName,
                        ivImage = ivIcon,
                        defalutImage = if (device != null) DeviceBean.getIconSimple(device!!) else R.drawable.icon_device_wz,
                        ivBackgroud = ivTitleBg,
                        defalutBgImage = R.color.breif_bg_defualt_color,
                        isLoadOneDeviceBrief = true
                )
            }
        })
    }

    private var mQBadgeView: QBadgeView? = null
    private fun refreshTrans() {
        val total = transferCount //+ messagesCount + sharesCount
        if (total > 0) {
            if (mQBadgeView == null) {
                mQBadgeView = QBadgeView(context).apply {
                    badgeGravity = Gravity.END or Gravity.TOP
                    bindTarget(ab_iv_right2)
                }
            }
            mQBadgeView?.badgeNumber = total
        } else {
            if (mQBadgeView != null) {
                mQBadgeView!!.hide(false)
            }
        }
    }

    private fun notifyItemChanged(type: String, count: Int, adapter: FileTypeItemAdapter) {
        for ((index, sectionEntity) in adapter.data.withIndex()) {
            val item = sectionEntity.t
            if (item?.flag == type) {
                item.ext2 = count
                adapter.notifyItemChanged(index)
                break
            }
        }
    }

    private val onItemClickListener: BaseQuickAdapter.OnItemClickListener =
            BaseQuickAdapter.OnItemClickListener { baseQuickAdapter, view, i ->
                if (Utils.isFastClick(view)) return@OnItemClickListener
                if (FilesCommonHelper.checkNetworkStatus()) {
                    return@OnItemClickListener
                }
                val sectionEntity = baseQuickAdapter.getItem(i) as? SectionEntity<*>
                        ?: return@OnItemClickListener
                val t = sectionEntity.t ?: return@OnItemClickListener
                when (t) {
                    is FileTypeItem -> {
                        dispartcherEvent(t, view)
                    }
                }
            }

    private fun dispartcherEvent(t: FileTypeItem, view: View) {
        when (val type = t.flag) {
            is OneOSFileType -> {
                onFileTypeEvent(type)
            }
            type_details -> {
                jumpToDevDetail()

            }
            type_file_share -> {
                val intent = Intent(view.context, ShareActivity::class.java)
                //                        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
                startActivity(intent)
            }
            type_torrents_transfer -> {
                TorrentActivity.startActivityWithId(requireContext(), devId,null, false)
            }
            type_transfer -> {
                startTransfer(view)
            }
            type_receive_score -> {
                t.ext2?.let {
                    //                            startScoreActivity(view.context, it as String)
                    ReceiveScoreUtil.showReceiveScoreDialog(
                            context,
                            devId,
                            null
                    )
                }
            }
            type_sys_msg -> {
                startActivity(Intent(context, SystemMessageActivity::class.java))
            }
            type_device_LAN_access -> {
                val args = LANAccessSettingFragmentArgs(devId!!).toBundle()
                val findNavController = findNavController()
                findNavController.navigate(
                        R.id.global_action_to_LANAccessSettingFragment,
                        args
                )
            }

            type_download_offline -> {
                if (viewModel.hasToolsServer(type_download_offline)) {
                    L.i("进入方法", "type_download_offline", "SelectTypeFragment", "nwq", "2021/3/27");
                    context?.startActivity(
                            Intent(context, DownloadOfflineActivity::class.java)
                                    .putExtra(
                                            io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                            devId
                                    ) //是否是EN服务器
                    )
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }

            type_duplicate_removal -> {
                if (viewModel.hasToolsServer(type_duplicate_removal)) {
                    L.i("进入方法", "type_duplicate_removal", "SelectTypeFragment", "nwq", "2021/3/27");
                    context?.startActivity(
                            Intent(context, RemoveDuplicateActivity::class.java)
                                    .putExtra(
                                            io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                            devId
                                    ) //是否是EN服务器
                    )
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }
            type_self_check -> {
                if (viewModel.hasToolsServer(type_self_check)) {
                    L.i("进入方法", "type_self_check", "SelectTypeFragment", "nwq", "2021/3/27");
                    context?.startActivity(
                            Intent(context, DiskSelfCheckActivity::class.java)
                                    .putExtra(
                                            io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                            devId
                                    ) //是否是EN服务器
                    )
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }
            type_safe_deposit_box -> {
//                    SafeBoxEntrance(devId!!).show(childFragmentManager, SafeBoxEntrance::javaClass.name)
                if (viewModel.hasToolsServer(type_safe_deposit_box) && SessionManager.getInstance()
                                .getDeviceModel(devId)?.isEnableUseSpace == true
                ) {
                    L.i("进入方法", "type_safe_deposit_box", "SelectTypeFragment", "nwq", "2021/3/27");
                    SafeBoxEntrance(devId!!).show(
                            childFragmentManager,
                            SafeBoxEntrance::javaClass.name
                    )
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }
            type_file_favorites -> {
                val favoriteId =
                        SessionManager.getInstance().getLoginSession(devId)?.userInfo?.favoriteId
                if (viewModel.hasToolsServer(type) && favoriteId != 0) {
                    val args = FavoritesFragmentArgs(
                            devId!!,
                            favoriteId!!,
                            OneOSFileType.FAVORITES
                    ).toBundle()
                    val findNavController = findNavController()
                    if (findNavController.currentDestination?.id == R.id.selectTypeFragment) {
                        findNavController.navigate(R.id.action_global_favoritesFragment, args)
                    }
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }
            type_file_samba -> {
                if (viewModel.hasToolsServer(type)) {
                    if (CMAPI.getInstance().isConnected) {
                        startActivity(Intent(requireContext(), SAMBAActivity::class.java)
                                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, devId)
                                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_IP, device?.priIp)
                                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_IS_ADMIN, (device?.isOwner
                                        ?: false) || (device?.isAdmin ?: false)))
                    } else {
                        ToastUtils.showToast(R.string.network_not_available)
                    }
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }

            type_file_dlna -> {
                if (viewModel.hasToolsServer(type)) {
                    if (CMAPI.getInstance().isConnected) {
                        startActivity(Intent(requireContext(), DNLAActivity::class.java)
                                //是否有外部存储功能
                                .putExtra("isSupportExternalStorage", viewModel.hasToolsServer(OneOSFileType.EXTERNAL_STORAGE))
                                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, devId)
                                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_IS_ADMIN, (device?.isOwner
                                        ?: false) || (device?.isAdmin ?: false)))
                    } else {
                        ToastUtils.showToast(R.string.network_not_available)
                    }
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }

            type_device_information -> {
                if (viewModel.hasToolsServer(type)) {
                    NavigationContainerActivity.startContainerActivity(R.navigation.device_information_nav, devId!!, requireContext())
                } else {
                    ToastHelper.showLongToast(R.string.coming_soon)
                }
            }
        }
    }

    private fun startTransfer(view: View) {
        val intent = Intent(view.context, TransferActivity::class.java)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
        startActivity(intent)
    }

    private fun toPosterActivity(){
        val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
        val intent = Intent(context, PosterActivity::class.java)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
        intent.putExtra("isComeCircle", isComeCircle)
        intent.putExtra("domain", deviceModel?.device?.domain)
        startActivityForResult(intent,ACCESS_POSTER_CODE)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private val ACCESS_POSTER_CODE = 0x003
    private fun onFileTypeEvent(type: Any) {
        //                        if (devId?.let { viewModel.noPerm(it, type) } == true) {
        //                            ToastHelper.showLongToast(R.string.ec_no_permission)
        //                            return@OnItemClickListener
        //                        }
        if (type == OneOSFileType.VIDEO) {
            toPosterActivity()
            return
        }
        if (type == OneOSFileType.PICTURE && SessionCache.instance.isNasV3(devId!!)) {
            val args = ImagesFragmentArgs(devId!!).toBundle()
            val findNavController = findNavController()
            findNavController.navigate(R.id.global_action_to_imagesFragment, args)
            return
        }
        val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
//        if (BuildConfig.DEBUG) {
//            if (type == OneOSFileType.GROUP) {
//                val args = GroupOSIndexFragmentArgs(devId!!).toBundle()
//                val findNavController = findNavController()
//                if (findNavController.currentDestination?.id == R.id.selectTypeFragment) {
//                    findNavController.navigate(R.id.action_global_group_index, args)
//                }
//                return
//            }
//        }
        if ((!SessionCache.instance.isV5(devId!!) && DevTypeHelper.isAndroidTV(
                        deviceModel?.devClass
                                ?: 0
                ) && type != OneOSFileType.PRIVATE)
                || ((type == OneOSFileType.GROUP
                        || type == OneOSFileType.EXTERNAL_STORAGE) && !viewModel.hasToolsServer(type))
        ) {
            ToastHelper.showLongToast(R.string.coming_soon)
            return
        }
        if (type == OneOSFileType.GROUP) {
            val args = GroupOSIndexFragmentArgs(devId!!).toBundle()
            val findNavController = findNavController()
            findNavController.navigate(R.id.action_global_group_index, args)
            return
        }
        val spFieldFileType = type as OneOSFileType
        val deviceDisplayModel = filesViewModel.getDeviceDisplayModel(devId!!)
        if (deviceDisplayModel != null) {
            deviceDisplayModel.isChangedType = deviceDisplayModel.mFileType != spFieldFileType
        }
        val args = VP2QuickCloudNavFragmentArgs(devId!!, spFieldFileType, null).toBundle()
        val findNavController = findNavController()
        findNavController.navigate(R.id.global_action_to_VP2, args)
    }

    override fun onResume() {
        super.onResume()
        findDeviceBean()?.let { device ->
            if (tvTitle.getTag() == null) tvTitle.text = device.name
            deviceViewModel.refreshDevNameById(device.id)
                    .`as`(RxLife.`as`(this))
                    .subscribe({ s: String? ->
                        if (!s.isNullOrEmpty()) {
                            if (tvTitle.getTag() != s) tvTitle.text = s
                        }
                        tvTitle.setTag(tvTitle.text.toString())
                    }) { _: Throwable? ->
                        tvTitle.text = device.name
                        tvTitle.setTag(tvTitle.text.toString())
                    }
        }

        val preList = viewModel.getDirs(devId)
        fileTypeItemAdapter.setNewData(preList)
        val preList0 = viewModel.getMedia(devId)
        fileTypeItemAdapter0.setNewData(preList0)
        toolsPagerAdapter.setData(viewModel.getPagerItems(devId!!))
        indicatorHelper?.updateCount(toolsPagerAdapter.count)
        val preList1 = viewModel.getTransmissionItems(devId)
        fileTypeItemAdapter1.setNewData(preList1)

        val preList2 = viewModel.getMoreItems(devId)
        fileTypeItemAdapter2.setNewData(preList2)

        viewModel.liveData.observe(this, Observer {
            toolsPagerAdapter.setData(viewModel.getPagerItems(devId!!))
            indicatorHelper?.updateCount(toolsPagerAdapter.count)
            refreshSpaceView()
//            if (it.isNullOrEmpty()) {
//                card_view_tool.visibility = View.VISIBLE
//            } else {
//                if (card_view_tool.visibility == View.VISIBLE && fileTypeItemAdapterTool.data.size == it.size) {
//                    for ((i, datum) in toolsPagerAdapter.data.withIndex()) {
//                        if (it[i] != datum) {
//                            toolsPagerAdapter.setData(viewModel.getPagerItems(devId!!))
//                            return@Observer
//                        }
//                    }
//
//                } else {
//                    card_view_tool.visibility = View.VISIBLE
//                    //fileTypeItemAdapterTool.setNewData(it)
//                }
//                toolsPagerAdapter.setData(viewModel.getPagerItems(devId!!))
//               // fileTypeItemAdapterTool.setNewData(it)
//
//                card_view_tool.visibility = View.VISIBLE
//            }
        })


        devId?.let { deviceId ->
            viewModel.getToolItems(deviceId)
        }

        refreshData(fileTypeItemAdapter1)
        refreshData(fileTypeItemAdapter2)
    }

    private fun refreshData(adapter: FileTypeItemAdapter): Boolean {
        var isChanged = false
        adapter.data.forEach { sectionEntity ->
            val item = sectionEntity.t
            if (item != null) {
                when (item.flag) {
                    type_sys_msg -> {
                        if (item.ext2 != messagesCount) {
                            item.ext2 = messagesCount
                            isChanged = true
                        }
                    }
                    type_file_share -> {
                        if (item.ext2 != sharesCount) {
                            item.ext2 = sharesCount
                            isChanged = true
                        }
                    }
                    type_transfer -> {
                        if (item.ext2 != transferCount) {
                            item.ext2 = transferCount
                            isChanged = true
                        }
                    }
                }
            }
        }
        if (isChanged) {
            adapter.notifyDataSetChanged()
        }
        return isChanged
    }

    private val ACCESS_DETAIL_CODE = 0x002
    private val itemClickRateLimit = RateLimiter<Any>(1, TimeUnit.SECONDS)
    override fun initView(view: View) {
        tvName.setText(R.string.no_summary)
        mTipsBar = tipsBar
        findDeviceBean()
        device?.let {
            ivIcon.setImageResource(DeviceBean.getIconSimple(it))
        }
        val activity = requireActivity()
        ab_iv_left.singleClick {
            activity.finish()
        }

        ab_iv_right.singleClick {
            if (!itemClickRateLimit.shouldFetch(it.id)) {
                return@singleClick
            }
            //简介详情
            jumpToDevDetail()
        }
        ivIcon.singleClick {
            if (findDeviceBean()?.isOwner == true) {
                showBottomDialog(intArrayOf(R.string.change_avatar))
            } else {
                jumpToDevIntr()
            }
        }
        ivTitleBg.singleClick {
            if (findDeviceBean()?.isOwner == true) {
                showBottomDialog(intArrayOf(R.string.change_cover))
            } else {
                jumpToDevIntr()
            }
        }
        toolbar.singleClick {
            if (findDeviceBean()?.isOwner == true) {
                showBottomDialog(intArrayOf(R.string.change_cover))
            } else {
                jumpToDevIntr()
            }
        }
        tvName.singleClick {//查看简介
            if (findDeviceBean()?.isOwner == true) {
                showBottomDialog(intArrayOf(R.string.edit_summary))
            } else {
                jumpToDevIntr()
            }
        }
        val bgParam = ivTitleBg.layoutParams
        val screenWidth = Dp2PxUtils.getScreenWidth(requireContext())
        val height = (screenWidth * MyConstants.COVER_W_H_PERCENT + 0.5f).toInt();
        bgParam.height = height
        ivTitleBg.layoutParams = bgParam

        val titleParam = ivTitleTransparentBg.layoutParams
        val statueBarHeight = UIUtils.getStatueBarHeight(requireActivity())
        titleParam.height = screenWidth * 2 / 10 + statueBarHeight
        ivTitleTransparentBg.layoutParams = titleParam

        val layoutParams = toolbar.layoutParams
        layoutParams.height = layoutParams.height + statueBarHeight
        toolbar.layoutParams = layoutParams

        toolbar_layout.minimumHeight = toolbar_layout.minimumHeight + statueBarHeight

        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset -> //verticalOffset始终为0以下的负数
            //verticalOffset始终为0以下的负数
            val percent = Math.abs(verticalOffset * 1.0f) / appBarLayout.totalScrollRange
            val limit = 0.8f
            when {
                percent > limit -> {
                    if (clHeaderPanel?.isVisible == true)
                        clHeaderPanel.visibility = View.GONE
                    toolbar?.setBackgroundResource(R.drawable.bg_title_bar_gradient)
                    ivTitleTransparentBg?.visibility = View.GONE
                    ab_tv_title?.text = tvTitle.text
                    ab_tv_title?.visibility = View.VISIBLE
                    toolbar?.alpha = (percent - limit) / (1 - limit)
                    ab_iv_left.imageTintList =
                            ColorStateList.valueOf(resources.getColor(R.color.title_icon_color));
                    ab_iv_right.imageTintList =
                            ColorStateList.valueOf(resources.getColor(R.color.title_icon_color));
                    ab_iv_right2.imageTintList =
                            ColorStateList.valueOf(resources.getColor(R.color.title_icon_color));
                }
                percent <= limit -> {
                    if (clHeaderPanel?.isVisible == false) {
                        clHeaderPanel.visibility = View.VISIBLE
                    }
                    clHeaderPanel?.alpha = 1f - percent
                    ab_iv_left?.alpha = 1f
                    ab_iv_right?.alpha = 1f
                    toolbar?.alpha = 1f
                    toolbar?.setBackgroundColor(Color.TRANSPARENT)
                    ivTitleTransparentBg?.visibility = View.VISIBLE
                    ab_tv_title?.visibility = View.INVISIBLE
                    ab_iv_left.imageTintList =
                            ColorStateList.valueOf(resources.getColor(R.color.white));
                    ab_iv_right.imageTintList =
                            ColorStateList.valueOf(resources.getColor(R.color.white));
                    ab_iv_right2.imageTintList =
                            ColorStateList.valueOf(resources.getColor(R.color.white));
                }
            }
        })

//        itb_iv_left.let {
//            it.setImageResource(R.drawable.icon_return)
//            it.isVisible = true
//            it.setOnClickListener {
//                activity.onBackPressed()
//            }
//        }
//        itb_tv_title.isVisible = true
//        itb_tv_title.gravity = Gravity.CENTER
        val isChinese = isHans() || isHant()
        val context = view.context
        val spanCount = Dp2PxUtils.getScreenWidth(context) / Dp2PxUtils.dp2px(
                context, if (isChinese) {
            90
        } else {
            120
        }
        )
        val margin1dp = Dp2PxUtils.dp2px(context, 1)
        //file type
        val gridDivider = GridDividerDecoration.create(
                color = ContextCompat.getColor(context, R.color.line_gray),
                size = resources.dpToPx(1),
                columnProvider = object : ColumnProvider {
                    override fun getNumberOfColumns(): Int = 4
                },
                widthMargin = resources.dpToPx(0),
                heightMargin = resources.dpToPx(0),
                orientation = RecyclerView.VERTICAL
        )
//        val gridLayoutManager = GridLayoutManager(context, 4)
//        recycle_view_file_type.layoutManager = gridLayoutManager
        fileTypeItemAdapter = FileTypeItemAdapter()
//        fileTypeItemAdapter.bindToRecyclerView(recycle_view_file_type)
//        val margin = Dp2PxUtils.dp2px(context, 1)
//        recycle_view_file_type.addItemDecoration(GridSpanMarginDecoration(margin, gridLayoutManager))
//        recycle_view_file_type.addItemDecoration(gridDivider)
//        recycle_view_file_type.addItemDecoration(DividerItemDecoration(context, GridLayoutManager.VERTICAL).apply {
//            setDrawable(context.getDrawable(R.drawable.line_gray))
//        })
//        recycle_view_file_type.addItemDecoration(DividerItemDecoration(context, GridLayoutManager.HORIZONTAL).apply {
//            setDrawable(context.getDrawable(R.drawable.line_gray))
//        })
//        val preList = viewModel.getDirs(devId)
//        fileTypeItemAdapter.setNewData(preList)
//        fileTypeItemAdapter.onItemClickListener = onItemClickListener

        //media
        val gridLayoutManager0 = GridLayoutManager(context, 4)
        recycle_view_file_type0.layoutManager = gridLayoutManager0
        fileTypeItemAdapter0 = FileTypeItemAdapterL()
        fileTypeItemAdapter0.bindToRecyclerView(recycle_view_file_type0)
        val preList0 = viewModel.getMedia(devId)
        fileTypeItemAdapter0.setNewData(preList0)
        fileTypeItemAdapter0.onItemClickListener = onItemClickListener
        // transfer
//        val gridLayoutManager1 = GridLayoutManager(context, 4)
//        recycle_view_file_type1.layoutManager = gridLayoutManager1
        fileTypeItemAdapter1 = FileTypeItemAdapter()
//        fileTypeItemAdapter1.bindToRecyclerView(recycle_view_file_type1)
//        recycle_view_file_type1.addItemDecoration(GridSpanMarginDecoration(margin, gridLayoutManager1))
//        recycle_view_file_type1.addItemDecoration(DividerItemDecoration(context, GridLayoutManager.HORIZONTAL).apply {
//            setDrawable(context.getDrawable(R.drawable.line_gray))
//        })

//        val gridLayoutManagerT = GridLayoutManager(context, 4)
//        recycle_view_file_tool.layoutManager = gridLayoutManagerT
        fileTypeItemAdapterTool = FileTypeItemAdapter()
//        fileTypeItemAdapterTool.onItemClickListener = this@SelectTypeFragmentLenovo.onItemClickListener
//        fileTypeItemAdapterTool.bindToRecyclerView(recycle_view_file_tool)
//
//
//        val preList1 = viewModel.getTransmissionItems(devId)
//        fileTypeItemAdapter1.setNewData(preList1)
//        fileTypeItemAdapter1.onItemClickListener = onItemClickListener
//
//
        devId?.let { deviceId ->
            viewModel.getToolItems(deviceId)
        }

        //more
//        val gridLayoutManager2 = GridLayoutManager(context, 4)
//        recycle_view_file_type2.layoutManager = gridLayoutManager2
        fileTypeItemAdapter2 = FileTypeItemAdapter()
//        fileTypeItemAdapter2.bindToRecyclerView(recycle_view_file_type2)
////        recycle_view_file_type2.addItemDecoration(DividerItemDecoration(context, GridLayoutManager.HORIZONTAL).apply {
////            setDrawable(context.getDrawable(R.drawable.line_gray))
////        })
//        val preList2 = viewModel.getMoreItems(devId)
//        fileTypeItemAdapter2.setNewData(preList2)
//        fileTypeItemAdapter2.onItemClickListener = onItemClickListener

        card_view_private.singleClick {
            onFileTypeEvent(OneOSFileType.PRIVATE)
        }
        card_view_public.singleClick {
            onFileTypeEvent(OneOSFileType.PUBLIC)
        }
        card_view_group.singleClick {
            onFileTypeEvent(OneOSFileType.GROUP)
        }

        ab_iv_right2.singleClick {
            startTransfer(it)
        }

        toolsPagerAdapter = ToolsPagerAdapter()
//        toolsPagerAdapter.setData(viewModel.getPagerItems(devId!!))
        toolsPagerAdapter.setOnItemClickListener(object : OnItemClickListener<FileTypeItem> {
            override fun OnItemClick(data: FileTypeItem, position: Int, view: View) {
                dispartcherEvent(data, view)
            }
        })
        indicatorHelper = IndicatorHelper(
                requireContext().resources.getColor(R.color.primary),
                requireContext().resources.getColor(R.color.line_gray),
                toolsPagerAdapter.count,
                view_page_file_tool
        )

        view_page_file_tool.adapter = toolsPagerAdapter
        view_page_file_tool.currentItem = 0
        view_page_file_tool.offscreenPageLimit = 1

        refreshSpaceView()

        if(isComePoster){
            /*val args = VP2QuickCloudNavFragmentArgs(devId!!, OneOSFileType.PUBLIC, null).toBundle()
            val findNavController = findNavController()
            findNavController.navigate(R.id.global_action_to_VP2, args)*/
            if(isComeCircle){
                isToNextPage = true
                val path = arguments?.getString("path","")
                val pathS = path?.split("/")
                var name: String? = null
                if(pathS != null && pathS.isNotEmpty()){
                    name = pathS[(pathS.size-2)]
                }
                TorrentActivity.startActivityWithId(requireContext(), devId,name, false)
            } else{
                if(isToNextPage) return
                isToNextPage = true
                download()
            }

        }
        else if(findDeviceBean()?.isOwner != true){
            if(isToNextPage){
                requireActivity().finish()
                return
            }
            if(isComeCircle){
                toPosterActivity()
            }
            else if(isToPublic){
                requireActivity().finish()
            }
            else{
                isToPublic = true
                val spFieldFileType = OneOSFileType.PUBLIC
                val args = VP2QuickCloudNavFragmentArgs(devId!!, spFieldFileType, null).toBundle()
                findNavController().navigate(R.id.global_action_to_VP2, args)
            }
        }
        else{
            layout_fff.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        if(isComePoster && isComeCircle && isToNextPage){
            requireActivity().finish()
        }
    }

    private fun download(){
        val selectedList: ArrayList<OneOSFile> = arrayListOf()
        val path = arguments?.getString("path","")
        val pathS = path?.split("/")
        val file = OneOSFile()
        file.share_path_type = 2
        file.setPath(path)
        if(pathS != null && pathS.isNotEmpty()){
            val name = pathS[(pathS.size-1)]
            file.setName(name)
        }
        file.setTime(System.currentTimeMillis() / 1000)
        selectedList.add(file)

        val requestKey = "select_device_DOWNLOAD_$devId"
        findNavController().navigate(
                R.id.global_action_to_selectDeviceFragment,
                SelectDeviceFragmentArgs(
                        requestKey,
                        SelectDeviceFragmentArgs.Companion.FilterType.FILE_SHARE,
                        selectedList.find { it.isDirectory() } == null,
                        arrayListOf(devId!!)
                ).toBundle()
        )
        setFragmentResultListener(
                requestKey = requestKey,
                listener = { requestKeyResult, bundle ->
                    if (requestKey == requestKeyResult) {
                        val mToDevId =
                                bundle.getString(AppConstants.SP_FIELD_DEVICE_ID)
                        if(mToDevId.isNullOrEmpty()){
                            requireActivity().finish()
                            return@setFragmentResultListener
                        }
                        if (mToDevId == SELF) {
                            val service = SessionManager.getInstance().service
                            service?.addDownloadTasks(selectedList, devId, null)
                            requireActivity().finish()
                            /*normalManage(
                                    action,
                                    view,
                                    selectedList,
                                    fileManage,
                                    mFileType1,
                                    false
                            )*/
                            return@setFragmentResultListener
                        }
                        var rootPathType =
                                NasFileConstant.CONTAIN_USER or NasFileConstant.CONTAIN_PUBLIC

                        clearFragmentResult(requestKeyResult)
                        SessionManager.getInstance().getLoginSession(devId)
                        val loadingCallback: ILoadingCallback? =
                                requireActivity().takeIf { it is ILoadingCallback } as ILoadingCallback?
                        val fileManage = OneOSFileManage(requireActivity(),
                                loadingCallback,
                                SessionManager.getInstance().getLoginSession(devId)!!,
                                null,null)
                        selectPathToAction(
                                FileManageAction.DOWNLOAD,
                                mToDevId,
                                rootPathType,
                                selectedList
                                , fileManage
                        )
                    }
                })
    }

    private fun selectPathToAction(
            action: FileManageAction,
            mToDevId: String,
            rootPathType: Int,
            selectedList: List<OneOSFile>
            , fileManage: OneOSFileManage
    ) {
        val requestKeySelectPath = "select_path_${action.name}"
        val selectToPathFragmentArgs =
                SelectToPathFragmentArgs(
                        mToDevId!!,
                        rootPathType,
                        action,
                        requestKeySelectPath
                )
        findNavController().navigate(
                R.id.global_action_to_selectToPathFragment,
                selectToPathFragmentArgs.toBundle()
        )
        setFragmentResultListener(
                requestKey = requestKeySelectPath,
                listener = { requestKeyResult, bundle ->
                    if (requestKeyResult == requestKeySelectPath) {
                        var devID = if (mToDevId != devId) {
                            mToDevId
                        } else null
                        val path = bundle.getString("path")
                        if (path.isNullOrEmpty()){
                            requireActivity().finish()
                            return@setFragmentResultListener
                        }
                        val sharePathType = bundle.getInt("sharePathType")
                        fileManage.manage(action, devID, sharePathType, path, selectedList,OneOSFileType.PUBLIC)
                        requireActivity().finish()
//                        showSelectAndOperatePanel(false)
                    }
                })
    }

    private fun refreshSpaceView() {
        val isAndroidTV = FilesCommonHelper.isAndroidTV(devId!!)
        val isAndroidTVAndNasV1 = FilesCommonHelper.isAndroidTVAndNasV1(devId!!)
        val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
        val enableUseSpace = deviceModel?.isEnableUseSpace
                ?: false
        card_view_group.isVisible = enableUseSpace && !isAndroidTV && viewModel.hasToolsServer(OneOSFileType.GROUP)
        val isPrivateVisible =
                enableUseSpace && (isAndroidTVAndNasV1 || !isAndroidTV || (isAndroidTV && deviceModel?.isOwner ?: false))
        card_view_private.isVisible = isPrivateVisible
        val margins = if (isPrivateVisible) Dp2PxUtils.dp2px(requireContext(), 12) else 0
        card_view_group.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = margins
        }
        card_view_public.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = margins
        }
    }

    var indicatorHelper: IndicatorHelper? = null

    private fun showBottomDialog(intArrayOf: IntArray) {
        commonViewModel.showBottomDialog(requireActivity(), devId!!, intArrayOf, { intent, i ->
            startActivityForResult(intent, i)
        })
    }

    private fun jumpToDevIntr() {
        findDeviceBean()?.let {
            val dev = it
            //1.检查设备是否在线
            if (!it.isOnline) {//设备离线
                ToastUtils.showToast(R.string.device_offline)
            } else {//设备在线
                //2.检查设备状态是否正常
                CheckStatus.checkDeviceStatus(requireActivity(), parentFragmentManager,
                        dev, androidx.arch.core.util.Function {// true ：状态回调 状态正常，按原逻辑走，false ：状态异常
                    if (it) {//状态正常
                        var owner = dev.hardData?.nickname
                        if (TextUtils.isEmpty(owner)) owner = dev.owner
                        dev.getId()?.let {
                            DevBriefActivity.start(
                                    requireContext(),
                                    it,
                                    dev.name,
                                    owner ?: "",
                                    dev.isOwner(),
                                    dev.devClass
                            )
                        }
                    }
                    null
                })
            }
        }
    }

    private fun findDeviceBean(): DeviceBean? {
        return DevManager.getInstance().deviceBeans.find {
            it.id == devId
        }.also { device = it }
    }

    private fun jumpToDevDetail() {
        //简介详情
        val intent = Intent(requireContext(), DevicelDetailActivity::class.java).apply {
            putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
        }
        DevicelDetailActivity.startActivityForResult(requireActivity(), intent, ACCESS_DETAIL_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACCESS_DETAIL_CODE && resultCode == FunctionHelper.DEVICE_REMOVE) {//移除设备，关闭页面
            requireActivity().finish()
        }
        else if (requestCode == ACCESS_POSTER_CODE){
            if (resultCode == Activity.RESULT_OK){
//                val deviceDisplayModel = filesViewModel.getDeviceDisplayModel(devId!!)
//                if (deviceDisplayModel != null) {
//                    deviceDisplayModel.isChangedType = deviceDisplayModel.mFileType != spFieldFileType
//                }
                if(findDeviceBean()?.isOwner != true) isToNextPage = true
                val spFieldFileType = OneOSFileType.PUBLIC
                val args = VP2QuickCloudNavFragmentArgs(devId!!, spFieldFileType, null).toBundle()
                findNavController().navigate(R.id.global_action_to_VP2, args)
            }
            else if(findDeviceBean()?.isOwner != true){
                requireActivity().finish()
            }
        }
    }

    companion object {
        fun newInstance(devId: String): SelectTypeFragment {
            val args = Bundle()
            args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            val fragment = SelectTypeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}