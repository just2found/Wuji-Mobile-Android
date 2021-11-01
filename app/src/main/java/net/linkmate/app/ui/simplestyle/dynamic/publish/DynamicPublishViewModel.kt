package net.linkmate.app.ui.simplestyle.dynamic.publish

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.huantansheng.easyphotos.models.album.entity.Photo
import libs.source.common.utils.UriUtils
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Device
import net.sdvn.cmapi.protocal.EventObserver
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.DynamicMedia

/**
 * @author Raleigh.Luo
 * date：20/12/21 16
 * describe：
 */
class DynamicPublishViewModel : ViewModel() {
    val VIDEO_REQUEST_CODE = 1
    val PICTURE_REQUEST_CODE = 2

    private lateinit var deviceId: String
    private lateinit var deviceIP: String
    private lateinit var networkId: String
    fun init(deviceId: String, deviceIP: String) {
        this.deviceId = deviceId
        this.deviceIP = deviceIP
    }

    /**
     * 监听设备推送
     * 比如上下线，ip更改
     */
    private val mEventObserver: EventObserver = object : EventObserver() {
        override fun onDeviceStatusChange(p0: Device?) {
            super.onDeviceStatusChange(p0)
            if (!TextUtils.isEmpty(deviceId) && p0?.id == deviceId) {//必须上线
                deviceIP = if (p0.isOnline) p0.vip else ""
            }
        }
    }

    init {
        CMAPI.getInstance().subscribe(mEventObserver)
    }

    override fun onCleared() {
        super.onCleared()
        CMAPI.getInstance().unsubscribe(mEventObserver)
    }

    /**--选中的视频信息---------------------------------***/
    private val _selectedVideo: MutableLiveData<Photo> = MutableLiveData()
    val selectedVideo: LiveData<Photo> = _selectedVideo
    fun updateSelectedVideo(video: Photo?) {
        _selectedVideo.value = video
    }

    /**--选中的图片信息---------------------------------***/
    private val _selectedPictures: MutableLiveData<ArrayList<Photo>> = MutableLiveData()
    val selectedPictures: LiveData<ArrayList<Photo>> = _selectedPictures
    fun updateSelectedPictures(photos: ArrayList<Photo>?) {
        _selectedPictures.value = photos
    }

    /**
     * 添加图片
     */
    fun addSelectedPictures(datas: ArrayList<Photo>) {
        val photos = _selectedPictures.value ?: arrayListOf()
        photos.addAll(datas)
        _selectedPictures.value = photos
    }

    /**
     * 移除
     */
    fun removeSelectedPicture(index: Int) {
        val photos = _selectedPictures.value ?: arrayListOf()
        photos.removeAt(index)
        _selectedPictures.value = photos
    }

    /**--发布动态---------------------------------***/
    private fun getNickName(): String {
//        val bean = UserInfoManager.getInstance().userInfoBean
//        return if (TextUtils.isEmpty(bean.nickname)) bean.loginname else bean.nickname
        return CMAPI.getInstance().baseInfo.account
    }

    private fun getUserId(): String {
        return CMAPI.getInstance().baseInfo.userId
    }

    fun publish(content: String?, callback: Function<Void, Void>) {
        networkId = CMAPI.getInstance().baseInfo.netid
        val dynamic = Dynamic()
        dynamic.networkId = networkId
        dynamic.deviceId = deviceId
        dynamic.UID = getUserId()
        dynamic.Username = getNickName()
        dynamic.Content = content

        var index = 0

        _selectedVideo.value?.let {
            val uri = UriUtils.uri2File(MyApplication.getContext(), Uri.parse(it.path))
            val path = if(uri == null) it.path else uri.absolutePath
            val media = DynamicMedia()
            media.id = -1
            media.index = index
            media.thumbnail = path
            media.momentID = -1
            media.localPath = path
            media.width = it.width
            media.height = it.height
            if (it.width == 0 || it.height == 0) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(path)
                    val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                            ?: 0 //宽
                    val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                            ?: 0 //高
                    val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt()
                            ?: 0//视频的方向角度
                    if (rotation % 180 == 0) {//纵向
                        media.width = width
                        media.height = height
                    } else {//横向
                        media.width = height
                        media.height = width
                    }
                } catch (e: Exception) {
                }
            }
            media.type = media.getVideoType()
            dynamic.MediasPO.add(media)
            index++
        }

        _selectedPictures.value?.let {
            it.forEach {
                val uri = UriUtils.uri2File(MyApplication.getContext(), Uri.parse(it.path))
                val path = if(uri == null) it.path else uri.absolutePath
                val media = DynamicMedia()
                media.id = -1
                media.index = index
                media.thumbnail = path
                media.momentID = -1
                media.localPath = path
                media.type = media.getImageType()
                media.width = it.width
                media.height = it.height
                dynamic.MediasPO.add(media)
                index++
            }
        }
        DBDelegete.dynamicDelegete.updateLocal(dynamic, callback = Function {
            DynamicQueue.push(DynamicQueue.PUBLISH_DYNAMIC_TYPE, it)
            callback.apply(null)
            null
        })
    }

}