package net.linkmate.app.ui.viewmodel

import android.app.Activity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import io.reactivex.disposables.Disposable
import net.linkmate.app.R
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader.HttpLoaderStateListener
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.V2BindDevicesHttpLoader
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.nascommon.iface.EventListener
import net.sdvn.nascommon.model.eventbus.DevHDAddNewUsers
import net.sdvn.nascommon.model.http.OnHttpRequestListener
import net.sdvn.nascommon.model.oneos.api.SyncDevInfoOneOsApi
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.widget.UserSelectPopupView
import java.util.*

class UserUpdateViewModel(private val mDevHDAddNewUsers: DevHDAddNewUsers, private val mContext: Activity,
                          private val mEventListener: EventListener<String>) {
    operator fun invoke() {
        val newUsers = mDevHDAddNewUsers.newUsers
        val userSelectPopupView: UserSelectPopupView<ShareUser> =
                object : UserSelectPopupView<ShareUser>(mContext, R.string.user_migration) {
                    inner class SelectUserViewHolder(view: View) : ViewHolder(view) {
                        var userName: TextView = view.findViewById(R.id.share_user)
                        var userAccount: TextView = view.findViewById(R.id.share_user_account)
                        var userSelect: CheckBox = view.findViewById(R.id.select_user)

                    }

                    override fun bindViewHolder(holder: ViewHolder, position: Int) {
                        val shareUser = userList[position]!!
                        val selectUserViewHolder = holder as SelectUserViewHolder
                        selectUserViewHolder.userAccount.text = shareUser.username
                        selectUserViewHolder.userName.text = shareUser.fullName
                        selectUserViewHolder.userSelect.isChecked = isSelected(position)
                    }

                    override fun getLayoutResId(): Int {
                        return R.layout.item_listview_share
                    }

                    override fun createViewHolder(view: View): ViewHolder {
                        return SelectUserViewHolder(view)
                    }
                }
        userSelectPopupView.setOnItemClickListener { parent, view, position, id ->
            val check = view.findViewById<CheckBox>(R.id.select_user)
            check.toggle()
            val isSelect = check.isChecked
            userSelectPopupView.isSelected[position] = isSelect
            userSelectPopupView.adapter.notifyDataSetChanged()
        }
        userSelectPopupView.addUsers(newUsers)
        userSelectPopupView.setPositiveButton(View.OnClickListener { v ->
            if (Utils.isFastClick(v)) {
                return@OnClickListener
            }
            val selectShareUser: MutableList<String> = ArrayList()
            for (i in newUsers.indices) {
                if (userSelectPopupView.isSelected(i)) {
                    selectShareUser.add(newUsers[i].userid)
                }
            }
            if (selectShareUser.size > 0) {
                val bindDevicesHttpLoader = V2BindDevicesHttpLoader(GsonBaseProtocol::class.java)
                bindDevicesHttpLoader.setParams(selectShareUser
                        , mDevHDAddNewUsers.devAttrInfo.sys.devicesn
                        , mDevHDAddNewUsers.devAttrInfo.sys.appId)
                bindDevicesHttpLoader.setHttpLoaderStateListener(
                        object : HttpLoaderStateListener {
                            override fun onLoadStart(disposable: Disposable) {
                                mEventListener.onStart(null)
                            }

                            override fun onLoadComplete() {}
                            override fun onLoadError() {
                                mEventListener.onFailure(null, 0, "")
                            }
                        })
                bindDevicesHttpLoader.executor(object : ResultListener<GsonBaseProtocol> {
                    override fun success(tag: Any?, data: GsonBaseProtocol) {
                        ToastHelper.showToast(R.string.tip_user_migration_success)
                        var syncDevInfoOneOsApi: SyncDevInfoOneOsApi? = null
                        if (mDevHDAddNewUsers.deviceModelNew.loginSession != null) {
                            syncDevInfoOneOsApi = SyncDevInfoOneOsApi(mDevHDAddNewUsers.deviceModelNew.loginSession!!)
                            syncDevInfoOneOsApi.sync(object : OnHttpRequestListener {
                                override fun onStart(url: String) {}
                                override fun onSuccess(url: String, result: String) {
                                    mEventListener.onSuccess(url, result)
                                }

                                override fun onFailure(url: String, httpCode: Int, errorNo: Int, strMsg: String) {
                                    mEventListener.onFailure(url, errorNo, strMsg)
                                }
                            })
                        }
                    }

                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                        ToastHelper.showToast(R.string.tip_user_migration_failed)
                    }
                })
                userSelectPopupView.dismiss()
            } else {
                ToastHelper.showToast(R.string.tip_please_check_user)
            }
        })
    }

}