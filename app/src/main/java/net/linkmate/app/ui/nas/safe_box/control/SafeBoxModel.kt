package net.linkmate.app.ui.nas.safe_box.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.weline.repo.data.model.SafeBoxCheckData
import io.weline.repo.data.model.SafeBoxStatus
import io.weline.repo.net.V5ObserverImpl
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession

/**
create by: 86136
create time: 2021/4/1 14:02
Function description:
 */

class SafeBoxModel : ViewModel() {


    companion object {
        const val SAFE_BOX_TYPE_KEY = "safe_box_type_key"

        //这三个是Activity用的
        const val LOGIN_TYPE = 0;//登录的流程控制
        const val INITIALIZATION = 1;//初始话
        const val CONTROL = 2;//设置中心

        //这二个是FRAGMENT用的
        const val RESET_PASSWORD = 3;//重置密码
        const val RESET_QUESTION = 4;//重置密保

        const val CLOSE_ACTIVITY = 444
    }


    private fun <T> getLoginSession(liveData: MutableLiveData<Resource<T>>, devID: String, next: (loginSession: LoginSession) -> Unit) {
        SessionManager.getInstance().getLoginSession(devID,
                object : GetSessionListener(false) {
                    override fun onSuccess(url: String, data: LoginSession) {
                        next(data)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        liveData.postValue(Resource.error("", null, errorNo))
                    }
                })
    }


    fun querySafeBoxStatus(devID: String): LiveData<Resource<SafeBoxStatus>> {
        val liveData = MutableLiveData<Resource<SafeBoxStatus>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().querySafeBoxStatus(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), v5ObserverImpl)
        }
        return liveData
    }


    fun initSafeBoxStatus(devID: String, newQuestion: String, newAnswer: String, newKey: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().initSafeBoxStatus(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), newQuestion, newAnswer, newKey, v5ObserverImpl)
        }
        return liveData
    }


    fun unlockSafeBoxStatus(devID: String, oldKey: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().unlockSafeBoxStatus(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), oldKey, v5ObserverImpl)
        }
        return liveData
    }

    fun lockSafeBoxStatus(devID: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().lockSafeBoxStatus(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), v5ObserverImpl)
        }
        return liveData
    }


    fun resetSafeBoxByOldKey(devID: String, ranStr: String, newKey: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().resetSafeBoxByOldKey(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), ranStr, newKey, v5ObserverImpl)
        }
        return liveData
    }

    fun resetSafeBoxQuestion(devID: String, trans: String, newQuestion: String, newAnswer: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().resetSafeBoxQuestion(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), trans, newQuestion, newAnswer, v5ObserverImpl)
        }
        return liveData
    }


    fun checkOldPsw(devID: String, oldPsw: String): LiveData<Resource<SafeBoxCheckData>> {
        val liveData = MutableLiveData<Resource<SafeBoxCheckData>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl<SafeBoxCheckData>(devID, liveData)
            V5Repository.INSTANCE().checkSafeBoxOldPsw(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), oldPsw, v5ObserverImpl)
        }
        return liveData
    }

    fun checkOldAnswer(devID: String, oldAnswer: String): LiveData<Resource<SafeBoxCheckData>> {
        val liveData = MutableLiveData<Resource<SafeBoxCheckData>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl<SafeBoxCheckData>(devID, liveData)
            V5Repository.INSTANCE().checkSafeBoxOldAnswer(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), oldAnswer, v5ObserverImpl)
        }
        return liveData
    }


    fun resetSafeBoxAll(devID: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().resetSafeBox(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), v5ObserverImpl)
        }
        return liveData
    }


}