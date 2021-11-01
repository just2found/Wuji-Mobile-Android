package net.linkmate.app.data.user

import androidx.lifecycle.LiveData
import net.sdvn.common.internet.protocol.GetUserInfoResultBean
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.GsonUtils

class UserDataSource {
    companion object {
        const val PREFIX_USER_INFO = "user_info_"
    }

    fun getUserInfoByDB(userId: String): LiveData<GetUserInfoResultBean?> {
        return FileUtils.loadDiskCache<GetUserInfoResultBean>(PREFIX_USER_INFO + userId)
    }

    fun saveUserInfo(userId: String, item: GetUserInfoResultBean) {
        FileUtils.putDiskCache(PREFIX_USER_INFO + userId, GsonUtils.encodeJSON(item));
    }

}