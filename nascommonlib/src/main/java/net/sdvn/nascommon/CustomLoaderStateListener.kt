package net.sdvn.nascommon

import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.nascommon.model.oneos.api.BaseAPI

/**
 * @author Raleigh.Luo
 * date：21/4/8 14
 * describe：
 */
interface CustomLoaderStateListener: HttpLoader.HttpLoaderStateListener {
    fun onLoadStart(http: BaseAPI)
    fun isCanceled(): Boolean
}