package net.linkmate.app.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_device_flow_detail_filter.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.util.business.DeviceUserUtil
import net.linkmate.app.view.ViewHolder
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.CommonResultListener
import net.sdvn.common.internet.loader.DeviceSharedUsersHttpLoader
import net.sdvn.common.internet.protocol.SharedUserList
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.nascommon.utils.ToastHelper
import java.util.*

/**
 * @author Raleigh.Luo
 * date：21/4/23 20
 * describe：
 */
class FlexUserAdapter(val mContext: Context) : RecyclerView.Adapter<ViewHolder>() {
    private var sources: List<ShareUser>? = null
    var selectedUserId: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(mContext).inflate(R.layout.item_device_flow_detail_filter, null, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (sources == null) 0 else ((sources?.size ?: 0) + 1)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            cbCheck.setOnCheckedChangeListener(null)
            cbCheck.setOnClickListener(null)

            if (position == 0) {
                cbCheck.setTag(null)
                ivChecked.setTag(null)
                cbCheck.setText(mContext.getString(R.string.all))
                if (selectedUserId == null) {
                    cbCheck.isChecked = true
                    ivChecked.visibility = View.VISIBLE
                } else {
                    cbCheck.isChecked = false
                    ivChecked.visibility = View.GONE
                }
                cbCheck.setOnClickListener {
                    if (cbCheck.isChecked) {
                        selectedUserId = null
                        ivChecked.visibility = View.VISIBLE
                        notifyDataSetChanged()
                    } else {//不能取消
                        cbCheck?.isChecked = true
                    }
                }
            } else {

                val index = position - 1
                val user = sources?.get(index)!!
                var name = user.nickname
                if (TextUtils.isEmpty(name)) name = user.username
                if (TextUtils.isEmpty(name)) name = user.email
                cbCheck.setText(name)
                cbCheck.setTag(user.userid)
                if (selectedUserId == user.userid) {
                    cbCheck.isChecked = true
                    ivChecked.visibility = View.VISIBLE
                } else {
                    cbCheck.isChecked = false
                    ivChecked.visibility = View.GONE
                }

                cbCheck.setOnClickListener {
                    if (cbCheck.isChecked) {
                        selectedUserId = user.userid
                        ivChecked.visibility = View.VISIBLE
                        notifyDataSetChanged()
                    } else {//不能取消
                        cbCheck?.isChecked = true
                    }
                }
            }
        }
    }

    fun clear() {
        sources = null
        selectedUserId = null
        notifyDataSetChanged()
    }

    fun update(deviceId: String, isNas: Boolean) {
        if (isNas) {
            getNasUserList(deviceId)
        } else {
            getUserList(deviceId)
        }
    }

    fun getNasUserList(deviceId: String) {
        //获取服务器中的用户列表
        val loader = DeviceSharedUsersHttpLoader(SharedUserList::class.java)
        loader.setParams(deviceId)
        loader.executor(object : CommonResultListener<SharedUserList>() {

            override fun success(tag: Any?, sharedUserList: SharedUserList) {
                sharedUserList.users.sortBy { it.mgrlevel }
                sources = sharedUserList.users
                notifyDataSetChanged()
            }

            override fun error(tag: Any?, mErrorProtocol: GsonBaseProtocol) {
                ToastHelper.showToast(SdvnHttpErrorNo.ec2String(mErrorProtocol.result))
                sources = arrayListOf()
                notifyDataSetChanged()
            }
        })
    }

    fun getUserList(deviceId: String) {
        DeviceUserUtil.shareUsers(deviceId, null,
                object : MyOkHttpListener<SharedUserList>() {
                    override fun success(tag: Any?, data: SharedUserList) {
                        Collections.sort(data.users, object : Comparator<ShareUser> {
                            override fun compare(o1: ShareUser, o2: ShareUser): Int {
                                return if (o1.mgrlevel != o2.mgrlevel) o1.mgrlevel - o2.mgrlevel else o1.datetime.compareTo(o2.datetime)

                            }
                        })
                        sources = data.users
                        notifyDataSetChanged()
                    }

                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                        super.error(tag, baseProtocol)
                        sources = arrayListOf()
                        notifyDataSetChanged()
                    }
                })
    }
}