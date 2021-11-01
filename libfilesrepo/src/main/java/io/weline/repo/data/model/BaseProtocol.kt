package io.weline.repo.data.model

import androidx.annotation.Keep

/**
 * @author Raleigh.Luo
 * date：20/9/17 14
 * describe：
 */
@Keep
data class BaseProtocol<T> (var result:Boolean = false, var error:Error?, var data:T?) {

}

@Keep
data class Error(val code:Int = 0, val msg:String?)