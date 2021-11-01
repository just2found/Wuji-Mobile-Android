package net.linkmate.app.ui.simplestyle.dynamic.delegate.db

import net.sdvn.common.vo.DynamicRelated

/**
 * @author Raleigh.Luo
 * date：21/2/4 11
 * describe：
 */
class RelatedDelegateImpl : DBDelegete<DynamicRelated>() {
    override fun save(networkId: String, deviceId: String, data: List<DynamicRelated>?) {
        data?.let {
            val list = it
            getBoxStore()?.runInTx(Runnable {
                list.forEach {
                    it.init(networkId, deviceId)
                }
                getBoxStore()?.boxFor(DynamicRelated::class.java)?.put(list)
            })
        }
    }
}