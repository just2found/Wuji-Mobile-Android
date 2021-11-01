package io.weline.repo.api

/** 

Created by admin on 2020/10/29,09:09
//    重名选项：0 自定重命名（默认）
//    ，1 覆盖(当重名时，处理 文件夹->文件夹为文件夹合并；文件->文件为覆盖；文件->文件夹和文件夹->文件为删除目标对象并拷贝源对象)，
//    -1 不覆盖（跳过）
//    2 重名时信息给用户选择
 */
object DefCopyOrMoveAction {
    const val ACTION_DEFAULT = 0
    const val ACTION_REPLACE = 1
    const val ACTION_SKIP = -1
    const val ACTION_ASK = 2
}

object DefSyncAction {
    const val SYNC = 0
    const val ASYNC = 1
}