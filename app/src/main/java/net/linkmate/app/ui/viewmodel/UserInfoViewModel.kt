package net.linkmate.app.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import libs.source.common.livedata.Status
import net.linkmate.app.data.user.UserDataSource
import net.linkmate.app.data.user.UserRepository
import net.sdvn.common.internet.protocol.GetUserInfoResultBean
import net.sdvn.nascommon.LibApp

class UserInfoViewModel : ViewModel() {
    private var userRepository: UserRepository = UserRepository(LibApp.instance.getAppExecutors(), userDataSource = UserDataSource())
    private val _userInfoLiveData = MutableLiveData<GetUserInfoResultBean>()
    val userInfoLiveData = _userInfoLiveData
    fun loadUserInfo(userId: String, ticket: String) {
        val loadUserInfo = userRepository.loadUserInfo(userId, ticket)
        if (loadUserInfo.value?.status == Status.SUCCESS) {
            _userInfoLiveData.postValue(loadUserInfo.value?.data)
        }
    }
}