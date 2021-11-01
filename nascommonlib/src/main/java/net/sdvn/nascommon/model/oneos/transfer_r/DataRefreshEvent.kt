package net.sdvn.nascommon.model.oneos.transfer_r

data class DataRefreshEvent(val dataVersion: Int = -1, var refreshType: Int, var startPosition: Int, var itemCount: Int = 1, var keyStr: String? = null)