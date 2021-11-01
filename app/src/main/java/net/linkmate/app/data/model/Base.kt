package net.linkmate.app.data.model

import androidx.annotation.Keep
import libs.source.common.livedata.Resource

/**
 * @author Raleigh.Luo
 * date：20/12/25 11
 * describe：
 */
@Keep
open class Base {
    open var code: Int? = null
    open var msg: String? = null

    /**
     * 处理业务返回错误码
     */
    fun <T> checkResponseFailed(response: Base): Resource<T>? {
        if(response.code == 200){
            return null
        }else{
            return Resource.error(response.msg?:"",null,response.code?:0)
        }
    }
}