package net.sdvn.common.exception

/**
 * @Description: 设备未发现异常
 * @Author: todo2088
 * @CreateDate: 2021/3/10 14:41
 */
class DeviceNotFountException(devId: String?) : Exception(devId) 