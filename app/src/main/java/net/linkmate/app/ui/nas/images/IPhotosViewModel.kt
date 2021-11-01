package net.linkmate.app.ui.nas.images

import androidx.lifecycle.LiveData
import libs.source.common.livedata.Resource
import net.linkmate.app.ui.viewmodel.GenFileUrl
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.DataFile

/**
 *
 * @Description: java类作用描述
 * @Author: todo2088
 * @CreateDate: 2021/3/17 16:01
 */
interface IPhotosViewModel<T : DataFile> {
    abstract val liveDataPicFiles: LiveData<Resource<List<T>>>
    fun getDeviceId(): String
    fun getItemShareTransitionName(item: DataFile): String {
        return "istn_${item.getPathType()}_${item.getPath()}"
    }

    abstract fun getPagesPicModel(): OneFilePagesModel<T>
    abstract fun loadImageMore()

    fun getGlideModeTb(model: DataFile,groupId:Long?=null): Any? {
        return GenFileUrl.getGlideModeTb(getDeviceId(), model.getPathType(), model.getPath(),groupId=groupId)
    }

    fun getGlideMode(model: DataFile,groupId:Long?=null): EliCacheGlideUrl? {
        return GenFileUrl.getGlideMode(getDeviceId(), model.getPathType(), model.getPath(),groupId=groupId)
    }

}