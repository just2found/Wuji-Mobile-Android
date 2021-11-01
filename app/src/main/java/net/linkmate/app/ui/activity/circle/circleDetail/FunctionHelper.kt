package net.linkmate.app.ui.activity.circle.circleDetail

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.*
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.detail.CircleDetialAdapter
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.device.*
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee.*
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.manager.CircleManagerAdapter
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.member.CircleMemberAdapter
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.member.CircleMemberDetialAdapter
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.member.CircleMemberDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.member.CircleMemberViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.setting.CircleSettingAdapter
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.setting.CircleSettingViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.share.CircleShareAdapter
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.share.CircleShareViewModel

/**
 * @author Raleigh.Luo
 * date：20/7/23 17
 * describe：
 */
object FunctionHelper {
    const val FUNCTION = "function"
    const val EXTRA_ENTITY = "extra_entity"
    const val POSITION = "position"
    const val EXTRA = "extra"
    const val NETWORK_ID = "network_id"

    /*---圈子管理------------------------------------------------------------------------*/
    //圈子管理
    const val CIRCLE_MANANGER = 0x000

    //圈子管理－详情
    const val CIRCLE_DETAIL = 0x001

    //圈子管理－分享
    const val CIRCLE_SHARE = 0x002

    //圈子管理－成员管理
    const val CIRCLE_MEMBER = 0x003

    //EN设备管理
    const val CIRCLE_EN_DEVICE = 0x004

    //查看收益
    const val CIRCLE_BENEFITS = 0x005

    //设置
    const val CIRCLE_SETTING = 0x006

    //设置主EN设备
    const val CIRCLE_SETTING_MAIN_EN_DEVICE = 0x007

    //设置收费项
    const val CIRCLE_SETTING_JOIN_WAY = 0x008

    //退出圈子
    const val CIRCLE_EXIT = 0x009

    //我的圈内设备
    const val CIRCLE_OWN_DEVICE = 0x010

    //退出圈子
    const val CIRCLE_ALTER_NAME = 0x011

    //圈子简介
    const val CIRCLE_BRIEF = 0x012

    /*---非圈子管理------------------------------------------------------------------------*/
    //与圈子管理界限,用于处理onBackPressed的返回事件
    const val CIRCLE_MANAGER_LIMIT = 0x1000

    //选择en设备
    const val SELECT_EN_DEVICE = 0x1001

    //选择我的设备
    const val SELECT_OWN_DEVICE = 0x1002

    //选择圈子类型
    const val SELECT_CIRCLE_TYPE = 0x1003

    //我的付费项
    const val CIRCLE_OWN_FEE_RECORDS = 0x1004

    //购买流量
    const val CIRCLE_PURCHASE_FLOW = 0x1005

    //设置EN服务器增值费抽成
    const val CIRCLE_EN_SERVER_FEES = 0x1006

    /*----二级弹框-----------------------------------------------------------------------*/

    //加入方式详情 二级弹框
    const val CIRCLE_SETTING_FEES_DETIAL = 0x2001

    //en设备详情 二级弹框
    const val CIRCLE_EN_DEVICE_DETAIL = 0x2002

    //成员管理 二级弹框
    const val CIRCLE_MEMBER_DETAIL = 0x2003

    //成员管理 删除
    const val CIRCLE_MEMBER_DELETE = 0x2004

    //成员管理 移交圈子所有权
    const val CIRCLE_MEMBER_TRANSFER_OWNER = 0x2005
    //成员管理 提升为管理员
    const val CIRCLE_MEMBER_TRANSFOR_MANAGE = 0x2006

    //成员管理  降级为普通用户
    const val CIRCLE_MEMBER_TRANSFOR_MEMBER = 0x2007

    //选择加入方式
    const val CIRCLE_SELECT_JOIN_WAY = 0x2008

    //我的圈内设备详情
    const val CIRCLE_OWN_DEVICE_DETAIL = 0x2009

    //取消EN服务器
    const val CANCEL_EN_SERVER = 0x2010

    /**
     * 获取设备菜单-图标／标题
     */
    fun getMenu(function: Int): DetailMenu {
        var detailMenu = DetailMenu()
        detailMenu.function = function
        when (function) {
            CIRCLE_DETAIL -> { //详情
                detailMenu.icon = R.drawable.ic_device_1
                detailMenu.title = getString(R.string.details_file)
            }
            CIRCLE_EN_DEVICE -> {//EN设备管理
                detailMenu.icon = R.drawable.ic_en_server_manager
                detailMenu.title = getString(R.string.en_device_manager)
            }
            CIRCLE_SHARE -> { //分享设备
                detailMenu.icon = R.drawable.ic_device_2
                detailMenu.title = getString(R.string.circle_share)
            }
            CIRCLE_BENEFITS -> { //查看受益
                detailMenu.icon = R.drawable.icon_me_dev_traffic
                detailMenu.title = getString(R.string.view_benefits)
            }
            CIRCLE_SETTING -> { //设置
                detailMenu.icon = R.drawable.ic_device_11
                detailMenu.title = getString(R.string.settings)
            }
            CIRCLE_MEMBER -> {//成员管理
                detailMenu.icon = R.drawable.ic_device_3
                detailMenu.title = getString(R.string.members_mng)
            }
            CIRCLE_SETTING_MAIN_EN_DEVICE -> {
                detailMenu.icon = R.drawable.ic_en_server_setting
                detailMenu.title = getString(R.string.setting_main_en_device)
            }
            CIRCLE_ALTER_NAME -> {
                detailMenu.icon = R.drawable.icon_edit_dev_name
                detailMenu.title = getString(R.string.alter_circle_name)
            }
            CIRCLE_SETTING_JOIN_WAY -> {
                detailMenu.icon = R.drawable.ic_set_charge_items
                detailMenu.title = getString(R.string.settting_join_way)
            }
            CIRCLE_EXIT -> {
                detailMenu.icon = R.drawable.ic_device_10
                detailMenu.title = getString(R.string.exit_circle)
            }
            CIRCLE_MEMBER_DELETE -> {
                detailMenu.icon = R.drawable.ic_device_10
                detailMenu.title = getString(R.string.delete)
            }
            CIRCLE_OWN_DEVICE -> {
                detailMenu.icon = R.drawable.icon_me_devmng
                detailMenu.title = getString(R.string.circle_own_device)
            }
            CIRCLE_OWN_FEE_RECORDS ->{
                detailMenu.icon = R.drawable.ic_my_paid_item
                detailMenu.title = getString(R.string.own_fee_records)
            }
            CIRCLE_PURCHASE_FLOW ->{
                detailMenu.icon = R.drawable.ic_buy_traffic
                detailMenu.title = getString(R.string.purchase_flow_fee_ways)
            }
            CIRCLE_EN_SERVER_FEES ->{
                detailMenu.icon = R.drawable.ic_set_en_commission
                detailMenu.title = getString(R.string.setting_en_setting_fees)
            }
            CIRCLE_BRIEF ->{
                detailMenu.icon = R.drawable.ic_summary
                detailMenu.title = getString(R.string.summary)
            }

        }
        return detailMenu
    }

    /**
     * 构建适配器，底部状态显示
     */
    fun initFragmentPanel(function: Int,
                          context: Fragment,
                          viewModel: CircleDetialViewModel,
                          navController: NavController): DialogBaseAdapter<CircleFragmentViewModel> {

        //底部按钮参数
        val params = ViewStatusParams()
        //适配器
        var adapter: DialogBaseAdapter<CircleFragmentViewModel>
        when (function) {
            SELECT_EN_DEVICE -> {//选择EN设备
                val fragmentViewModel: CircleSelectDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.add_en_server)
                params.headerDescribe = getString(R.string.select_own_en_device)
                params.headBackButtonVisibility = View.GONE
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.confirm)
                params.bottomIsEnable = false
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSelectDeviceAdapter(context, viewModel, fragmentViewModel)
            }
            SELECT_OWN_DEVICE -> {//选择我的设备
                val fragmentViewModel: CircleSelectDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.add_circle_new_device)
                params.headerDescribe = getString(R.string.select_own_device)
                params.headBackButtonVisibility = View.GONE
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.confirm)
                params.bottomIsEnable = false
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSelectDeviceAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_SETTING_MAIN_EN_DEVICE -> {//设置主EN设备
                val fragmentViewModel: CircleSetMainENDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)
                params.headerTitle = getString(R.string.setting_main_en_device)
                params.headerDescribe = getString(R.string.set_main_en_server_hint)
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.confirm)
                params.bottomIsEnable = false
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSetMainENDeviceAdapter(context, viewModel, fragmentViewModel)
            }
            SELECT_CIRCLE_TYPE -> {//选择圈子类型
                val fragmentViewModel: CircleSelectTypeViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.create_circle)
                params.headerDescribe = getString(R.string.select_circle_type)
                
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.confirm)
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSelectTypeAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_SELECT_JOIN_WAY -> {//选择圈子加入方式
                val fragmentViewModel: CircleSelectJoinWayViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.add_share_circle)
                params.headerDescribe = getString(R.string.please_select_jion_way)
                
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.confirm)
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSelectJoinWayAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_MANANGER -> {//圈子管理
                val fragmentViewModel: CircleFragmentViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.circle_manager)
                params.headerDescribe = getString(R.string.circle_manager)
                
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleManagerAdapter(context, viewModel, fragmentViewModel, navController)
            }
            CIRCLE_DETAIL -> {//圈子详情
                val fragmentViewModel: CircleFragmentViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.circle_manager)
                params.headerDescribe = getString(R.string.details_file)
                
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleDetialAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_SHARE -> {//圈子分享
                val fragmentViewModel: CircleShareViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.circle_share)
                params.headerDescribe = getString(R.string.circle_share)
                
                //底部按钮不可用
                params.bottomIsEnable = false
                params.bottomTitle = getString(R.string.share)
                params.bottomIsFullButton = true
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleShareAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_MEMBER -> {//圈子成员
                val fragmentViewModel: CircleMemberViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.members_mng)
                params.headerDescribe = getString(R.string.members_mng)
                
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleMemberAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_EN_DEVICE -> {//EN设备管理
                val fragmentViewModel: CircleENDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.en_device_manager)
                params.headerDescribe = getString(R.string.en_device_manager)
                
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.add_en_server)
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleENDeviceAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_SETTING -> {//设置
                val fragmentViewModel: CircleSettingViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.settings)
                params.headerDescribe = getString(R.string.settings)
                
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSettingAdapter(context, viewModel, fragmentViewModel, navController)
            }
            CIRCLE_OWN_FEE_RECORDS -> {//我的付费项
                val fragmentViewModel: CircleOwnFeeRecordViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)
                params.headerTitle = getString(R.string.own_fee_records)
                params.headerDescribe = getString(R.string.own_fee_records)
                
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleOwnFeeRecordAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_PURCHASE_FLOW -> {//选购流量
                val fragmentViewModel: CirclePurchaseFlowViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.purchase_flow_fee_ways)
                params.headerDescribe = getString(R.string.purchase_flow_fee_ways)
                
                params.bottomIsFullButton = true
                params.bottomTitle = getString(R.string.purchase)
                params.bottomIsEnable = false
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CirclePurchaseFlowAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_OWN_DEVICE -> {//我的圈内设备
                val fragmentViewModel: CircleOwnDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.circle_own_device)
                params.headerDescribe = getString(R.string.circle_own_device)
                
                params.bottomIsFullButton = true
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleOwnDeviceAdapter(context, viewModel, fragmentViewModel)
            }

            CIRCLE_OWN_DEVICE_DETAIL -> {//我的圈内设备 详情
                val fragmentViewModel: CircleOwnDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)
                
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleOwnDeviceDetialAdapter(context, viewModel, fragmentViewModel)
            }

            CIRCLE_EN_DEVICE_DETAIL -> {//en设备详情 二级弹框
                val fragmentViewModel: CircleENDeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headBackButtonVisibility = View.GONE
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleENDeviceDetailAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_MEMBER_DETAIL -> {//成员管理 二级弹框
                val fragmentViewModel: CircleMemberDetialViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headBackButtonVisibility = View.GONE
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleMemberDetialAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_SETTING_FEES_DETIAL -> {//成员管理 二级弹框
                val fragmentViewModel: CircleSettingFeesDetialViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headBackButtonVisibility = View.GONE
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSettingFeesDetialAdapter(context, viewModel, fragmentViewModel)
            }
            CIRCLE_EN_SERVER_FEES -> {//设置EN抽成费用
                val fragmentViewModel: CircleENServerFeesViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                params.headerTitle = getString(R.string.setting_en_setting_fees)
                params.headerDescribe = getString(R.string.setting_en_setting_fees)
                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleENServerFeesAdapter(context, viewModel, fragmentViewModel)
            }
            else -> {
                val fragmentViewModel: CircleSelectDeviceViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.networkId,viewModel.mStateListener,viewModel.mUnDismissStateListener)

                //setViewStatusParams 得在adapter前，否则影响头部更新顺序
                fragmentViewModel.setViewStatusParams(params)
                adapter = CircleSelectDeviceAdapter(context, viewModel, fragmentViewModel)
            }

        }

        return adapter
    }

    /**
     * @param headerTitle 头部标题
     * @param headerDescribe 头部描述
     * @param bottomVisibility 底部是否可见
     * @param bottomTitle  底部标题，（非空和非空字符串）有标题则显示
     * @param bottomIsFullButton 是否为全满Button,即蓝色按钮
     * @param bottomIsEnable 底部按钮是否可用
     *
     * @param bottomAddTitle 底部添加的蓝色 按钮 标题，有标题则显示
     * @param bottomAddIsEnable 底部添加的蓝色 按钮 是否可用
     * @param headerIcon -1表示默认使用设备头像，0表示不显示头像，否则为指定资源
     *
     */
    data class ViewStatusParams(var headerIcon: Int = -1, var headerTitle: String? = null, var headerDescribe: String? = null,
                                var headBackButtonVisibility: Int = View.VISIBLE,
                                var bottomTitle: String? = null,
                                var bottomIsEnable: Boolean = true,
                                var bottomIsFullButton: Boolean = false,

                                var bottomAddTitle: String? = null,
                                var bottomAddIsEnable: Boolean = true)

    data class DetailMenu(var function: Int = 0, var title: String? = null, var icon: Int = 0)

    fun getString(id: Int): String {
        return net.linkmate.app.base.MyApplication.getInstance().resources.getString(id)
    }


}