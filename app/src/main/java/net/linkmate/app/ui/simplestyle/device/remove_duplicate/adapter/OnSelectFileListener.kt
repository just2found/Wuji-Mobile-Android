package net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter

import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupInfo

interface OnSelectFileListener {

    fun onSelectFileChange(dupInfo: DupInfo, selected: Boolean): Boolean

    fun hasSelect(dupInfo: DupInfo): Boolean
}