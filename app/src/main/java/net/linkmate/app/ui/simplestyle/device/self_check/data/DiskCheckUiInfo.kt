package net.linkmate.app.ui.simplestyle.device.self_check.data

/**
create by: 86136
create time: 2021/2/7 13:58
Function description:这是UI获取到的信息
这个对象不需要序列化
 */
data class DiskCheckUiInfo(val deviceId: String, var goodCondition: Boolean, var diskName: String? = null, var diskSize: String? = null,
                           var serialNumber: String? = null, var useTime: String? = null) {

    //这个是判断需要异步请求的数据是否完备
    fun isComplete(): Boolean {
        return (diskName != null && diskSize != null && serialNumber != null&& useTime != null)
    }
}