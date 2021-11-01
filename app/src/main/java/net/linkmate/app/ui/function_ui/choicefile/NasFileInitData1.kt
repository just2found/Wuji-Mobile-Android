package net.linkmate.app.ui.function_ui.choicefile

import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_ALL
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileInitData
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType

/**
 *如果 mRootPathType 使用则  mInitPath  mInitPathType 必须为空
 */
data class NasFileInitData1(
    private val mDeviceId: String,//设备ID
    val mTargetSharePathType: Int, //存放目标的类型
    val mTargetSharePath: String, //存放目标的路径
    val mFragmentTitle: String, //标题头
    private val mOneOSFilterType: OneOSFilterType = OneOSFilterType.ALL, //筛选出来的的文件类型
    private val mAddFolderAble: Boolean = false,//是否可以新建文件夹
    private val mOptionalFolderAble: Boolean = true,//文件夹是否可以被选中,不可选中则不展示选中按钮
    private val mMaxNum: Int = Int.MAX_VALUE, //可以选中几个文件
    private val mInitPath: String? = null,//初始化时候的默认路径
    private val mInitPathType: Int? = null, //初始化时候的默认类型
    private val mRootPathType: Int = CONTAIN_ALL, //根节点显示的空间类型
    private val mRNoDataTips: String? = null,//当没有数据的时候的提示信息
    private val mGroupId: Long? = null

) : NasFileInitData {

    override fun getDeviceId(): String {
        return mDeviceId
    }

    override fun getOneOSFilterType(): OneOSFilterType {
        return mOneOSFilterType
    }

    override fun addFolderAble(): Boolean {
        return mAddFolderAble
    }

    override fun optionalFolderAble(): Boolean {
        return mOptionalFolderAble
    }

    override fun getMaxNum(): Int {
        return mMaxNum
    }

    override fun getInitPath(): String? {
        return mInitPath
    }

    override fun getInitPathType(): Int? {
        return mInitPathType
    }

    override fun getRootPathType(): Int {
        return mRootPathType
    }

    override fun showSplitLine(): Boolean {
        return false
    }

    override fun getNoDataTips(): String? {
        return   mRNoDataTips
    }

    override fun getGroupId(): Long? {
        return mGroupId
    }

}
