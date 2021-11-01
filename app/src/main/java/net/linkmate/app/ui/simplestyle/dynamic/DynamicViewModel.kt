package net.linkmate.app.ui.simplestyle.dynamic

import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.data.model.dynamic.DynamicList
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.manager.DevManager
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.delegate.TextShrinkDelegate
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.NetworkModel
import net.linkmate.app.ui.simplestyle.dynamic.CircleStatus.*
import net.linkmate.app.util.CheckStatus
import net.sdvn.cmapi.Device
import net.sdvn.cmapi.protocal.EventObserver
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import java.lang.Exception

/**
 * @author Raleigh.Luo
 * date：20/11/21 11
 * describe：
 */
class DynamicViewModel : DynamicBaseViewModel() {
    var mainENDevice: DeviceBean? = null
        private set(value) {
            field = value
        }

    /**
     * 监听设备推送
     * 比如上下线，ip更改
     */
    private val mEventObserver: EventObserver = object : EventObserver() {
        override fun onDeviceStatusChange(p0: Device?) {
            super.onDeviceStatusChange(p0)
            if (!TextUtils.isEmpty(DynamicQueue.deviceId) && p0?.id == DynamicQueue.deviceId) {//必须上线
                mainENDevice = DevManager.getInstance().deviceBeans.find {
                    it.id == DynamicQueue.deviceId
                }
                DynamicQueue.deviceIP = (if (p0.isOnline) p0.vip else "")
                initCircleStatus()
                if (circleStatus.value == NOMARL && (refreshDynamicResult.value == null || refreshDynamicResult.value?.status == Status.NONE)) {
                    refresh(false)
                }
            }
        }
    }


    init {
        CMAPI.getInstance().subscribe(mEventObserver)
    }


    val textShrinkDelegatesManager: ArrayList<TextShrinkDelegate> = arrayListOf()
    private val _toastText: MutableLiveData<String> = MutableLiveData()
    val toastText: LiveData<String> = _toastText
    fun showToast(text: String) {
        _toastText.value = text
    }

    /**--保存上一次请求的动态 网络／用户／设备信息----------------------------------------------------------**/
    var mLastNetworkId: String? = null //上一次请求的网络id
        protected set(value) {
            field = value
        }
    var mLastUserId: String? = null
        //上一次请求的用户id
        protected set(value) {
            field = value
        }
    var mLastDeviceId: String? = null
        //上一次请求的设备id
        protected set(value) {
            field = value
        }

    /**
     * 是否显示数据
     */
    fun isDisplayData(): Boolean = circleStatus.value == NOMARL || circleStatus.value == DEVICE_OFFLINE
    fun isRequestFailed(): Boolean = circleStatus.value == NOMARL && refreshDynamicResult.value?.status == Status.ERROR

    @Volatile
    private var isSameDynamic = false

    /**
     * 是否时相同动态
     * 仅在currentNetwork 监听时调用一次
     *
     */
    fun isSameDynamic(): Boolean {
        isSameDynamic = mLastNetworkId == currentNetwork.value?.netId && mLastUserId == CMAPI.getInstance().baseInfo.userId
                && mLastDeviceId == currentNetwork.value?.getMainENDeviceId()
        mLastNetworkId = currentNetwork.value?.netId
        mLastDeviceId = currentNetwork.value?.getMainENDeviceId()
        mLastUserId = CMAPI.getInstance().baseInfo.userId
        return isSameDynamic
    }

    /**--CommentPopupWindow屏幕的位置 y坐标----------------------------------------------------------**/
    private val _commentPopupWindowLocationY = MutableLiveData<Int>()
    val commentPopupWindowLocationY: LiveData<Int> = _commentPopupWindowLocationY
    fun setCommentPopupWindowLocationY(value: Int) {
        _commentPopupWindowLocationY.value = value
    }

    /**--查看详情----------------------------------------------------------*/
    val accessDynamicDetailId = MutableLiveData<Long>()
    var accessDynamicDetailComment: CommentEvent? = null

    fun accessDynamicDetail(dynamicId: Long?, commentEvent: CommentEvent? = null) {
        accessDynamicDetailComment = commentEvent
        accessDynamicDetailId.value = dynamicId
    }

    /**--显示左上角发布按钮----------------------------------------------------------*/
    val swipeRefreshLayoutEnable = MutableLiveData<Boolean>(false)
    //显示头部圈子名称
//    val isHeaderCircleNameVisibility = MutableLiveData<Boolean>(true)

    /**--显示左上角发布按钮----------------------------------------------------------*/

    private val _showAddMenu = MutableLiveData<Boolean>()
    val showAddMenu: LiveData<Boolean> = _showAddMenu
    fun showAddMenu(isShow: Boolean) {
        _showAddMenu.value = isShow
    }

    /**--当前网络----------------------------------------------------------**/

    val currentNetwork: LiveData<NetworkModel> = NetsRepo.getCurrentNetwork()

    fun clearDynamicList() {
        _isRefresh.value = null
    }

    /**--圈子当前状态----------------------------------------------------------**/
    private val _circleStatus = MutableLiveData<CircleStatus>(NONE)
    val circleStatus: LiveData<CircleStatus> = _circleStatus

    //用来处理无积分的优先级最高问题
    var isWithOutPoint = false
        set(value) {
            //从没有积分到有积分,自动请求数据
            if (field && !value) {
                field = value
                refresh(true)
                initCircleStatus()
            } else {
                field = value
            }
        }

    fun updateCircleStatus(status: CircleStatus) {
        if (!isWithOutPoint) {//必须有积分的情况下才去更新其它状态
            //实时更新
            DynamicQueue.currentCircleStatus = status.type
            _circleStatus.value = status
        }
    }

    /**--登录----------------------------------------------------------**/
    private val _startLogin = MutableLiveData<Boolean>()
    val loginResult = _startLogin.switchMap {
        isSameDynamic = true
        repository.login(DynamicQueue.deviceIP)
    }

    /**--动态列表----------------------------------------------------------**/
    val PAGE_SIZE: Int = 20 //动态加载页数
    private var mLimitMaxTime: Long = 0L//最大边界值 时间戳秒数，加载＝最后一条记录，刷新＝ 当前时间

    //true 刷新， false 加载
    private val _isRefresh = MutableLiveData<Boolean>()

    val refreshDynamicResult: LiveData<Resource<DynamicList>> = _isRefresh.switchMap {
        if (it == null) {//清空数据
            mLimitMaxTime = getCurrentTime()
            loadFromDB(null)
            //清空列表，需在loadFromDB后
            MutableLiveData(Resource<DynamicList>(Status.NONE, null, null, null))
        } else {//加载数据
            if (it) {//刷新
                //刷新才更新last信息，保证 请求列表的数据 mLastNetworkId = currentNet, deviceId ＝ mLastDeviceId
                //后续操作都使用该字段
                mLimitMaxTime = getCurrentTime()
            } else {//加载
                //获取最后一个项的时间
                mLimitMaxTime = dynamicList.value?.get((dynamicList.value?.size
                        ?: 0) - 1)?.CreateAt ?: 0L
            }
            _refreshAboutMessage.value = true
            repository.getDynamicList(mLastNetworkId
                    ?: "", DynamicQueue.deviceId, DynamicQueue.deviceIP, mLimitMaxTime, PAGE_SIZE)
        }
    }

    //当前请求的数据
    private val _refreshDB = MutableLiveData<Long>()

    /**
     * 加载数据库数据
     * 注意：有新数据得重新加载
     */
    fun loadFromDB(currentRequestTotalSize: Long? = _refreshDB.value) {
        _refreshDB.value = currentRequestTotalSize
    }

    val dynamicList: LiveData<out List<Dynamic>> = _refreshDB.switchMap {
        if (it == null) {
            //现有列表数据条数+页数
            MutableLiveData<List<Dynamic>>(ArrayList<Dynamic>())
        } else {
            //现有列表数据条数+页数
            DBDelegete.dynamicDelegete.querysLiveData(mLastNetworkId
                    ?: "", DynamicQueue.deviceId, it)
                    ?: MutableLiveData<List<Dynamic>>(ArrayList<Dynamic>())
        }
    }


    fun getDynamicListSize(): Int {
        return dynamicList.value?.size ?: 0
    }

    private fun getCurrentTime(): Long {
        return System.currentTimeMillis() / 1000
    }

    /**
     * 后台刷新动态列表，获取后台缓存，后台子线程更新，且已加载过一次后
     */
    fun refreshNewestDynamicList() {
        /**
         * 1.必须已经连接vpn
         * 2.必须未在加载中
         * 3.必须有主EN
         */
        if (CMAPI.getInstance().isEstablished && refreshDynamicResult.value?.status != Status.LOADING && circleStatus.value == NOMARL) {
            val size = Math.max(getDynamicListSize(), PAGE_SIZE)
            _refreshNewestDynamicSize.value = Math.min(size, 50)
        }
    }

    private val _refreshNewestDynamicSize = MutableLiveData<Int>()
    val refreshNewestDynamicListResult = _refreshNewestDynamicSize.switchMap {
        repository.getNewestDynamicList(DynamicQueue.deviceId, DynamicQueue.deviceIP, it)
    }

    fun getNewestDynamicListSize(): Long {
        val dynamicListSize = getDynamicListSize()
        //以现有列表数据刷新，最少PAGE_SIZE
        return Math.max(dynamicListSize, PAGE_SIZE).toLong()

    }


    /**
     * 初始化圈子状态
     */
    fun initCircleStatus() {
        currentNetwork.value?.let {//有网络
            DynamicQueue.isCircleOwner = it.isOwner
            val mainENDeviceId = it.getMainENDeviceId()
            if (!TextUtils.isEmpty(mainENDeviceId)) {
                if (TextUtils.isEmpty(DynamicQueue.deviceId) || DynamicQueue.deviceId != mainENDeviceId) {
                    //设备为空或设备不同时，更新设备id
                    DynamicQueue.deviceId = mainENDeviceId
                    DynamicQueue.deviceIP = SDVNManager.instance.getDevVip(mainENDeviceId) ?: ""
                }
                mainENDevice = DevManager.getInstance().deviceBeans.find {
                    it.id == mainENDeviceId
                }
                if (TextUtils.isEmpty(DynamicQueue.deviceIP)) DynamicQueue.deviceIP = mainENDevice?.vip
                        ?: ""
                updateCircleStatus(CheckStatus.checkDeviceStatus(mainENDevice))
            } else {//无主EN，提示
                if (it.isCharge == true) {
                    DynamicQueue.deviceId = ""
                    DynamicQueue.deviceIP = ""
                    updateCircleStatus(WITHOUT_DEVICE_SERVER)
                } else {//免费圈子
                    DynamicQueue.deviceId = ""
                    DynamicQueue.deviceIP = ""
                    updateCircleStatus(NOT_SUPPORT_DYNAMIC)
                }

            }
        } ?: let {//无网络
            DynamicQueue.deviceId = ""
            DynamicQueue.deviceIP = ""
            updateCircleStatus(WITHOUT_NETWORK)
        }
    }

    /**刷新动态,并且检查圈子状态
     * @param isForcusRefresh 是否为强制刷新
     * @param callback 回调是否刷新列表了
     */
    fun refreshAndCheckStatus(isForcusRefresh: Boolean, callback: Function<Boolean, Void>? = null) {
        initCircleStatus()
        refresh(isForcusRefresh, callback)
    }

    /**刷新动态
     * @param isForcusRefresh 是否为强制刷新
     * @param callback 回调是否刷新列表了
     */
    fun refresh(isForcusRefresh: Boolean, callback: Function<Boolean, Void>? = null) {
        if (circleStatus.value == NOMARL) {//需要刷新数据
            checkLoginToken(Function {
                //刷新数据
                if (it && !TextUtils.isEmpty(DynamicQueue.deviceIP)) {
                    /**刷新条件
                     * 1.还未被赋值／强制刷新／非相同动态
                     */
                    if (_isRefresh.value == null || isForcusRefresh || !isSameDynamic) {
                        if (!isSameDynamic) {//不同动态圈子先登录
                            _isRefresh.value = null
                            _startLogin.value = true
                        } else {
                            //优先加载数据库数据
                            loadFromDB(PAGE_SIZE.toLong())
                            _isRefresh.value = true
                        }
                        callback?.apply(true)
                    } else {
                        callback?.apply(false)
                    }
                } else {
                    callback?.apply(false)
                }

                null
            })
        } else {
            callback?.apply(false)
        }
    }

    //是否正在加载
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    fun updateLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    //是否加载到底部了
    private val _isLoadToEnd = MutableLiveData<Boolean>()
    val isLoadToEnd: LiveData<Boolean> = _isLoadToEnd
    fun updateLoadToEnd(isLoadToEnd: Boolean) {
        _isLoadToEnd.value = isLoadToEnd
    }

    /**
     * 下拉加载
     */
    fun loadDynamicList() {
        if (getDynamicListSize() > 0) {
            updateLoading(true)
            loadFromDB(getDynamicListSize().toLong() + PAGE_SIZE)
            _isRefresh.value = false
        }
    }

    /**
     * 被删除后，页面不足一页时，自动刷新
     */
    fun reloadDynamicList() {
        if (getDynamicListSize() <= PAGE_SIZE) {
            refresh(true)
        }
    }


    /**--与我相关----------------------------------------------------------*/
    private val _refreshAboutMessage = MutableLiveData<Boolean>()
    fun refreshAboutMessage() {
        if (CMAPI.getInstance().isEstablished) _refreshAboutMessage.value = true
    }

    val aboutMessageResult = _refreshAboutMessage.switchMap {
        repository.getRelatedMessage(DynamicQueue.deviceId, DynamicQueue.deviceIP)
    }

    /**--与我相关刷新指定动态----------------------------------------------------------*/
    var lastMomentIds: List<String>? = null
    fun refreshDynamics() {
//        aboutMessageResult.value?.data?.momentid?.let {
//            viewModelScope.launch(Dispatchers.IO) {//开启协程
//                if (lastMomentIds == null) {
////                    val updateDynamicIds = arrayListOf<String>()//不更新重复动态
////                    it.forEach {//逐个更新动态
////                        if (!updateDynamicIds.contains(it)) {
////                            repository.getDynamicRxForAbout(deviceId, deviceIP, it)
////                            updateDynamicIds.add(it)
////                        }
////                    }
//                } else {//有旧数据，匹配下，已更新的不更新
////                    val updateDynamicIds = arrayListOf<String>()//不更新重复动态
////                    for (i in 0 until it.size) {
////                        val dynamicId = it[i]
////                        if (!updateDynamicIds.contains(dynamicId) && (i >= (lastMomentIds?.size
////                                        ?: 0) || lastMomentIds?.get(i) != dynamicId)) {
////                            updateDynamicIds.add(dynamicId)
////                            repository.getDynamicRxForAbout(deviceId, deviceIP, it[i])
////                        }
////                    }
////                    lastMomentIds = it
//                }
//            }
//            true
//        } ?: let {
//            lastMomentIds = null
//        }
    }

    private var loadingManyRelatedJob: Job? = null

    /**
     * 加载多关系，需预先加载，否则会很慢,UI卡顿
     */
    fun loadingManyRelated(callback: Function<Void, Void>) {
        if ((dynamicList.value?.size ?: 0) == 0) {
            callback.apply(null)
        } else {
            if (loadingManyRelatedJob != null && !(loadingManyRelatedJob?.isCompleted ?: false)) {
                //已有任务，取消，再开始
                loadingManyRelatedJob?.cancel()
            }

            loadingManyRelatedJob = null
            loadingManyRelatedJob = viewModelScope.launch {
                loadMany()
            }
            loadingManyRelatedJob?.invokeOnCompletion {//任务完成
                callback.apply(null)
            }
        }
    }

    suspend fun loadMany() = withContext(Dispatchers.IO) {
        // 繁重任务
        dynamicList.value?.let {
            try {
                for (i in 0 until it.size) {
                    val dynamic = it.get(i)
                    while (!dynamic.MediasPO.isResolved) dynamic.MediasPO.isEmpty()
                    if (!dynamic.CommentsPO.isResolved) dynamic.CommentsPO.isEmpty()
                    if (!dynamic.LikesPO.isResolved) dynamic.LikesPO.isEmpty()
                    if (!dynamic.AttachmentsPO.isResolved) dynamic.AttachmentsPO.isEmpty()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**--清除与我相关----------------------------------------------------------*/
    private val _clearRelatedMessage = MutableLiveData<Boolean>()
    fun clearRelatedMessage() {
        lastMomentIds = null
        _clearRelatedMessage.value = true
    }

    val clearRelatedMessageResult = _clearRelatedMessage.switchMap {
        repository.getRelatedMessage(DynamicQueue.deviceId, DynamicQueue.deviceIP, 1)
    }

    /***－－设圈子简介－－－－－－－－－－**/
    val startGetCircleBrief = MutableLiveData<String>()
    fun startGetCircleBrief(deviceId: String?) {
        if (startGetCircleBrief.value == null || startGetCircleBrief.value != deviceId) {
            startGetCircleBrief.value = deviceId
            //请求远端数据，不强制更新
            if (!TextUtils.isEmpty(deviceId)) BriefManager.requestRemoteBrief(deviceId!!, BriefRepo.FOR_CIRCLE, BriefRepo.ALL_TYPE)
        }
    }

    val circleBrief = startGetCircleBrief.switchMap {
        if (TextUtils.isEmpty(it)) {
            MutableLiveData<List<BriefModel>>(null)
        } else {
            BriefRepo.getBriefLiveData(it, BriefRepo.FOR_CIRCLE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        CMAPI.getInstance().unsubscribe(mEventObserver)
        //释放对象
        for (delegate in textShrinkDelegatesManager) {
            delegate.clear()
        }
        textShrinkDelegatesManager.clear()
    }

}