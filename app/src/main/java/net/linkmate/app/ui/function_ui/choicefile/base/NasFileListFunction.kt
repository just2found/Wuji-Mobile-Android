package net.linkmate.app.ui.function_ui.choicefile.base

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import io.weline.repo.api.V5HttpErrorNo
import libs.source.common.livedata.Status
import libs.source.common.utils.RateLimiter
import libs.source.common.utils.ToastHelper
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import net.linkmate.app.ui.nas.helper.FilesCommonHelper
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.model.PathTypeCompat
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.FilePathPanel
import java.util.concurrent.TimeUnit


//这个类主要实现通用的列表加载，回退相关逻辑，UI请看子类如AbsChoiceFileFragment
abstract class NasFileListFunction : TipsBaseFragment() {

    //和数据相关需要实现的接口
    private val pNasFileInitData by lazy {
        getNasFileInitData()
    }

    abstract fun getNasFileInitData(): NasFileInitData


    abstract fun getFilePathPanel(): FilePathPanel?

    //和UI相关需要实现的接口
    abstract fun getMiddleTile(): View?
    abstract fun getAddFolderBtn(): View?
    abstract fun getRecyclerView(): RecyclerView
    abstract fun onLoadFileError(code: Int?)

    //功能出口
    abstract fun onNext(sharePathType: Int, result: List<OneOSFile>)//又选中结果的狮虎
    abstract fun onDismiss()//取消

    protected var mNowPath: String? = null
    protected var mNowType = -1
    protected var mPage = 0;

    private val mRateLimiter = RateLimiter<Any>(500, TimeUnit.MILLISECONDS) //点击加间隔
    protected var isRootPath = true// 注意这里默认是根目录


    //下面是选择覆盖的
    open fun getPageSize(): Int {
        return AppConstants.PAGE_SIZE
    }

    //这个是否可以回退   返回值 是否消耗掉事件
    fun onPathBack(): Boolean {
        if (isRootPath || getFilePathPanel() == null || mNowPath == null) {
            return false
        } else {
            val parentPath = FileUtils.getParentPath(mNowPath!!)
            if (!TextUtils.isEmpty(parentPath) && parentPath != mNowPath) {
                mNowPath = parentPath!!
                setNowPath()
                reload()
                return true
            } else if (pNasFileInitData.getInitPathType() == null) {
                mNowType = -1
                mNowPath = null
                onNewPathEnable(true)
                onRootSpaceChange(true)
                setNowPath()
                mChoiceNasFileAdapter.setNewData(
                        getNasFileListModel().getRootList(
                                requireContext(),
                                pNasFileInitData.getRootPathType()
                        )
                )
                return true
            } else {
                return false
            }

        }


    }

    open fun getNasFileListModel(): NasFileListModel {
        return pViewModel
    }

    private val pViewModel by viewModels<NasFileListModel>()

    protected val mChoiceNasFileAdapter by lazy {
        val it = ChoiceNasFileAdapter()
        it.mMaxNum = pNasFileInitData.getMaxNum()
        it.showFolderSelect = pNasFileInitData.optionalFolderAble()
        val emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_empty_directory, null)
        getNasFileInitData().getNoDataTips()?.let {
            val txtEmptyTv = emptyView.findViewById<TextView>(R.id.txt_empty_list)
            txtEmptyTv.text = it
        }
        it.emptyView = emptyView
        it.bindToRecyclerView(getRecyclerView())
        it.showSplitLine = getNasFileInitData().showSplitLine()
        //单点击事件
        it.setOnItemClickListener { baseQuickAdapter, view, position ->
            val item = baseQuickAdapter.getItem(position)
            item?.let { it1 ->
                if (mRateLimiter.shouldFetch(it1)) {
                    if (item is OneOSFile) {
                        if ((!pNasFileInitData.optionalFolderAble() || it.mSelectList.isNullOrEmpty()) && item.isDirectory()) {//如果是文件夹

                            onPathChange(item.share_path_type, item.getPath())
                        } else if (pNasFileInitData.getMaxNum() > 1 && baseQuickAdapter is ChoiceNasFileAdapter) {//如果是多选模式

                            baseQuickAdapter.changeItemSelect(position)
                        } else if (pNasFileInitData.getMaxNum() == 1) {//如果是单选模式

                            onNext(mNowType, listOf(item))
                        }
                    }
                }
            }
        }

        //子类点击事件
        it.setOnItemChildClickListener { baseQuickAdapter, view, position ->
            val item = baseQuickAdapter.getItem(position)
            item?.let { it1 ->
                if (item is OneOSFile) {
                    if (pNasFileInitData.getMaxNum() > 1 && baseQuickAdapter is ChoiceNasFileAdapter) {//如果是多选模式
                        baseQuickAdapter.changeItemSelect(position)
                    } else if (pNasFileInitData.getMaxNum() == 1) {//如果是单选模式
                        onNext(mNowType, listOf(item))
                    }
                }
            }
        }

        it.setLoadMoreView(FileLoadMoreView())

        it.setOnItemLongClickListener { baseQuickAdapter, view, position ->
            val item = baseQuickAdapter.getItem(position)
            if (item is OneOSFile) {
                if (!pNasFileInitData.optionalFolderAble() && item.isDirectory()) {//如果是文件夹
                    onPathChange(item.share_path_type, item.getPath())
                } else if (pNasFileInitData.getMaxNum() > 1 && baseQuickAdapter is ChoiceNasFileAdapter) {//如果是多选模式
                    baseQuickAdapter.onLongClickAdd(position)
                } else if (pNasFileInitData.getMaxNum() == 1) {//如果是单选模式
                    onNext(mNowType, listOf(item))
                }
            }
            true
        }



        it
    }
    protected val mRootList by lazy {
        getNasFileListModel().getRootList(
                requireContext(),
                pNasFileInitData.getRootPathType()
        )
    }

    override fun initView(view: View) {
        getRecyclerView().adapter = mChoiceNasFileAdapter
        getNasFileListModel().mSessionLiveData.observe(this, Observer {
            mChoiceNasFileAdapter.mLoginSession = it
        })

        mNowPath = pNasFileInitData.getInitPath()
        mNowType = pNasFileInitData.getInitPathType() ?: -1

        if (mNowPath == null && mNowType == -1) {//如果未指定命名空间
            isRootPath = false
            onRootSpaceChange(true)
            onNewPathEnable(true)
            mChoiceNasFileAdapter.loadMoreEnd(false)
            mChoiceNasFileAdapter.loadMoreComplete()
            mChoiceNasFileAdapter.setNewData(
                    mRootList
            )
        } else {
            isRootPath = true
            onRootSpaceChange(false)
            loadFilesFromServer()
        }
        if(pNasFileInitData.getInitPathType()== SharePathType.GROUP.type&& pNasFileInitData.getGroupId()!=null && pNasFileInitData.getGroupId()!! >0)
        {
            getFilePathPanel()?.setGroupDirShownName(GroupsKeeper.findGroup(pNasFileInitData.getDeviceId(),pNasFileInitData.getGroupId()!!)?.name)
        }
        setNowPath()
        getFilePathPanel()?.showNewFolderButton(false)
        getFilePathPanel()?.showOrderButton(false)
        getFilePathPanel()?.setOnPathPanelClickListener { v, path ->
            if (path == null) {
                showAllTypes()
            } else {
                if (mNowPath != path) {
                    loadFilesFromServer(path)
                }
            }
        }


//        getPathSegmentLayout()?.setOnPathPanelClickListener { view, path ->
//            if (mNowPath != path) {
//                mNowPath = path
//                setNowPath()
//                loadFilesFromServer()
//            }
//        }


        getAddFolderBtn()?.apply {
            if (!pNasFileInitData.addFolderAble()) {
                visibility = View.GONE
            } else {
                setOnClickListener {
                    if (mNowPath.isNullOrEmpty() || mNowType == -1) {
                        ToastHelper.showLongToast(R.string.tip_params_error)
                    } else {
                        getNasFileListModel().createFolder(requireActivity(), mNowPath!!, mNowType,
                                OneOSFileManage.OnManageCallback {
                                    if (it) {
                                       reload()
                                    }
                                })
                    }
                }
            }
        }
    }

    /**
     * 显示全部可选类型
     *
     *
     */
    protected fun showAllTypes() {
        isRootPath = true
        onRootSpaceChange(true)
        onNewPathEnable(true)
        mChoiceNasFileAdapter.loadMoreEnd(false)
        mChoiceNasFileAdapter.loadMoreComplete()
        mChoiceNasFileAdapter.setNewData(
                mRootList
        )
        mNowType = -1
        mNowPath = null
        setNowPath()
    }


    protected fun onPathChange(sharePathType: Int, path: String) {
        if (sharePathType == mNowType && mNowPath == path)
            return
        onRootSpaceChange(false)
        mNowType = sharePathType
//        mNowPath = path
//        setNowPath()
        mPage = 0
        loadFilesFromServer(path)
    }


    protected fun reload() {
        mPage = 0
        loadFilesFromServer()
    }

    private fun loadMoreData() {
        if (isRootPath) {//根目录不加载更多
            mChoiceNasFileAdapter.setEnableLoadMore(false)
            return
        }
        if (mChoiceNasFileAdapter.data.size <= (mPage + 1) * getPageSize()) {//如果数据小于预期则表示 实际没有跟多数据
            mChoiceNasFileAdapter.setEnableLoadMore(false)
            return
        }
        mPage++
        loadFilesFromServer()
    }


    //是不是再根目录选择
    open fun onRootSpaceChange(isRoot: Boolean) {
        if (isRootPath != isRoot) {
            isRootPath = isRoot;
            //           showMiddleTile(!isRootPath)
            showAddFolderBtn(!isRootPath)
        }
    }


    //==如果要增加新的类型需要在这里设置新的头名字 比如R.string.root_dir_name_private
    private fun setNowPath() {
        val filePathPanel = getFilePathPanel() ?: return
        val onsFile = PathTypeCompat.getOneOSFileType(mNowType)
        if (getNasFileInitData().getInitPathType() != null) {
            filePathPanel.updatePath(onsFile, mNowPath)
        } else {
            filePathPanel.updatePath(onsFile, mNowPath, getString(R.string.all))
        }
    }


    private fun loadFilesFromServer(path:String= mNowPath?:"/") {
        val deviceId = pNasFileInitData.getDeviceId()
        getNasFileListModel().loadChoiceFilesFormServer(
                deviceId,
                mNowType,
                path,
                pNasFileInitData.getOneOSFilterType(),
                page = mPage,
                groupId=pNasFileInitData.getGroupId(),
                num = getPageSize()
        ).observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                val data = it.data
                if (data?.isSuccess == true) {
                    if(mNowPath!=path)
                    {
                        mNowPath=path
                        setNowPath()
                    }
                    val files = data.data.files ?: mutableListOf<OneOSFile>()
                    if (mPage == 0 && it.data!!.data.page == 0) {
                        mChoiceNasFileAdapter.setNewData(files)
                    } else if (mPage > 0 && it.data!!.data.page == mPage) {
                        mChoiceNasFileAdapter.addData(files)
                    }

                    if (it.data!!.data.page == mPage && data.data.hasMorePage()) {
                        mChoiceNasFileAdapter.loadMoreComplete()
                        mChoiceNasFileAdapter.setEnableLoadMore(true)
                        mChoiceNasFileAdapter.setOnLoadMoreListener(
                                { loadMoreData() },
                                getRecyclerView()
                        )
                    } else {
                        mChoiceNasFileAdapter.setEnableLoadMore(false)
                    }
                    onRootSpaceChange(false)
                    val isNotEnable = FilesCommonHelper.isNotEnablePath(deviceId,mNowType,mNowPath)
                    onNewPathEnable(isNotEnable)
                } else {
                    onLoadFileError(data?.error?.code)
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(data?.error?.code))
                }

            } else if (it.status == Status.ERROR) {
                mPage--//因为下次进入加载方法时候会触发加一 为保证加载依旧是同一页则减一操作
                //这里也可以考虑不继续加载处理
                mChoiceNasFileAdapter.loadMoreComplete()
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
            }
        })
    }

    /**
     * 新路径是否是可写的
     * */
    open fun onNewPathEnable(isNotEnable: Boolean) {

    }

    private fun showMiddleTile(boolean: Boolean) {
        getMiddleTile()?.let {
            if (boolean) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    private fun showAddFolderBtn(boolean: Boolean) {
        if (!pNasFileInitData.addFolderAble())
            return
        getAddFolderBtn()?.let {
            if (boolean) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }


}