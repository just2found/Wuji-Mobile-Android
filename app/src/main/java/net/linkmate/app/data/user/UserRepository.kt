package net.linkmate.app.data.user

import androidx.lifecycle.LiveData
import libs.source.common.AppExecutors
import libs.source.common.livedata.ApiResponse
import libs.source.common.livedata.Resource
import libs.source.common.utils.RateLimiter
import net.linkmate.app.data.V2AgApiService
import net.sdvn.common.internet.core.V1AgApiHttpLoader
import net.sdvn.common.internet.protocol.GetUserInfoResultBean
import net.sdvn.nascommon.repository.base.NetworkBoundResource
import java.util.concurrent.TimeUnit

class UserRepository(private val appExecutors: AppExecutors, private val userDataSource: UserDataSource) {
    private val repoListRateLimit = RateLimiter<String>(2, TimeUnit.SECONDS)
    fun loadUserInfo(userId: String, ticket: Any): LiveData<Resource<GetUserInfoResultBean>> {
        return object : NetworkBoundResource<GetUserInfoResultBean, GetUserInfoResultBean>(appExecutors) {
            override fun saveCallResult(item: GetUserInfoResultBean) {
                if (item.isSuccessful)
                    userDataSource.saveUserInfo(userId, item)
            }

            override fun shouldFetch(data: GetUserInfoResultBean?): Boolean {
                return data == null || !data.isSuccessful()|| repoListRateLimit.shouldFetch(userId)
            }

            override fun loadFromDb(): LiveData<GetUserInfoResultBean?> {
                return userDataSource.getUserInfoByDB(userId)
            }

            override fun createCall(): LiveData<ApiResponse<GetUserInfoResultBean>> {
                return V2AgApiService.build().getUserInfo(V1AgApiHttpLoader.getMap(), hashMapOf(Pair("ticket", ticket)))
            }

        }.asLiveData()
    }
}