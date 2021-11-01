package net.sdvn.nascommon.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.chad.library.adapter.base.entity.SectionEntity
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.UserSpace
import io.weline.repo.data.model.Users
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.CommonResultListener
import net.sdvn.common.internet.loader.DeviceSharedUsersHttpLoader
import net.sdvn.common.internet.protocol.SharedUserList
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.db.FriendsKeeper
import net.sdvn.nascommon.db.objecbox.FriendItem
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.ILoadingCallback
import net.sdvn.nascommon.iface.LoadingCallback
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSHardDisk
import net.sdvn.nascommon.model.oneos.OneOSUser
import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSListUserAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommonlib.R
import timber.log.Timber
import java.util.regex.Pattern

class UserModel(app: Application) : AndroidViewModel(app) {
    val mUserList = MutableLiveData<List<OneOSUser>>()
    private val map = hashMapOf<Boolean, LinkedHashMap<Any, SectionEntity<OneOSUser>>>()
    val mUserEntity = mUserList.map { list: List<OneOSUser>? ->
        list ?: return@map listOf<SectionEntity<OneOSUser>>()
        val sortedBy = list.sortedBy { it.isRemote }
        val listEntity = mutableListOf<SectionEntity<OneOSUser>>()
        for (key in listOf(true, false)) {
            var linkedHashMap = map.get(key)
            if (linkedHashMap == null) {
                linkedHashMap = linkedMapOf()
                map.put(key, linkedHashMap)
            } else {
                linkedHashMap.clear()
            }
        }
        for (oneOSUser in sortedBy) {
            if (oneOSUser.name.isNullOrEmpty()) continue
            val remote = oneOSUser.isRemote
            var linkedHashMap = map.get(remote)
            if (linkedHashMap == null) {
                linkedHashMap = linkedMapOf()
                map.put(remote, linkedHashMap)
            }
            val key = oneOSUser.name!!
            var entity = linkedHashMap.get(key)
            if (entity == null) {
                entity = SectionEntity(oneOSUser)
                linkedHashMap.put(key, entity)
            }
            entity.t = oneOSUser
        }

        for (key in listOf(true, false)) {
            val i = if (key) R.plurals.shared_users else R.plurals.local_accounts
            map.get(key).takeIf { it != null && it.size > 0 }?.apply {
                val header = app.resources.getQuantityString(i, size, size)
                Timber.d("$size :$header")
                listEntity.add(SectionEntity(true, header))
                listEntity.addAll(values)
            }
        }
        return@map listEntity
    }

    var mServerList :SharedUserList? =null
    val mServerUserList = MutableLiveData<SharedUserList>()
    var total: Long = 0
        private set
    private val REG_TEST_ACCOUNT = "MemeTest+[0-9]{2}"
    fun getUserList(baseActivity: BaseActivity, devId: String, mLoginSession: LoginSession) {
        getUserList(baseActivity, object : LoadingCallback() {}, devId, mLoginSession, true)
    }

    fun getUserList(baseActivity: AppCompatActivity, loadingCallback: ILoadingCallback, devId: String,
                    mLoginSession: LoginSession, needGetSpace: Boolean) {

        //获取服务器中的用户列表
        val loader = DeviceSharedUsersHttpLoader(SharedUserList::class.java)
        loader.setParams(devId)
        loader.executor(object : CommonResultListener<SharedUserList>() {

            override fun success(tag: Any?, sharedUserList: SharedUserList) {
                viewModelScope.launch(Dispatchers.Default) {
                    sharedUserList.users.sortBy { it.mgrlevel }
                    with(sharedUserList.users){
                       val list =filter {
                            it.mgrlevel<3
                        }
                        clear()
                        addAll(list)
                    }
                    mServerList=sharedUserList
                    //mServerUserList.postValue(sharedUserList)
                }
                getDeviceLocalAccounts(mLoginSession, loadingCallback, sharedUserList, needGetSpace, devId, baseActivity)
            }

            override fun error(tag: Any?, mErrorProtocol: GsonBaseProtocol) {
                ToastHelper.showToast(SdvnHttpErrorNo.ec2String(mErrorProtocol.result))
                loadingCallback.dismissLoading()
                mUserList.postValue(mUserList.value)
            }
        })
    }

    fun getDeviceLocalAccounts(mLoginSession: LoginSession, loadingCallback: ILoadingCallback,
                               sharedUserList: SharedUserList, needGetSpace: Boolean,
                               devId: String, baseActivity: AppCompatActivity) {

        val listener = object : OneOSListUserAPI.OnListUserListener {
            override fun onStart(url: String) {

                loadingCallback.showLoading(R.string.getting_user_list)
            }

            override fun onSuccess(url: String, users: List<OneOSUser>?) {
                if (users != null) {
                    for (user in users) {
                        if (url != "V5") {
                            if (user.isAdmin == 0) {
                                user.isAdmin = Integer.parseInt(MGR_LEVEL.COMMON)
                            }
                        }
                        user.type = user.type or OneOSUser.TYPE_LOCAL
                    }
                }

                val oneOSUsers: MutableList<OneOSUser>? = combineServerLocalUserAndSort(users, sharedUserList, mLoginSession)
                loadingCallback.dismissLoading()
                if (oneOSUsers != null) {
                    val iterator = oneOSUsers.iterator()
                    val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
                    if (UiUtils.isM8(deviceModel?.devClass ?: 0)) {
                        while (iterator.hasNext()) {
                            val osUser = iterator.next()
                            val compile = Pattern.compile(REG_TEST_ACCOUNT)
                            val name = osUser.name
                            if (!name.isNullOrEmpty()) {
                                if (compile.matcher(name).matches()) {
                                    iterator.remove()
                                    delUser(devId, name)
                                }
                            }
                        }
                    }
                }
                if (needGetSpace) {
                    queryDeviceTotalSpace(devId, mLoginSession, oneOSUsers)
                }
                val data=  mServerList
                oneOSUsers?.forEach{
                   if(!it.isRemote && !hasUser(it.name!!) )
                   {
                       val shareUser= ShareUser()
                       shareUser.username=it.name
                       shareUser.userid=it.name
                       shareUser.mgrlevel=MGR_LEVEL.UNBOUND.toInt()
                       data?.users?.add(shareUser)
                   }
                }
                data?.let {mServerUserList.postValue(it) }
                mUserList.postValue(oneOSUsers!!)

            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                var msg = errorMsg
                loadingCallback.dismissLoading()
                msg = HttpErrorNo.getResultMsg(true, errorNo, msg)
                if (SessionManager.getInstance().isLogin(devId) && baseActivity.lifecycle.currentState == Lifecycle.State.RESUMED)
                    loadingCallback.showTipView(msg, false)
                mServerList?.let { mServerUserList.postValue(it) }
                val oneOSUsers: MutableList<OneOSUser>? = combineServerLocalUserAndSort(null, sharedUserList, mLoginSession)
                mUserList.postValue(oneOSUsers!!)

            }
        }
        val observer = object : V5Observer<Users>(mLoginSession.id ?: "") {

            override fun onSubscribe(d: Disposable) {
                listener.onStart("")
            }

            override fun success(result: BaseProtocol<Users>) {
                result.data?.users?.let {
                    val resultUsers = it
                    Observable.create { emitter: ObservableEmitter<List<OneOSUser>> ->
                        //开启线程转化数据
                        val users: ArrayList<OneOSUser> = arrayListOf()
                        for (user in resultUsers) {
                            val mUser = OneOSUser(user.username, user.uid, user.gid, user.admin, user.mark)
                                    .apply {
                                        permissions = user.permissions
                                    }
//                            mUser.type = mUser.type or TYPE_LOCAL
                            users.add(mUser)
                        }
                        emitter.onNext(users)
                        emitter.onComplete()
                    }.subscribe {
                        listener.onSuccess("V5", it)
                    }
                }
            }

            override fun fail(result: BaseProtocol<Users>) {
                listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
            }


            override fun retry(): Boolean {
                //重试
//                V5Repository.INSTANCE().getUserList(mLoginSession.id?:"", mLoginSession.ip, LoginTokenUtil.getToken(), this)
                return false
            }

            override fun isNotV5() {
                //调用旧API
                val listUserAPI = OneOSListUserAPI(mLoginSession)
                listUserAPI.setBackOnUI(false)
                listUserAPI.setOnListUserListener(listener)
                listUserAPI.list()
            }
        }
        V5Repository.INSTANCE().getUserList(mLoginSession.id
                ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), observer)
    }

    private fun delUser(devId: String, name: String) {
        DeviceViewModel().removeSelf(devId, name, Callback { }, false)
    }

    private fun hasUser(name: String):Boolean{
        mServerList?.users?.forEach {
            if(it.username==name)
                return true
        }
        return false
    }


    fun queryDeviceTotalSpace(devId: String, mLoginSession: LoginSession, users: List<OneOSUser>?) {
        val spaceAPI2 = OneOSSpaceAPI(mLoginSession)
        if (users != null && users.size > 0) {
            val startOldRequest = {
                spaceAPI2.setOnSpaceListener(object : OneOSSpaceAPI.OnSpaceListener {
                    override fun onStart(url: String) {}

                    override fun onSuccess(url: String, isOneOSSpace: Boolean, hd1: OneOSHardDisk, hd2: OneOSHardDisk?) {
                        total = hd1.total
                        if (users != null) {
                            for (user in users) {
                                if (user.type and OneOSUser.TYPE_LOCAL != 0) {
                                    getUserSpace(devId, user, mLoginSession, total)
                                } else {
                                    user.space = total
                                    user.used = 0
                                }
                            }
                        }
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {

                    }
                })
                spaceAPI2.query(true)
            }

            val observer = object : V5Observer<Any>(mLoginSession.id ?: "") {
                override fun success(result: BaseProtocol<Any>) {
                    val hd1 = OneOSHardDisk()
                    val hd2 = OneOSHardDisk()
                    OneOSSpaceAPI.getHDInfo(Gson().toJson(result.data), hd1, hd2)
                    total = hd1.total
                    if (users != null) {
                        for (user in users) {
                            if (user.type and OneOSUser.TYPE_LOCAL != 0) {
                                getUserSpace(devId, user, mLoginSession, total)
                            } else {
                                user.space = total
                                user.used = 0
                            }
                        }
                    }
                }

                override fun fail(result: BaseProtocol<Any>) {

                }

                override fun isNotV5() {//调用旧接口
                    startOldRequest()
                }

                override fun retry(): Boolean {
//                    V5Repository.INSTANCE().getHDSmartInforSystem(mLoginSession.id?:"", mLoginSession.ip, LoginTokenUtil.getToken(), this)
                    return false
                }
            }
            V5Repository.INSTANCE().getHDSmartInforSystem(mLoginSession.id
                    ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), observer)


        }
    }

    fun combineServerLocalUserAndSort(users: List<OneOSUser>?, sharedUserList: SharedUserList, mLoginSession: LoginSession): MutableList<OneOSUser>? {
        var oneOSUsers: MutableList<OneOSUser>? = mUserList.value?.toMutableList()
        oneOSUsers?.clear()

        val newUsers = ArrayList<OneOSUser>()
//        if (null != users) {

        for (user in sharedUserList.users) {
            //过滤掉未通过验证的用户
            if (user.mgrlevel.toString() == MGR_LEVEL.UNCONFIRMED)
                continue
            if (user != null && !TextUtils.isEmpty(user.username)) {
                var isFound = false
                if (users != null) {
                    for (oneOSUser in users) {
                        if (user.username == oneOSUser.name) {
                            oneOSUser.isRemote = true
                            oneOSUser.type = oneOSUser.type or OneOSUser.TYPE_REMOTE
                            isFound = true
                            if (newUsers.contains(oneOSUser)) {
                                //已包含
                                break
                            }
                            //只添加服务器中存在绑定关系的用户
                            //                                            int isAdmin = user.mgrlevel != 0 && user.mgrlevel != 1 ? 0 : 1;
                            oneOSUser.isAdmin = user.mgrlevel
                            if (TextUtils.isEmpty(oneOSUser.markName)) {
                                oneOSUser.markName = user.fullName
                            }
                            user.devMarkName = oneOSUser.markName
                            when (user.mgrlevel) {
                                0 -> newUsers.add(0, oneOSUser)
                                1 -> newUsers.add(if (newUsers.size > 0) 1 else 0, oneOSUser)
                                2 -> newUsers.add(oneOSUser)
                            }
                            if (user.username != mLoginSession.userInfo!!.name) {
                                val info = FriendItem()
                                info.username = user.username
                                info.phone = user.phone
                                info.nickname = oneOSUser.markName
                                FriendsKeeper.addFriendOrUpdate(info)
                            }

                            break
                        }
                    }
                }
                if (!isFound) {
                    val oneOSUser = OneOSUser(user.username, -1, -1, user.mgrlevel, user.fullName)
                    oneOSUser.isRemote = true
                    oneOSUser.type = /*oneOSUser.type or*/ OneOSUser.TYPE_REMOTE
                    newUsers.add(oneOSUser)
                }

            }
        }
        if (users != null) {
            for (user in users) {
                if (newUsers.contains(user)) continue
                newUsers.add(user)
            }
        }

//        }
        if (oneOSUsers == null) {
            oneOSUsers = newUsers
        } else {
            oneOSUsers.addAll(newUsers)
        }
        return oneOSUsers
    }

    fun getUserSpace(devId: String, user: OneOSUser, mLoginSession: LoginSession, total: Long) {
        val startOldRequest = {
            val spaceAPI = OneOSSpaceAPI(mLoginSession)
            spaceAPI.setOnSpaceListener(object : OneOSSpaceAPI.OnSpaceListener {
                override fun onStart(url: String) {

                }

                override fun onSuccess(url: String, isOneOSSpace: Boolean, hd1: OneOSHardDisk, hd2: OneOSHardDisk?) {
                    if (hd1.total > 0)
                        user.space = hd1.total
                    else
                        user.space = total
                    user.used = hd1.used
                    mUserList.postValue(mUserList.value)
                }

                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                    user.space = total
                    user.used = 0
                    mUserList.postValue(mUserList.value)
                }
            })
            if (user.name != null) {
                spaceAPI.query(user.name!!)
            }
        }
        val observer = object : V5Observer<UserSpace>(mLoginSession.id ?: "") {
            override fun success(result: BaseProtocol<UserSpace>) {
                val space = result.data?.space ?: 0L
                if (space == 0L) user.space = total
                //V5 space MB单位 非V5 space Byte 单位
                //used Byte 单位
                else user.space = space * 1024 * 1024 * 1024
                user.used = result.data?.used ?: 0
                mUserList.postValue(mUserList.value)
            }

            override fun fail(result: BaseProtocol<UserSpace>) {
                user.space = total
                user.used = 0
                mUserList.postValue(mUserList.value)
            }

            override fun isNotV5() {//调用旧接口
                startOldRequest()
            }

            override fun retry(): Boolean {
//                V5Repository.INSTANCE().getUserSpace(mLoginSession.id?:"", mLoginSession.ip, LoginTokenUtil.getToken(), user.name
//                        ?: "", this)
                return false
            }
        }
        V5Repository.INSTANCE().getUserSpace(mLoginSession.id
                ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.name
                ?: "", observer)

    }

    fun removeByName(delUid: String?) {
        mUserList.value?.let {
            val toMutableList = it.toMutableList()
            val iterator = toMutableList.iterator()
            while (iterator.hasNext()) {
                val osUser = iterator.next()
                if (osUser.name == delUid) {
                    iterator.remove()
                    break
                }
            }
            mUserList.postValue(toMutableList)
        }
    }

    fun getServerUserId(username: String?): String? {
        if (username.isNullOrEmpty()) return null
        val serverUserList = mServerUserList.value ?: return null
        for (user in serverUserList.users) {
            var loginName = user.username
            if (TextUtils.isEmpty(loginName))
                loginName = user.phone
            if (username == loginName) {
                return user.userid
            }
        }
        return null
    }

    fun getOneOsUser(shareUser: ShareUser): OneOSUser? {
        return mUserList.value?.find { shareUser.username == it.name }?.apply {
            isCurrent = shareUser.isCurrent
        }
    }
}
