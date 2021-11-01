package io.weline.repo.api

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/5
 */
object GroupUserPerm {
    // 群组空间文件操作权限，511为具备所有权限(默认群主所有)
    // 群主转移群主身份后，自身依旧维持511权限
    const val PERM_VIEW = 1        // 可浏览文件
    const val PERM_DOWN = 2        // 可下载文件
    const val PERM_OPEN = 4        // 可打开文件
    const val PERM_MKDIR = 8        // 可创建目录
    const val PERM_COPY = 16    // 可拷贝文件
    const val PERM_UPLOAD = 32    // 可上传文件
    const val PERM_DELETE = 64    // 可删除文件
    const val PERM_RENAME = 128    // 可重命名文件
    const val PERM_MOVE = 256    // 可移动文件
    const val PERM_RECYCLE = 512    // 可操作回收站
    const val PERM_ALL = 1023    // 全部权限

    const val PERM_VIEW_GROUP = PERM_VIEW or PERM_DOWN or PERM_OPEN or PERM_COPY
    const val PERM_CREATE_GROUP = PERM_MKDIR or PERM_COPY or PERM_UPLOAD
    const val PERM_MANAGE_GROUP = PERM_DELETE or PERM_RENAME or PERM_MOVE or PERM_RECYCLE

    fun isUploadEnable(perm: Int): Boolean {
        return perm.and(PERM_CREATE_GROUP) == PERM_CREATE_GROUP
    }

    fun isManageEnable(perm: Int): Boolean {
        return perm.and(PERM_MANAGE_GROUP) == PERM_MANAGE_GROUP
    }

    fun switchUploadEnable(perm: Int, isEnable: Boolean): Int {
        return if (isEnable) {
            perm.or(PERM_MKDIR or PERM_UPLOAD)
        } else {
            perm.xor(PERM_MKDIR or PERM_UPLOAD)
        }
    }

    fun switchManagementEnable(perm: Int, isEnable: Boolean): Int {
        return if (isEnable) {
            perm.or(PERM_DELETE or PERM_RENAME or PERM_MOVE or PERM_RECYCLE)
        } else {
            perm.xor(PERM_DELETE or PERM_RENAME or PERM_MOVE or PERM_RECYCLE)
        }
    }
}

object GroupAction {
    const val ACTION_DELETE = "delete"
    const val ACTION_LEAVE = "leave"
    const val ACTION_TRANSFER = "transfer"
    const val ACTION_RENAME = "rename"
    const val ACTION_DELETE_USER = "remove_user"
    const val ACTION_ADD_USER = "add_group_user"
    const val ACTION_USER_MARKNAME = "set_user_markname"
    const val ACTION_USER_PERMISSION = "permission"
}