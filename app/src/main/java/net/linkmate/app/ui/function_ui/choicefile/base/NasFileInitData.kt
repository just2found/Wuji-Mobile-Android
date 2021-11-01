package net.linkmate.app.ui.function_ui.choicefile.base

/**
create by: 86136
create time: 2021/5/11 16:52
Function description:
 */

interface NasFileInitData {
    fun getDeviceId(): String
    fun getOneOSFilterType(): OneOSFilterType //筛选出来的的文件类型
    fun addFolderAble(): Boolean//是否可以新建文件夹
    fun optionalFolderAble(): Boolean//文件夹是否可以被选中,不可选中则不展示选中按钮   当有文件被选中的时候，点击文件夹，是触发选中(true),还是跳转(false)
    fun getMaxNum(): Int //可以选中几个文件
    fun getInitPath(): String?//初始化时候的默认路径
    fun getInitPathType(): Int? //初始化时候的默认类型
    fun getRootPathType(): Int  //根节点显示的空间类型
    fun showSplitLine(): Boolean  //是否显示分割县
    fun getNoDataTips(): String?  //当没有数据的时候的提示信息

    fun getGroupId(): Long? { //获取群组空间的群ID
        return null
    }
}