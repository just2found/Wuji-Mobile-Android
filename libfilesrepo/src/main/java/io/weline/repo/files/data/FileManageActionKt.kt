package io.weline.repo.files.data

import java.util.*

/** 

Created by admin on 2020/7/16,13:56

 */
fun FileManageAction.getActionName(): String {
    return name.toLowerCase(Locale.ENGLISH)
}