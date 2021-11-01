package net.linkmate.app.ui.function_ui.choicefile.base

/**
create by: 86136
create time: 2021/5/11 17:02
Function description:
 */

object NasFileConstant {

    const val CONTAIN_USER = 1
    const val CONTAIN_PUBLIC = 2
    const val CONTAIN_SAFE = 4
    const val CONTAIN_EXT_STORAGE = 8
    const val CONTAIN_GROUP = 16

    const val CONTAIN_ALL = CONTAIN_USER or CONTAIN_PUBLIC or CONTAIN_SAFE
}