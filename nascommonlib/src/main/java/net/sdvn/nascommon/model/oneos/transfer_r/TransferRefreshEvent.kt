package net.sdvn.nascommon.model.oneos.transfer_r

import net.sdvn.nascommon.model.oneos.transfer.TransferElement

data class TransferRefreshEvent<T : TransferElement>(val refreshType: Int, val transferElement: T)
