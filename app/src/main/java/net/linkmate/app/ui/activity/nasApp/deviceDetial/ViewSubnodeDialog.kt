package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.R
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.bean.VNodeBean
import net.linkmate.app.view.adapter.VNodeRVAdapter
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Device.VNode
import java.util.*

/** 查看子节点
 * @author Raleigh.Luo
 * date：20/8/4 11
 * describe：
 */
class ViewSubnodeDialog(context: Context): Dialog(context, R.style.DialogTheme)  {
    private lateinit var vnodeBack: View
    private lateinit var  vnodeRv: RecyclerView
    private lateinit var adapter:VNodeRVAdapter
    var device:DeviceBean?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LayoutInflater.from(getContext()).inflate(R.layout.layout_vnode, null))
        vnodeBack =  findViewById(R.id.lv_iv_back)
        vnodeRv =  findViewById(R.id.lv_rv)
        vnodeBack.setOnClickListener {
            dismiss()
        }

        adapter = VNodeRVAdapter(ArrayList())
        val emptyView = LayoutInflater.from(context).inflate(R.layout.pager_empty_text, null)
        (emptyView.findViewById<View>(R.id.tv_tips) as TextView).setText(R.string.tips_no_dev)
        adapter.setEmptyView(emptyView)
        vnodeRv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        vnodeRv.itemAnimator = null
        vnodeRv.adapter = adapter
        refreshSubnode()
    }

    override fun show() {
        super.show()
        refreshSubnode()
    }

    fun refreshSubnode() {
        device?.let {
            CMAPI.getInstance().refreshBaseInfo()
            // TODO: 2019/01/20  vnode test
            CMAPI.getInstance().refreshDevices()
            val beans: MutableList<VNodeBean> = ArrayList()
            val vNodes: List<VNode> = ArrayList<VNode>(it.vNode)
            Collections.sort(vNodes, object : Comparator<VNode> {
                override fun compare(o1: VNode, o2: VNode): Int {
                    return if (o1.needPaid == o2.needPaid) o1.groupName.compareTo(o2.groupName) else if (o1.needPaid) -1 else 1
                }
            })
            for (vNode in vNodes) {
                if (vNode.deviceIds.size <= 0) continue
                val title = VNodeBean()
                title.name = vNode.groupName
                title.type = -1
                beans.add(title)
                val usableSnid = CMAPI.getInstance().baseInfo.usableSnid
                for (gd in vNode.deviceIds) {
                    val bean = VNodeBean()
                    if (usableSnid == gd.id) {
                        bean.isSelected = true
                    }
                    if (!TextUtils.isEmpty(gd.name)) {
                        bean.name = gd.name
                    } else for (device in CMAPI.getInstance().devices) {
                        if (device.id == gd.id) {
                            bean.name = device.name
                            bean.isOnline = true
                            break
                        }
                    }
                    bean.type = 0
                    beans.add(bean)
                }
            }
            adapter.setNewData(beans)
        }
    }
}