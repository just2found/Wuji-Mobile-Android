package net.linkmate.app.ui.simplestyle.dynamic

import net.linkmate.app.R

/**圈子状态-主EN状态
 * @author Raleigh.Luo
 * date：21/2/24 17
 * describe：
 */
enum class CircleStatus(val type:Int,val titleRes:Int,val operateTextRes:Int) {
    //默认
    NONE(-2,0,0),
    //无网络
    WITHOUT_NETWORK(-1,0,0),
    //正常数据
    NOMARL(0,R.string.without_dynamic,R.string.publish_immediately),
    //未选购流量
    WITHOUT_PURCHASE_CIRCLE_FLOW(1,R.string.flow_is_expire,R.string.to_purchase_immediately),
    WITHOUT_PURCHASE_DEVICE_FLOW(2,R.string.flow_is_expire,R.string.to_purchase_immediately),
    //未订阅设备服务
    UNSUBSCRIBE_DEVICE_SERVER(3,R.string.unsubscribe_device_server,R.string.subscribe_immediately),
    //主EN离线,空页面才显示
    DEVICE_OFFLINE(4,R.string.not_find_en_server,0),
    //无主EN
    WITHOUT_DEVICE_SERVER(5,R.string.not_find_en_server,R.string.add_immediately),
    //积分不足,优先级最高
    WITHOUT_POINTS(6,R.string.ec_insufficient_points,R.string.purchase_immediately),
    //免费圈子不支持动态
    NOT_SUPPORT_DYNAMIC(8,R.string.the_circle_not_support_dynamic,0)

}