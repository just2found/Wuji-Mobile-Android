package io.weline.repo.net

import androidx.annotation.Keep

/**
 * @author Raleigh.Luo
 * date：20/9/17 14
 * describe：
 */
@Keep
open abstract class RealV5Observer<T>(private val devId: String) : V5Observer<T>(devId) {
    open override fun isNotV5() {

    };
}