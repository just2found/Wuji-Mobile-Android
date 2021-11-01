package net.linkmate.app.ui.activity.nasApp.deviceDetial.repository

import androidx.arch.core.util.Function
import net.linkmate.app.R
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.manager.DevManager
import net.linkmate.app.util.business.DeviceUserUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.GradeBindDeviceHttpLoader
import net.sdvn.common.internet.loader.UnbindDeviceHttpLoader
import net.sdvn.common.internet.protocol.SharedUserList
import net.sdvn.common.internet.protocol.UnbindDeviceResult
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.nascommon.utils.ToastHelper
import java.util.*

/**
 * @author Raleigh.Luo
 * date：20/7/29 10
 * describe：
 */
class DeviceMemberRepository {
    /**
     * 获取成员列表－非nas
     */
    fun getShareUsers(deviceId:String, stateListener: HttpLoader.HttpLoaderStateListener,callback: Function<List<ShareUser>, Void?>){
        DeviceUserUtil.shareUsers(deviceId, stateListener ,
                object : MyOkHttpListener<SharedUserList>() {
                    override fun success(tag: Any?, data: SharedUserList) {
                        Collections.sort(data.users, object : Comparator<ShareUser> {
                            override fun compare(o1: ShareUser, o2: ShareUser): Int {
                                return if (o1.mgrlevel != o2.mgrlevel) o1.mgrlevel - o2.mgrlevel else o1.datetime.compareTo(o2.datetime)

                            }
                        })
                        callback.apply(data.users)
                    }
                })
    }
      fun modifyUserLevel(user: ShareUser, mgrlevel: Int,deviceId:String,callback:Function<Boolean,Void?>) {
        val loader = GradeBindDeviceHttpLoader(GsonBaseProtocol::class.java)
        loader.setParams(user.userid, deviceId, mgrlevel)
        loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
            override fun success(tag: Any?, data: GsonBaseProtocol) {
                ToastHelper.showToast(R.string.success)
                callback.apply(true)
                if (mgrlevel == 0) {
                    DevManager.getInstance().initHardWareList(null)//modifyUserLevel 转让owner
                }
            }
        })
    }

      fun unbind(userId: String,deviceId: String,callback:Function<Boolean,Void?>) {
        //用户解绑
        val unbindDeviceHttpLoader = UnbindDeviceHttpLoader()
        unbindDeviceHttpLoader.unbindSingle(deviceId, userId, object : ResultListener<UnbindDeviceResult?> {
            override fun success(tag: Any?, data: UnbindDeviceResult?) {
                val curUid = CMAPI.getInstance().baseInfo.userId
                ToastHelper.showToast(R.string.remove_success)
                callback.apply(true)
                if (curUid == userId) {
                    DevManager.getInstance().initHardWareList(null)//设备用户解绑
                }
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                ToastHelper.showToast(R.string.remove_device_failed)
            }
        })
    }


}