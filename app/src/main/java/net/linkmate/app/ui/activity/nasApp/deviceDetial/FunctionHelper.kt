package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import net.linkmate.app.R
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter.*

/**
 * @author Raleigh.Luo
 * date：20/7/23 17
 * describe：
 */
object FunctionHelper {
    const val FUNCTION = "function"
    const val EXTRA_ENTITY = "extra_entity"
    const val EXTRA = "extra"
    const val EXTRA_IS_ENSERVER = "extra_is_enserver"
    const val DEVICE_BOUND_TYPE = "device_bound_type"
    const val POSITION = "position"
    const val DEVICE_BEAN = "device_bean"

    /*---设备管理------------------------------------------------------------------------*/
    //设备详情
    const val DEVICE_DETAIL = 0x000

    //状态
    const val DEVICE_STATUS = 0x001

    //查看子节点
    const val VIEW_SUBNODE = 0x002

    //分享设备
    const val DEVICE_SHARE = 0x003

    //所处网络
    const val DEVICE_NETWORK = 0x004

    //流量明细
    const val DEVICE_FLOW = 0x005

    //子网
    const val DEVICE_SUBNET = 0x006

    //升级固件
    const val FORMATE_UPGRADE_FIRMWARE = 0x007


    //控制面板－设置
    const val DEVICE_CONTROL = 0x008

    //领取积分
    const val DEVICE_SCORE = 0x009

    //简介
    const val DEVICE_BRIEF = 0x010

    /*---设置------------------------------------------------------------------------*/
    //编辑设备名称
    const val EDIT_DEVICE_NAME = 0x101

    //编辑设备备注名
    const val EDIT_DEVICE_REMARK = 0x102

    //成员管理
    const val DEVICE_MEMBER = 0x103

    //节点配置
    const val DEVICE_NODE_CONFIG = 0x104

    //远程管理
    const val DEVICE_REMOTE_MANAGER = 0x105

    //应用管理
    const val DEVICE_APP_MANAGER = 0x106

    //设备空间
    const val DEVICE_SPACE = 0x107

    //重新启动
    const val DEVICE_RESTART = 0x108

    //安全关机
    const val DEVICE_SELF_SHUTDOWN = 0x109

    //移除
    const val DEVICE_REMOVE = 0x110

    //设置收费项
    const val DEVICE_SETTING_FEES = 0x111

    //选购流量
    const val DEVICE_PURCHASE_FLOW = 0x112

    //局域网访问
    const val DEVICE_LAN_ACCESS = 0x113

    //更改流量费支付方
    const val ALTER_FLOW_FEE_PAYER = 0x114

    //设备切圈
    const val DEVICE_CHANGE_CIRCLE = 0x115

    /*---其它------------------------------------------------------------------------*/
    //切换节点
    const val DEVICE_CHANGE_NODE = 0x201

    //切换网络
    const val DEVICE_CHANGE_NETWORK = 0x202

    //格式化硬盘
    const val FORMATE_HARD_DISK = 0x203

    //编辑流量单价
    const val EDIT_UNIT_PRICE = 0x204

    /*---单独页面，不需要返回到上一级------------------------------------------------------------------------*/
    //与圈子管理界限,用于处理onBackPressed的返回事件
    const val DEVICE_MANAGER_LIMIT = 0x300

    //设置收费项 详情
    const val DEVICE_SETTING_FEES_DETIAL = 0x301

    //订阅设备服务（加入费、使用费）
    const val DEVICE_PURCHASE_JOIN = 0x302


    //DeviceBean 无法序列化，只能使用静态变量了
    var deviceBeanTemp: DeviceBean? = null

    /**
     * 释放对象
     */
    fun clear() {
        deviceBeanTemp = null
    }

    /**
     * 获取设备菜单-图标／标题
     */
    fun getDeviceMenu(function: Int): DetailMenu {
        var detailMenu = DetailMenu()
        detailMenu.function = function
        when (function) {
            DEVICE_DETAIL -> {
            }
            DEVICE_BRIEF -> {//简介
                detailMenu.icon = R.drawable.ic_summary
                detailMenu.title = FunctionHelper.getString(R.string.summary)
            }
            DEVICE_STATUS -> {//状态
                detailMenu.icon = R.drawable.ic_device_1
                detailMenu.title = getString(R.string.status)
            }
            DEVICE_SHARE -> { //分享设备
                detailMenu.icon = R.drawable.ic_device_2
                detailMenu.title = getString(R.string.share_device)
            }

            DEVICE_NETWORK -> { //所处网络
                detailMenu.icon = R.drawable.icon_net_blue
                detailMenu.title = getString(R.string.network_location)
            }

            DEVICE_FLOW -> { //流量明细
                detailMenu.icon = R.drawable.icon_me_dev_traffic
                detailMenu.title = getString(R.string.flow_details)
            }
            DEVICE_SUBNET -> {//子网
                detailMenu.icon = R.drawable.ic_device_12
                detailMenu.title = getString(R.string.subnet)
            }
            DEVICE_CONTROL -> { //控制面板
                detailMenu.icon = R.drawable.ic_device_11
                detailMenu.title = getString(R.string.settings)
            }
            DEVICE_SCORE -> {//领取积分
                detailMenu.icon = R.drawable.ic_device_5
                detailMenu.title = getString(R.string.receive_score)
            }
            DEVICE_CHANGE_NODE -> {//切换节点
                detailMenu.icon = R.drawable.ic_device_6
                detailMenu.title = getString(R.string.select_smartnode)
            }
            DEVICE_CHANGE_NETWORK -> {//切换网络
                detailMenu.icon = R.drawable.icon_me_netmng
                detailMenu.title = getString(R.string.switch_network)
            }
            DEVICE_MEMBER -> {//成员管理
                detailMenu.icon = R.drawable.ic_device_3
                detailMenu.title = getString(R.string.members_mng)
            }
            DEVICE_NODE_CONFIG -> {//节点配置
                detailMenu.icon = R.drawable.ic_device_6
                detailMenu.title = getString(R.string.node_config)
            }
            DEVICE_LAN_ACCESS -> {//局域网访问
                detailMenu.icon = R.drawable.ic_device_7
                detailMenu.title = getString(R.string.device_lan_access)
            }
            DEVICE_REMOTE_MANAGER -> {//远程管理
                detailMenu.icon = R.drawable.ic_device_7
                detailMenu.title = getString(R.string.view_remote_management)
            }
            DEVICE_APP_MANAGER -> {//应用管理
                detailMenu.icon = R.drawable.ic_device_13
                detailMenu.title = getString(R.string.app_mng)
            }
            DEVICE_SPACE -> { //设备空间
                detailMenu.icon = R.drawable.ic_device_4
                detailMenu.title = getString(R.string.device_space)
            }
            DEVICE_RESTART -> {//重新启动
                detailMenu.icon = R.drawable.ic_device_8
                detailMenu.title = getString(R.string.device_reboot)
            }
            DEVICE_SELF_SHUTDOWN -> {//安全关机
                detailMenu.icon = R.drawable.ic_device_9
                detailMenu.title = getString(R.string.device_power_off)
            }
            DEVICE_REMOVE -> {//移除
                detailMenu.icon = R.drawable.ic_device_10
                detailMenu.title = getString(R.string.remove)
            }
            FORMATE_HARD_DISK -> {//格式化硬盘
                detailMenu.icon = R.drawable.icon_add_device
                detailMenu.title = getString(R.string.hd_format)
            }
            FORMATE_UPGRADE_FIRMWARE -> {//升级固件
                detailMenu.icon = R.drawable.ic_device_14
                detailMenu.title = getString(R.string.upgrade_firmware)
            }
            VIEW_SUBNODE -> {//查看子节点
                detailMenu.icon = R.drawable.ic_device_12
                detailMenu.title = getString(R.string.view_subnode)
            }
            EDIT_DEVICE_NAME -> {//编辑设备名
                detailMenu.icon = R.drawable.icon_edit_dev_name
                detailMenu.title = getString(R.string.modify_dev_name)
            }
            EDIT_DEVICE_REMARK -> {//编辑设备备注
                detailMenu.icon = R.drawable.icon_edit_note
                detailMenu.title = getString(R.string.modify_remark_name)
            }
            EDIT_UNIT_PRICE -> {//编辑流量单价
                detailMenu.icon = R.drawable.icon_edit_traffic
                detailMenu.title = getString(R.string.modify_price)
            }
            DEVICE_SETTING_FEES -> {//设置收费项
                detailMenu.icon = R.drawable.ic_fee_setting
                detailMenu.title = getString(R.string.settting_join_way)
            }
            DEVICE_PURCHASE_FLOW -> {//购买流量
                detailMenu.icon = R.drawable.ic_fee_setting
                detailMenu.title = getString(R.string.purchase_flow_fee_ways)
            }
            ALTER_FLOW_FEE_PAYER -> {//更改流量费支付方
                detailMenu.icon = R.drawable.ic_alter_flow_fee_payer
                detailMenu.title = getString(R.string.alter_flow_fee_payer)
            }
            DEVICE_CHANGE_CIRCLE -> {//设备切圈
                detailMenu.icon = R.drawable.ic_device_change_circle
                detailMenu.title = getString(R.string.device_change_circle)
            }
        }
        return detailMenu
    }

    /**
     * 构建适配器，底部状态显示
     */
    fun initFragmentPanel(function: Int, context: Fragment, viewModel: DeviceViewModel,
                          navController: NavController)
            : DeviceBaseAdapter<DeviceDetailViewModel> {
        //底部按钮参数
        val params = ViewStatusParams()
        params.headerTitle = viewModel.device.name
        //适配器
        var adapter: DeviceBaseAdapter<DeviceDetailViewModel>
        when (function) {
            DEVICE_DETAIL -> {//设备详情
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.details_device)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceDetailAdapter(context, fragmentViewModel, viewModel, navController)
            }
            DEVICE_STATUS -> {//状态
                val fragmentViewModel: DeviceStatusViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.status)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceStatusAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_SHARE -> {//分享设备
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.share_device)
                params.bottomTitle = getString(R.string.share)
                params.bottomIsFullButton = true
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceShareAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_MEMBER -> {//成员管理
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.members_mng)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceMemberAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_NETWORK -> {//所处网络
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.network_location)
                params.bottomTitle = getString(R.string.enter_selected_network)
                params.bottomIsFullButton = true
                params.bottomIsEnable = true
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceNetWorkAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_SPACE -> {//设备空间
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.device_space)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceSpaceAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_SUBNET -> {//子网
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.subnet)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceSubnetAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_CONTROL -> {//控制面板
                val fragmentViewModel: DeviceControlViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.settings)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceControlAdapter(context, fragmentViewModel, viewModel, navController)
            }
            DEVICE_NODE_CONFIG -> {//节点配置
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.node_config)
                params.bottomTitle = getString(R.string.add_subnet)
                params.bottomIsFullButton = false
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceNodeConfigAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_CHANGE_NODE -> {//切换节点
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.select_smartnode)
                params.bottomTitle = getString(R.string.enable_node)
                params.bottomIsFullButton = true
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceChangeNodeAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_CHANGE_NETWORK -> {//切换网络
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.switch_network)
                params.bottomTitle = getString(R.string.change_to_network)
                params.bottomIsFullButton = true
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceChangeNetworkAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_REMOTE_MANAGER -> {//远程管理
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.view_remote_management)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceRemoteManagerAdapter(context, fragmentViewModel, viewModel, navController)
            }
            DEVICE_SETTING_FEES_DETIAL -> {//设置费用详情
                val fragmentViewModel: DeviceSettingFeesDetialViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)
                params.headBackButtonVisibility = View.GONE
                params.headerIcon = 0
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceSettingFeesDetialAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_PURCHASE_FLOW -> {//购买流量
                val fragmentViewModel: DevicePurchaseFlowViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)
                params.bottomIsFullButton = true
                params.bottomTitle = context.getString(R.string.purchase)
                params.headerDescribe = getString(R.string.purchase_flow_fee_ways)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DevicePurchaseFlowAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_PURCHASE_JOIN -> {//订阅设备服务（加入费、使用费）
                val fragmentViewModel: DevicePurchaseFlowViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)
                params.bottomIsFullButton = true
                params.bottomTitle = context.getString(R.string.purchase)
                params.headerTitle = getString(R.string.device_purchase_join_fees)
                params.headerDescribe = getString(R.string.pls_select_type)
                fragmentViewModel.setViewStatusParams(params)
                adapter = DevicePurchaseFlowAdapter(context, fragmentViewModel, viewModel)
            }
            DEVICE_CHANGE_CIRCLE -> {//设备切圈子
                val fragmentViewModel: DeviceChangeCircleViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                params.headerDescribe = getString(R.string.device_change_circle)
                params.bottomTitle = getString(R.string.change_to_network)
                params.bottomIsFullButton = true
                params.bottomIsEnable = false //默认先显示不可点击
                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceChangeCircleAdapter(context, fragmentViewModel, viewModel)
            }
            else -> {
                val fragmentViewModel: DeviceDetailViewModel by context.viewModels()
                fragmentViewModel.init(viewModel.mStateListener)

                fragmentViewModel.setViewStatusParams(params)
                adapter = DeviceDetailAdapter(context, fragmentViewModel, viewModel, navController)
            }
        }
        return adapter
    }

    /**
     * @param headerTitle 头部标题
     * @param headerDescribe 头部描述
     * @param bottomVisibility 底部是否可见
     * @param bottomTitle  标题，（非空和非空字符串）有标题则显示
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

    data class DetailMenu(var function: Int = 0, var title: String? = null, var icon: Int = 0, var remark: String? = null)

    fun getString(id: Int): String {
        return net.linkmate.app.base.MyApplication.getInstance().resources.getString(id)
    }


}