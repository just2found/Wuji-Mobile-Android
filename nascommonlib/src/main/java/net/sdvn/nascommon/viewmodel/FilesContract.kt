package net.sdvn.nascommon.viewmodel

import net.sdvn.nascommon.iface.DisplayMode
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.viewmodel.FilesViewModel.DeviceDisplayModel
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel.OnFileSelectListener

interface FilesContract {
    interface View<T> {
        fun showTipMessage(content: String)
        fun showLoading()
        fun showSelectAndOperatePanel(isShow: Boolean)
        fun updateSelectAndManagePanel(isMore: Boolean, mode: DisplayMode?
                                       , mFileSelectListener: OnFileSelectListener?
                                       , mFileManageListener: FileManagePanel.OnFileManageListener<OneOSFile>?)

        fun onLoadMore(ts: List<T>?)
        fun onRefresh(isSearch: Boolean, ts: List<T>?)
        fun onLoadFailed(err: String,errNo:Int)
        fun onRestoreState(deviceDisplayModel: DeviceDisplayModel)
        fun showNewFolder()
    }

    interface Presenter<T> {
        fun add(devId: String)
        fun getFiles(devId: String, fileType: OneOSFileType, path: String?, page: Int, isLoadMore: Boolean, orderTypeV2: FileOrderTypeV2)
        fun searchFiles(devId: String, fileType: OneOSFileType, searchFilter: String)
        fun loadLocalData(devId: String, curPath: String, orderTypeV2: FileOrderTypeV2)
    }
}