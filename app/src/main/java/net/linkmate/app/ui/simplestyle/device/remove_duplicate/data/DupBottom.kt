package net.linkmate.app.ui.simplestyle.device.remove_duplicate.data

import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.OnSelectChangeListener
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.OnSelectFileListener
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdSelectAdapter

/**
create by: 86136
create time: 2021/1/31 23:03
Function description:
 */

class DupBottom(val pathList: MutableList<DupInfo>, val dupHead: DupHead, val onSelectChangeListener: OnSelectChangeListener) : MultiItemEntity, OnSelectFileListener {

    val selectPathList = mutableListOf<DupInfo>()


    override fun getItemType(): Int {
        return RdSelectAdapter.BOTTOM
    }


    override fun onSelectFileChange(dupInfo: DupInfo, selected: Boolean): Boolean {
        var flag = false
        if (selected) {
            if (!selectPathList.contains(dupInfo)) {
                selectPathList.add(dupInfo)
                onSelectChangeListener.onSelectFile(dupInfo.size)
            }
        } else {
            flag = selectPathList.remove(dupInfo)
            if (flag) {
                onSelectChangeListener.onDeselectFile(dupInfo.size)
            }
        }
        return flag
    }

    override fun hasSelect(dupInfo: DupInfo): Boolean {
        return selectPathList.contains(dupInfo)
    }


    fun selectLongerPath() {
        selectPathList.clear()
        var temporary: DupInfo? = null
        pathList.forEach { dupInfo ->
            temporary = temporary?.let {
                if (it.path.length > dupInfo.path.length) {
                    selectPathList.add(it)
                    dupInfo
                } else {
                    selectPathList.add(dupInfo)
                    it
                }
            } ?: dupInfo
        }
    }

    fun selectLongerName() {
        selectPathList.clear()
        var temporary: DupInfo? = null
        pathList.forEach { dupInfo ->
            temporary = temporary?.let {
                if (it.name.length > dupInfo.name.length) {
                    selectPathList.add(it)
                    dupInfo
                } else {
                    selectPathList.add(dupInfo)
                    it
                }
            } ?: dupInfo
        }
    }

    fun selectShorterPath() {
        selectPathList.clear()
        var temporary: DupInfo? = null
        pathList.forEach { dupInfo ->
            temporary = temporary?.let {
                if (it.path.length < dupInfo.path.length) {
                    selectPathList.add(it)
                    dupInfo
                } else {
                    selectPathList.add(dupInfo)
                    it
                }
            } ?: dupInfo
        }
    }

    fun selectShorterName() {
        selectPathList.clear()
        var temporary: DupInfo? = null
        pathList.forEach { dupInfo ->
            temporary = temporary?.let {
                if (it.name.length < dupInfo.name.length) {
                    selectPathList.add(it)
                    dupInfo
                } else {
                    selectPathList.add(dupInfo)
                    it
                }
            } ?: dupInfo
        }
    }


    fun selectEarlierTime() {
        selectPathList.clear()
        var temporary: DupInfo? = null
        pathList.forEach { dupInfo ->
            temporary = temporary?.let { temporary ->
                if (temporary.time < dupInfo.time) {
                    selectPathList.add(temporary)
                    dupInfo
                } else {
                    selectPathList.add(dupInfo)
                    temporary
                }
            } ?: dupInfo
        }
    }


    fun selectLaterTime() {
        selectPathList.clear()
        var temporary: DupInfo? = null
        pathList.forEach { dupInfo ->
            temporary = temporary?.let { temporary ->
                if (temporary.time > dupInfo.time) {
                    selectPathList.add(temporary)
                    dupInfo
                } else {
                    selectPathList.add(dupInfo)
                    temporary
                }
            } ?: dupInfo
        }
    }


}