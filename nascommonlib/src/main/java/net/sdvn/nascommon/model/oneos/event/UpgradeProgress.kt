package net.sdvn.nascommon.model.oneos.event

import androidx.annotation.Keep


@Keep
class UpgradeProgress : Event() {

    /**
     * action : download
     * percent : 100
     * user : all
     * channel : sys
     * name : upgrade
     */
    var percent: Int = 0
    var name: String? = null
}


