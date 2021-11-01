package net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter

import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupInfo

interface OnSelectChangeListener {

    fun onSelectFile(size:Long)

    fun onDeselectFile(size:Long)
}