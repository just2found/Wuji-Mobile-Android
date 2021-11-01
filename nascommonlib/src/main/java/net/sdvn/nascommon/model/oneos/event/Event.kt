package net.sdvn.nascommon.model.oneos.event

import androidx.annotation.Keep

@Keep
open class Event {
    var devId: String? = null
    /**
     * action : download
     * percent : 100
     * user : all
     * channel : sys
     * name : upgrade
     */

    var action: String? = null
    var user: String? = null
    var channel: String? = null
}