package net.sdvn.nascommon.widget

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.objectbox.query.LazyList
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import libs.source.common.livedata.Status
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.FileShareHelper
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.ILoadingCallback
import net.sdvn.nascommon.iface.LoadingCallback
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.PathTypeCompat
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.comp.OneOSFileNameComparator
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.receiver.NetworkStateManager.Companion.instance
import net.sdvn.nascommon.repository.NasRepository
import net.sdvn.nascommon.rx.RxWorkLife
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommonlib.R
import timber.log.Timber
import java.util.*

class ServerFileTreeView @JvmOverloads constructor(private val mActivity: FragmentActivity,
                                                   loadingCallback: ILoadingCallback?,
                                                   loginSession: LoginSession?,
                                                   mTitleID: Int, mPositiveID: Int, negtiveId: Int = 0,
                                                   isEnableShareDownload :Boolean = false) {
    private var rxWorkLife: RxWorkLife = RxWorkLife(mActivity)
    private var mEmptyView: TextView?
    private var mDeviceViewModel: DeviceViewModel
    private var mCurPath: String? = null
    private val mPopupMenu: PopDialogFragment?
    private val mListView: ListView
    private val mFileList = ArrayList<OneOSFile>()
    private val mPasteBtn: TextView
    var mAdapter: PopupListAdapter? = null
    private val mPathPanel: FilePathPanel
    private var listener: OnPasteFileListener? = null
    private var mLoginSession: LoginSession?
    private val mTitleID: Int
    private var mPrivateRootDirShownName: String? = null
    private var mPublicRootDirShownName: String? = null
    private var mPrefixShownName: String? = null

    //    private final DeviceViewModel mDeviceViewModel;
//    private final Observer<List<DeviceModel>> mObserver;
    private var sourceId: String? = null
    private var toId: String? = null
    private var shareListener: OnShareFileListener? = null
    private var mShowDevId: String?
    private var popup: PopupWindow? = null
    private var onUploadFileListener: OnUploadFileListener? = null
    private var loadingCallback: ILoadingCallback? = null
    private val isShare: Boolean
        private get() = mTitleID == R.string.tip_copy_file
    private var nasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, LibApp.instance.getAppExecutors())

    private fun filterData(deviceModels: List<DeviceModel>?, mDeviceRVAdapter: BaseQuickAdapter<DeviceModel, BaseViewHolder>) {
        val models: MutableList<DeviceModel> = ArrayList()
        if (isShare) {
            if (deviceModels != null) {
                for (deviceModel in deviceModels) {
                    if (deviceModel.isEnableDownloadShare()) {
                        if (!deviceModel.isShareV2Available) {
                            val subscribe = FileShareHelper.version(deviceModel.devVip)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        if (it.isSuccessful) {
                                            deviceModel.isShareV2Available = true
                                            models.add(deviceModel)
                                            mDeviceRVAdapter.notifyDataSetChanged()
                                            Logger.LOGD(TAG, "version : " + it.msg)
                                        }
                                    }, {
                                        Timber.e(it, "debug error")
                                    })
                            rxWorkLife.addDisposable(subscribe)
                        } else {
                            models.add(deviceModel)
                        }
                    }/*&& !deviceModel.getDevId().equals(mShowDevId)*/
                }
            }
            mDeviceRVAdapter.setNewData(models)
        } else {
            //        String mDevId = mLoginSession.getId();
            if (deviceModels != null) {
                for (deviceModel in deviceModels) {
                    if (deviceModel.isOnline
                            && deviceModel.devId != mShowDevId) {
                        models.add(deviceModel)
                    }
                }
            }
            mDeviceRVAdapter.setNewData(models)
        }
    }

    private fun resetData(mTitleTxt: TextView) {
        var action = mTitleTxt.context.resources.getString(mTitleID)
        mLoginSession?.let {
            mShowDevId = it.id
            val deviceModel = SessionManager.getInstance().getDeviceModel(mShowDevId)
            val devMarkName = deviceModel?.devName ?: ""
            if (!TextUtils.isEmpty(devMarkName)) action += " $devMarkName"
            isAndroidTV = UiUtils.isAndroidTV(SessionManager.getInstance().getDeviceModel(it.id)?.devClass
                    ?: 0)
        }
        mTitleTxt.text = action
        getFileTreeFromServer(null, null)
    }

    private fun showDeviceSelector(context: Context,
                                   mTitleTxt: TextView,
                                   anchorView: View,
                                   mTitleID: Int) {
        if (popup != null && popup!!.isShowing) {
            popup!!.dismiss()
            return
        }
        val contentView = LayoutInflater.from(context).inflate(R.layout.layout_content_device, null)
        popup = PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {

            animationStyle = R.style.AnimAlphaEnterAndExit
            isTouchable = true
            isOutsideTouchable = false
        }
        //        final PopDialogFragment popup = PopDialogFragment.newInstance(true, contentView);
        contentView.findViewById<View>(R.id.tv_cancel).setOnClickListener { popup!!.dismiss() }
        val mRecycleView: RecyclerView = contentView.findViewById(R.id.recycle_view)
        val layoutManager = LinearLayoutManager(mRecycleView.context)
        mRecycleView.layoutManager = layoutManager

        val mDeviceRVAdapter: BaseQuickAdapter<DeviceModel, BaseViewHolder> =
                object : BaseQuickAdapter<DeviceModel, BaseViewHolder>(R.layout.item_listview_choose_device) {
                    private var briefs: List<BriefModel>? = null
                    override fun setNewData(data: List<DeviceModel>?) {
                        if (data == null) {
                            this.mData = arrayListOf()
                            briefs = null
                        } else {
                            this.mData = data
                            val ids = data.map {
                                it.devId
                            }.toTypedArray()
                            //加载简介数据
                            briefs = BriefRepo.getBriefs(ids, BriefRepo.FOR_DEVICE)
                        }
                        this.notifyItemRangeChanged(0, itemCount, arrayListOf(1))

                    }

                    override fun convertPayloads(helper: BaseViewHolder, item: DeviceModel?, payloads: MutableList<Any>) {
                        item?.let { convert(helper, it) }
                    }

                    override fun convert(holder: BaseViewHolder, item: DeviceModel) {
                        holder.setText(R.id.tv_device_name, item.devName)
                        val device = item.device
                        //                HardWareDevice wareDevice = item.getWareDevice();
                        holder.setText(R.id.tv_device_ip, device!!.vip)
                        holder.setGone(R.id.select_box, false)
                        val iconByeDevClass = io.weline.devhelper.IconHelper.getIconByeDevClass(item.devClass, item.isOnline, true)
//                        (device != null && wareDevice.isOnline()) && device.getDlt().clazz > 0);
                        val iconView = holder.getView<ImageView>(R.id.iv_device)
                        if (holder.itemView.getTag() != device.id) {
                            iconView.setTag(null)
                            holder.itemView.setTag(device.id)
                        }
                        if (iconView.getTag() == null) iconView.setImageResource(iconByeDevClass)
                        val brief = briefs?.find {
                            it.deviceId == device.id
                        }
                        LibApp.instance.getBriefDelegete().loadDeviceBrief(device.id, brief, iconView, null, defalutImage = iconByeDevClass)
                    }
                }

        mDeviceRVAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (Utils.isFastClick(view)) return@OnItemClickListener
            val deviceModel = adapter.getItem(position) as? DeviceModel
            deviceModel?.let {
                if (!deviceModel.isOnline) {
                    ToastHelper.showToast(R.string.device_offline)
                    return@OnItemClickListener
                }
                if (!instance.isEstablished()) {
                    ToastHelper.showToast(R.string.network_not_fine)
                    return@OnItemClickListener
                }
                SessionManager.getInstance().getLoginSession(deviceModel.devId, object : GetSessionListener(false) {
                    override fun onStart(url: String) {
                        loadingCallback?.showLoading()
                    }

                    override fun onSuccess(url: String, data: LoginSession) {
                        loadingCallback?.dismissLoading()
                        mLoginSession = data
                        resetData(mTitleTxt)
                        if (popup != null) popup!!.dismiss()
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        loadingCallback!!.dismissLoading()
                        ToastHelper.showToast(HttpErrorNo.getResultMsg(errorNo, errorMsg))
                    }
                })
            }
        }
        val observer = Observer<List<DeviceModel>?> { deviceModels -> filterData(deviceModels, mDeviceRVAdapter) }
        mDeviceViewModel.liveDevices.observe(mPopupMenu!!, observer)
        mRecycleView.adapter = mDeviceRVAdapter
        mRecycleView.visibility = View.VISIBLE
        val lifecycleObserver: LifecycleObserver = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDismiss() {
                popup?.dismiss()
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        popup?.setOnDismissListener {
            lifecycle.removeObserver(lifecycleObserver)
            popup = null
        }
        //        popup.show(context.getSupportFragmentManager(), "DeviceSelector");
        PopupWindowCompat.showAsDropDown(popup!!, anchorView, 0, 0, Gravity.CENTER)
        //        popup.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
//        popup.setFocusable(true);
//        popup.setOutsideTouchable(true);
//        popup.update();
    }

    private fun notifyRefreshComplete(isItemChange: Boolean, type: OneOSFileType) {
        if (mCurPath != null) {
            if (isAndroidTV) {
                val b = pathIsNotRoot(mCurPath)
                mPathPanel.showNewFolderButton(b)
                mEmptyView?.isVisible = b
                mPasteBtn.isEnabled = b
            } else {
                mEmptyView?.isVisible = true
                mPasteBtn.isEnabled = true
                mPathPanel.showNewFolderButton(true)
            }
        } else {
            mPasteBtn.isEnabled = false
            mEmptyView?.isVisible = false
            mPathPanel.showNewFolderButton(false)
        }
        mPathPanel.updatePath(type, mCurPath, mPrefixShownName)
        mAdapter?.notifyDataSetChanged(isItemChange)
    }

    private fun getFileTreeFromServer(path: String?, share_path_type: Int?) {
        if (mLoginSession?.isLogin != true) {
            dismiss()
            return
        }
        val subscribe = Observable.create<Boolean> {
            val isV5 = SessionCache.instance.isV5OrSynchRequest(mLoginSession?.id
                    ?: "", mLoginSession?.ip ?: "") ?: false
            it.onNext(isV5)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isV5 ->
                    if (EmptyUtils.isEmpty(path)) {
                        mCurPath = path
                        mFileList.clear()

                        val privateDir = OneOSFile()
                        privateDir.setPath(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)
                        privateDir.setName(mPrivateRootDirShownName ?: "")
                        privateDir.share_path_type = SharePathType.USER.type

                        val publicDir = OneOSFile()
                        publicDir.setPath(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)
                        publicDir.share_path_type = SharePathType.PUBLIC.type
                        publicDir.setName(mPublicRootDirShownName ?: "")

                        if (!isV5 && mLoginSession?.isV5 == true) {
                            mFileList.add(privateDir)
                        } else {
                            mFileList.add(privateDir)
                            mFileList.add(publicDir)
                        }
                        notifyRefreshComplete(true, OneOSFileType.PRIVATE)
                    } else {

                        val s = "/"

                        var type: OneOSFileType = OneOSFileType.getTypeByPath(path ?: s)
                        share_path_type?.let {
                            if (isV5) {
                                type = PathTypeCompat.getOneOSFileType(it)
                            } else if (mLoginSession?.isV5 == true) {
                                type = OneOSFileType.PUBLIC
                            }
                        }
                        nasRepository.loadFilesFromServer(devId = mLoginSession!!.id!!, session = mLoginSession!!.session, type = type, path = path
                                ?: s, filter = OneOSFileType.DIR, isV5 = mLoginSession?.isV5
                                ?: false).observe(mActivity, Observer {
                            if (it.status == Status.SUCCESS) {
                                val data = it.data
                                if (data?.isSuccess == true) {
                                    val files = data.data.files
                                    mCurPath = path
                                    mFileList.clear()
                                    if (!EmptyUtils.isEmpty(files)) {
                                        for (file in files) {
                                            if (file.isDirectory()) {
                                                mFileList.add(file)
                                            }
                                        }
                                    }
                                    Collections.sort(mFileList, OneOSFileNameComparator())
                                    notifyRefreshComplete(true, type)
                                } else {
                                    var errorMsg = it.message
                                    errorMsg = HttpErrorNo.getResultMsg(true, it.data?.error?.code
                                            ?: HttpErrorNo.UNKNOWN_EXCEPTION, errorMsg)
                                    if (instance.isEstablished()) ToastHelper.showToast(errorMsg)
                                    notifyRefreshComplete(true, type)
                                }
                            }
                        })
                    }
                }, {

                })
        rxWorkLife.addDisposable(subscribe)

//        val listDirAPI = OneOSListDirAPI(mLoginSession!!, path!!)
//        listDirAPI.setOnFileListListener(object : OnFileListListener {
//            override fun onStart(url: String) {}
//            override fun onSuccess(url: String, type: OneOSFileType, path: String, total: Int, pages: Int, page: Int, files: ArrayList<OneOSFile>) {
//                mCurPath = path
//                mFileList.clear()
//                if (!EmptyUtils.isEmpty(files)) {
//                    mFileList.addAll(files)
//                }
//                Collections.sort(mFileList, OneOSFileNameComparator())
//                notifyRefreshComplete(true)
//            }
//
//            override fun onFailure(url: String, type: OneOSFileType, path: String, errorNo: Int, errorMsg: String) {
//                var errorMsg: String? = errorMsg
//                errorMsg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
//                if (instance.isEstablished) ToastHelper.showToast(errorMsg)
//                notifyRefreshComplete(true)
//            }
//        })
//        listDirAPI.list(OneOSFileType.DIR)
    }

    fun setOnPasteListener(listener: OnPasteFileListener): ServerFileTreeView {
        this.listener = listener
        return this
    }

    fun dismiss() {
        mPopupMenu?.dismiss()
        //        if (mDeviceViewModel != null && mObserver != null)
//            mDeviceViewModel.mData.removeObserver(mObserver);
    }

    fun showPopupCenter( /*FragmentActivity parent*/) { //        PopupWindowCompat.showAsDropDown(mPopupMenu, parent, 0, 0, Gravity.CENTER);
//        mPopupMenu.setFocusable(true);
//        mPopupMenu.setOutsideTouchable(true);
//        mPopupMenu.update();
        mPopupMenu?.show(mActivity.supportFragmentManager, TAG)
    }

    val lifecycle: Lifecycle
        get() = mPopupMenu!!.lifecycle

    fun setShareListener(shareListener: OnShareFileListener?) {
        this.shareListener = shareListener
    }

    fun setOnUploadListener(onUploadFileListener: OnUploadFileListener?) {
        this.onUploadFileListener = onUploadFileListener
    }

    inner class PopupListAdapter(private val mTreeList: List<OneOSFile>?, mListener: View.OnClickListener?) : BaseAdapter() {
        private var mSelectPosition = -1
        private var mListener: View.OnClickListener? = null
        override fun getCount(): Int {
            return mTreeList?.size ?: 0
        }

        override fun getItem(position: Int): Any {
            return mTreeList?.get(position) ?: 0
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        private inner class ViewHolder {
            var userName: TextView? = null
            var userSelect: CheckBox? = null
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            var holder: ViewHolder? = null
            if (convertView == null) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_listview_tree_view,
                        null)
                holder = ViewHolder()
                holder.userName = convertView.findViewById(R.id.file_name)
                holder.userSelect = convertView.findViewById(R.id.file_select)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            val itemFile = mTreeList?.get(position)
            holder.userName?.text = itemFile?.getName()
            holder.userSelect?.isVisible = if (isAndroidTV) pathIsNotRoot(itemFile?.getPath()) else true
            holder.userSelect?.setOnClickListener { v ->
                mSelectPosition = if (mSelectPosition == position) {
                    -1
                } else {
                    position
                }
                notifyDataSetChanged()
                mListener?.onClick(v)
            }
            holder.userSelect?.isChecked = mSelectPosition == position
            return convertView
        }


        fun notifyDataSetChanged(cleanSelect: Boolean) {
            if (cleanSelect) {
                mSelectPosition = -1
            }
            notifyDataSetChanged()
        }

        val selectFile: OneOSFile?
            get() = if (mSelectPosition == -1) {
                null
            } else mTreeList?.get(mSelectPosition)

        init {
            this.mListener = mListener
        }
    }

    interface OnPasteFileListener {
        fun onPaste(tarPath: String?, share_path_type: Int = SharePathType.USER.type)
    }

    interface OnUploadFileListener {
        fun onUpload(toId: String?, tarPath: String?, share_path_type: Int = SharePathType.USER.type)
    }

    interface OnShareFileListener {
        fun onShare(sourceId: String?, toId: String?, tarPath: String?, share_path_type: Int = SharePathType.USER.type)
    }

    companion object {
        private val TAG = ServerFileTreeView::class.java.simpleName
    }

    private var isAndroidTV: Boolean
    private fun pathIsNotRoot(path: String?): Boolean {
        return !(Objects.equals(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR, path) || Objects.equals(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, path))
    }

    init {
        if (loadingCallback == null) {
            this.loadingCallback = object : LoadingCallback() {}
        } else {
            this.loadingCallback = loadingCallback
        }
        mLoginSession = loginSession
        isAndroidTV = UiUtils.isAndroidTV(SessionManager.getInstance().getDeviceModel(mLoginSession?.id)?.devClass
                ?: 0)
        this.mTitleID = mTitleID
        if (loginSession != null) sourceId = loginSession.id
        mShowDevId = sourceId
        val view = LayoutInflater.from(mActivity).inflate(R.layout.layout_popup_file_tree, null)
        mDeviceViewModel = ViewModelProviders.of(mActivity).get(DeviceViewModel::class.java)
        mPrivateRootDirShownName = mActivity.resources.getString(R.string.root_dir_name_private)
        mPublicRootDirShownName = mActivity.resources.getString(R.string.root_dir_name_public)
        mPrefixShownName = mActivity.resources.getString(R.string.root_dir_all)
        val mTitleTxt = view.findViewById<TextView>(R.id.txt_title)
        mPasteBtn = view.findViewById(R.id.btn_paste)
        if (mPositiveID > 0) {
            mPasteBtn.text = mActivity.resources.getString(mPositiveID)
        }
        mPasteBtn.isEnabled = false
        mPathPanel = view.findViewById(R.id.layout_path_panel)
        mPasteBtn.setOnClickListener {
            val mSelectFile = mAdapter?.selectFile
            val selPath: String?
            selPath = mSelectFile?.getPath() ?: mCurPath
            toId = mLoginSession?.id
            val share_path_type = mSelectFile?.share_path_type ?: mPathPanel.sharePathType
            Logger.LOGD(TAG, "Target Path: $selPath +path_type: $share_path_type")
            if (sourceId == toId) {
                if (listener != null) {
                    listener?.onPaste(selPath, share_path_type)
                }
                if (onUploadFileListener != null) {
                    onUploadFileListener?.onUpload(toId, selPath, share_path_type)
                }
            } else {
                if (onUploadFileListener != null) {
                    onUploadFileListener?.onUpload(toId, selPath, share_path_type)
                }
                if (shareListener != null) {
                    shareListener?.onShare(sourceId, toId, selPath, share_path_type)
                }
            }
            dismiss()
        }
        val btnOperate = view.findViewById<ImageButton>(R.id.btn_operate)
        val mCancelBtn = view.findViewById<TextView>(R.id.btn_cancel)
        if (negtiveId != 0) {
            mCancelBtn.setText(negtiveId)
        }
        mCancelBtn.setOnClickListener { dismiss() }

        mPathPanel.setOnPathPanelClickListener { view, path ->
            if (null == path && view.id == R.id.ibtn_new_folder) {
                mCurPath?.let {
                    val fileManage = OneOSFileManage(mActivity, loadingCallback,
                            mLoginSession!!, mPathPanel,
                            OneOSFileManage.OnManageCallback { getFileTreeFromServer(mCurPath, mPathPanel.sharePathType) })
                    fileManage.manage(FileManageAction.MKDIR, mCurPath!!, mPathPanel.sharePathType)
                }
            } else {
                getFileTreeFromServer(path, null)
            }
        }
        mPathPanel.showOrderButton(false)
        mPathPanel.showNewFolderButton(false)
        mEmptyView = view.findViewById<TextView>(R.id.txt_empty)
        mEmptyView?.setOnClickListener {
            mCurPath?.let {
                val fileManage = OneOSFileManage(mActivity, loadingCallback, mLoginSession!!, mPathPanel, OneOSFileManage.OnManageCallback { getFileTreeFromServer(mCurPath, mPathPanel.sharePathType) })
                fileManage.manage(FileManageAction.MKDIR, mCurPath!!, mPathPanel.sharePathType)
            }
        }
        //        mEmptyView.setCompoundDrawables(context.getResources().getDrawable(R.drawable.selector_button_new_folder, null),
//                null, null, null);
        mEmptyView?.setTextColor(mActivity.resources.getColor(R.color.color_white_text))
        mEmptyView?.background = mActivity.resources.getDrawable(R.drawable.bg_button_radius)
        mListView = view.findViewById(R.id.listview)
        mListView.emptyView = mEmptyView
        mListView.visibility = View.VISIBLE
        mAdapter = PopupListAdapter(mFileList, View.OnClickListener {
            val mSelectFile = mAdapter?.selectFile
            if (mSelectFile != null) {
                mPasteBtn.isEnabled = true
            }
        })
        mListView.adapter = mAdapter
        mListView.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val fileTree = mFileList[arg2]
            if (fileTree != null) {
                getFileTreeFromServer(fileTree.getPath(), fileTree.share_path_type)
            }
        }
        mAdapter?.notifyDataSetChanged(true)
        mPopupMenu = PopDialogFragment.newInstance(true, view)
        //        mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
//        mPopupMenu.setTouchable(true);
//        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        mPopupMenu.addDismissListener(DialogInterface.OnDismissListener { })
        mDeviceViewModel.liveDevices.observe(mPopupMenu, Observer {
            it?.let {
                var right: Drawable? = null
                val value = it
                if (mLoginSession?.isShareV2Available == true &&
                        (mTitleID == R.string.tip_copy_file || mTitleID == R.string.tip_upload_file)
                        && value.isNotEmpty() && isEnableShareDownload) {
                    right = mActivity.resources.getDrawable(R.drawable.ic_arrow_drop_down_black_24dp)
                    right.setBounds(0, 0, Utils.dipToPx(24f), Utils.dipToPx(24f))
                    mTitleTxt.setOnClickListener(View.OnClickListener { v ->
                        if (Utils.isFastClick(v)) return@OnClickListener
                        showDeviceSelector(mActivity, mTitleTxt, view.findViewById(R.id.container_title), mTitleID)
                    })
                }
                mTitleTxt.setCompoundDrawables(null, null, right, null)
            }
        })
        resetData(mTitleTxt)
        mTitleTxt.requestFocus()
        mTitleTxt.isSelected = true
    }
}