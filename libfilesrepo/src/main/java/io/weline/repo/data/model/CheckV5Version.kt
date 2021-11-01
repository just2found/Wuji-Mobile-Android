package io.weline.repo.data.model

import androidx.annotation.Keep

/**
 * @author Raleigh.Luo
 * date：20/9/18 15
 * describe：
 */
@Keep
data class CheckV5Version(val status: Int = 0, val result: Result?) {
    fun isV5(): Boolean {
        var isV5 = false
        result?.version?.let {
            //版本是2.0 如2.0.1.202009162049
//            isV5 = "2.1.1" == it.substring(0,5)
            //2.0.1
            val dot1 = 2
            val dot2 = 1
            val dot3 = 1
            val getInt: (Int) -> Int = { positon ->
                result.version.get(positon).toString().toInt()
            }
            isV5 = getInt(0) > dot1 ||
                    (getInt(0) == dot1 && getInt(2) > dot2) ||
                    (getInt(0) == dot1 && getInt(2) == dot2 && getInt(4) >= dot3)
        }
        return isV5
    }
}

@Keep
data class Result(val version: String)