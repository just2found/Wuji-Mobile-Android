package libs.source.common.livedata

import libs.source.common.livedata.Status.*

data class Resource<out T>(val status: Status, val data: T?, val message: String?, val code: Int? = null) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(SUCCESS, data, null, null)
        }

        fun <T> error( msg: String?, data: T?,code: Int? = ApiHTTPErrNO.STATUS_CODE_THROWABLE): Resource<T> {
            return Resource(ERROR, data, msg, code)
        }

        fun <T> loading(data: T?=null): Resource<T> {
            return Resource(LOADING, data, null, null)
        }

        fun <T> loading(data: T?, msg: String?): Resource<T> {
            return Resource(LOADING, data, msg, null)
        }
    }
}