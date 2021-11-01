package net.linkmate.app.ui.simplestyle.device.remove_duplicate.data

import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/2/3 15:39
Function description:
 */

data class RdTaskStatusResult(val total: Int, val page: Int, val pages: Int, val tasks: List<DuplicateFileTask>) {
    data class DuplicateFileTask(val taskId: Long, val uid: Long,
                                 val taskType: Int, val status: Int,
                                 val percent: String
    )
}


