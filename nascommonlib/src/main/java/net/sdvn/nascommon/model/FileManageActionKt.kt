package net.sdvn.nascommon.repository

import net.sdvn.nascommon.model.FileManageAction
import java.util.*

/** 

Created by admin on 2020/7/16,13:56

 */
fun FileManageAction.getActionName(): String {
    return name.toLowerCase(Locale.ENGLISH)
}