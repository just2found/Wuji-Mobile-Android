package net.linkmate.app.base

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import libs.source.common.AppExecutors
import libs.source.common.livedata.*

abstract class NetworkBoundResource<ResultType>
@MainThread constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        fetchFromNetwork()

    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private fun fetchFromNetwork() {
        val apiResponse = createCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        val responseBody = processResponse(response)
                        val error = checkResponseFailed(responseBody)
                        error?.let {//返回错误信息，业务返回请求失败
                            onFetchFailed()
                            appExecutors.mainThread().execute {
                                setValue(it)
                            }
                        } ?: let {//请求成功
                            //存到数据库中
                            saveCallResult(responseBody)
                            appExecutors.mainThread().execute {
                                // we specially request a new live data,
                                // otherwise we will get immediately last cached value,
                                // which may not be updated with latest results received from network.
                                setValue(Resource.success(responseBody))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        setValue(Resource.success(null))
                    }
                }
                is ApiErrorResponse -> {
                    onFetchFailed()
                    appExecutors.mainThread().execute {
                        setValue(Resource.error(response.errorMessage, null, response.code))
                    }

                }
            }
        }
    }

    /**
     * 检查是否请求失败（业务上）,
     * 可重写自定义，默认暂无处理
     * @return 失败就返回错误对象，成功返回null
     */
    @WorkerThread
    protected open fun checkResponseFailed(response: ResultType): Resource<ResultType>? {
//        Resource.error("", null, code)
        return null
    }

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>


    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<ResultType>) = response.body

    @WorkerThread
    protected open fun saveCallResult(item: ResultType) {
    }

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<ResultType>>
}
