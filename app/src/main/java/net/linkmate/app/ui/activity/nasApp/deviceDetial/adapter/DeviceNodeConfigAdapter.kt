package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_device_item_dns.view.*
import kotlinx.android.synthetic.main.dialog_device_item_subnet2.view.*
import kotlinx.android.synthetic.main.dialog_device_item_switch.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.EditIPDialog
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceNodeConfigRepository
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.util.CommonUtils
import net.sdvn.common.internet.protocol.entity.SubnetEntity
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.DialogUtils.OnDialogClickListener

/**
 * @author Raleigh.Luo
 * date：20/7/24 15
 * describe：
 */
class DeviceNodeConfigAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                              viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val sources: ArrayList<SubnetEntity> = ArrayList()
    private val repository: DeviceNodeConfigRepository = DeviceNodeConfigRepository()

    //不包含子网数量
    private var excludeSubnetCount: Int = 0
    private var isAccessInternet = repository.isAccessInternet(viewModel.device)
    private var isAccessSubnet = repository.isAccessSubnet(viewModel.device)

    //必须isAccessInternet先开启
    private var isDNSEnable = if (isAccessInternet) repository.isDNSEnable(viewModel.device) else false

    //子网内容是否更改过
    private var isSubnetChange = false

    init {
        repository.getSubnet(viewModel.device, viewModel.mStateListener, Function {
            sources.clear()
            sources.addAll(it)
            notifyDataSetChanged()
            null
        })
        if (isAccessSubnet) {
            fragmentViewModel.updateViewStatusParams(bottomTitle = context.getString(R.string.add_subnet))
        } else {
            fragmentViewModel.updateViewStatusParams(bottomTitle = "")
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> {
                return TYPE_SWITCH
            }
            1 -> {
                return if (isAccessInternet) TYPE_DNS else TYPE_SWITCH
            }
            2 -> {
                return if (isAccessInternet) TYPE_SWITCH else TYPE_DEFALUT
            }
            else -> {
                return TYPE_DEFALUT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = R.layout.dialog_device_item_subnet2
        when (viewType) {
            TYPE_SWITCH -> {
                layout = R.layout.dialog_device_item_switch
            }
            TYPE_DNS -> {
                layout = R.layout.dialog_device_item_dns
            }
        }
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        var size = 2
        if (isAccessInternet) size++
        excludeSubnetCount = size
        if (isAccessSubnet) size += sources.size
        return size
    }


    private var mEtFirstDNSText: String? = null
    private var mEtSecondDNSText: String? = null
    private val mHandler = Handler()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_SWITCH -> {
                with(holder.itemView) {
                    //解决mSwitch监听错位通知问题，先移除监听，调用完了再设置回去。
                    mSwitch.setOnCheckedChangeListener(null)
                    if (position == 0) {//访问因特网开关
                        mSwitch.text = context.getString(R.string.access_internet_by_sn)
                        mSwitch.isChecked = isAccessInternet
                        if (!isAccessInternet) {
                            mEtFirstDNSText = null
                            mEtSecondDNSText = null
                        }
                        mSwitch.setTag(position)
                    } else {//子网开关
                        mSwitch.text = context.getString(R.string.access_subnet_by_sn)
                        mSwitch.isChecked = isAccessSubnet

                    }
                    mSwitch.setOnCheckedChangeListener { compoundButton, b ->
                        if (position == 0) {
                            isAccessInternet = b
                            if (!b) isDNSEnable = false
                        } else {
                            isAccessSubnet = b
                            fragmentViewModel.updateViewStatusParams(bottomTitle = if (b) context.getString(R.string.add_subnet) else "")
                        }
                        /**解决mSwitch在RecyclerView开关时的刷新问题
                         * Cannot call this method while RecyclerView is computing a layout or scrolling
                         */
                        mHandler.post {
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            TYPE_DNS -> {
                with(holder.itemView) {
                    mSwitchDNS.setOnCheckedChangeListener(null)
                    etFirstDNS.addTextChangedListener {
                        mEtFirstDNSText = it.toString()
                    }
                    etSecondDNS.addTextChangedListener {
                        mEtSecondDNSText = it.toString()
                    }
                    //DNS开关
                    mSwitchDNS.text = context.getString(R.string.use_dedicated_dns)
                    mSwitchDNS.isChecked = isDNSEnable
                    mSwitchDNS.setOnCheckedChangeListener { compoundButton, b ->
                        isDNSEnable = b
                        if (b) {
                            goupDNS.visibility = View.VISIBLE
                            val mDns: String? = viewModel.device.getDns()
                            if (!mDns.isNullOrEmpty()) {
                                val split = mDns.split(",").toTypedArray()
                                if (split.size > 0) {
                                    etFirstDNS.setText(split[0])
                                } else {
                                    etFirstDNS.setText(split[0])
                                }
                                if (split.size > 1) etSecondDNS.setText(split[1])
                                else etSecondDNS.setText("")
                            }
                        } else {
                            goupDNS.visibility = View.GONE
                        }
                    }
                    if (isDNSEnable) {//有dns
                        if (etFirstDNSRequestFocus) {
                            etSecondDNSRequestFocus = false
                            etFirstDNS.isFocusable = true
                            etFirstDNS.isFocusableInTouchMode = true
                            etFirstDNS.requestFocus()
                        }
                        if (etSecondDNSRequestFocus) {
                            etSecondDNSRequestFocus = false
                            etSecondDNS.isFocusable = true
                            etSecondDNS.isFocusableInTouchMode = true
                            etSecondDNS.requestFocus()
                        }
                        goupDNS.visibility = View.VISIBLE
                        val mDns: String? = viewModel.device.getDns()
                        if (mEtFirstDNSText == null && mEtSecondDNSText == null) {
                            if (!TextUtils.isEmpty(mDns)) {
                                val split = mDns!!.split(",").toTypedArray()
                                if (split.size > 0) {
                                    etFirstDNS.setText(split[0])
                                } else etFirstDNS.setText("")
                                if (split.size > 1) etSecondDNS.setText(split[1])
                                else etSecondDNS.setText("")
                            }
                        } else {
                            etFirstDNS.setText(mEtFirstDNSText)
                            etSecondDNS.setText(mEtSecondDNSText)
                        }
                    } else {
                        mEtFirstDNSText = null
                        mEtSecondDNSText = null
                        goupDNS.visibility = View.GONE
                    }
                }
            }
            else -> {
                with(holder.itemView) {
                    val index = position - excludeSubnetCount
                    val net = sources.get(index).net
                    val mask = sources.get(index).mask
                    val maskShow = if (viewModel.regexMask.matches(mask)) {
                        try {
                            CommonUtils.calcPrefixLengthByNetMask(mask).toString()
                        } catch (e: Exception) {
                            mask
                        }
                    } else {
                        mask
                    }
                    tvIP.text = "$net/$maskShow"
                    //最后一个，显示底部线条
                    vSubnetBottomLine.visibility = if (index + 1 == sources.size) View.VISIBLE else View.GONE
                    setOnClickListener {
                        //是否已经被拦截处理
                        val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                        //没有拦截，则可以内部处理
                        if (!isInterceptor) internalItemClick(it, position)
                    }
                }
            }
        }

    }

    private var editIPDialog: EditIPDialog? = null

    override fun internalItemClick(view: View, position: Int) {
        //添加子网
        if (view.id == R.id.flBottom) {
            iniEditIPDialog()
            editIPDialog?.let {
                it.deleteBtnVisibility = View.GONE
                it.title = context.getString(R.string.add_subnet)
                it.ip = null
                it.mask = null
                it.show()
            }
        } else if (view.id == R.id.dialog_device_item_subnet_root) {//编辑子网
            iniEditIPDialog()
            editIPDialog?.let {
                val index = position - excludeSubnetCount
                it.deleteBtnVisibility = View.VISIBLE
                it.title = context.getString(R.string.edit)
                it.ip = sources.get(index).net
                val mask = sources.get(index).mask
                val maskShow = if (viewModel.regexMask.matches(mask)) {
                    try {
                        CommonUtils.calcPrefixLengthByNetMask(mask).toString()
                    } catch (e: Exception) {
                        mask
                    }
                } else {
                    mask
                }
                it.mask = maskShow
                it.position = index
                it.show()
            }
        }
    }

    private fun iniEditIPDialog() {
        if (editIPDialog == null) {
            editIPDialog = EditIPDialog(context.requireContext())
            editIPDialog?.let {
                it.positiveText = context.getString(R.string.confirm)
                it.negativeText = context.getString(R.string.cancel)
                it.onClickListener = View.OnClickListener {
                    //editIPDialog?.deleteBtnVisibility==View.VISIBLE){//编辑
                    if (it.id == R.id.positive) {
                        alterOrAddSubnet(editIPDialog?.position ?: -1)
                    } else if (it.id == R.id.tvDelete) {
                        deleteSubnet(editIPDialog?.position ?: -1)
                    }
                }
            }
        }
    }

    /**
     * 删除子网
     */
    private fun deleteSubnet(position: Int) {
        if (position >= 0 && position < sources.size) {
            isSubnetChange = true
            editIPDialog?.dismiss()
            sources.removeAt(position)
            notifyDataSetChanged()
        }

    }

    /**
     * 修改或新增子网
     */
    private fun alterOrAddSubnet(position: Int) {
        val ip = editIPDialog?.getIPContext() ?: ""
        val mask = editIPDialog?.getMaskContent() ?: ""
        if (ip.length == 0) {
            ToastUtils.showToast(R.string.pls_enter_right_subnet_ip)
        } else if (mask.length == 0) {
            ToastUtils.showToast(R.string.pls_enter_right_subnet_mask)
        } else if (sources.contains(SubnetEntity(ip, mask))) {
            ToastUtils.showToast(R.string.repeated_subnet)
        } else {
            isSubnetChange = true
            editIPDialog?.dismiss()
            if (editIPDialog?.deleteBtnVisibility == View.VISIBLE) {//编辑
                if (position >= 0 && position < sources.size) sources.set(position, SubnetEntity(ip, mask))
            } else {
                sources.add(SubnetEntity(ip, mask))
            }
            notifyDataSetChanged()
        }
    }


    override fun internalItemLongClick(view: View, position: Int) {
    }

    /**
     * 记住保存前的选择
     */
    private var isBackPressed = false
    private var isFinishActivity = false
    override fun interceptBackPressed(): Boolean {
        isBackPressed = true
        isFinishActivity = false
        askToSaveConfig()
        return true

    }

    override fun interceptFinishActivity(): Boolean {
        isFinishActivity = true
        isBackPressed = false
        askToSaveConfig()
        return true
    }

    /**
     * 询问是否保存
     */
    private fun askToSaveConfig() {
        val isChangeConfig = repository.isAccessInternet(viewModel.device) != isAccessInternet ||
                repository.isAccessSubnet(viewModel.device) != isAccessSubnet || isSubnetChange
                || (isAccessInternet && viewModel.device.getDns() != getEditTextDns())
        if (isChangeConfig) {
            DialogUtils.showConfirmDialog(context.requireContext(), 0, R.string.is_save_config, R.string.confirm,
                    R.string.cancel, OnDialogClickListener { dialog, isPositiveBtn ->
                if (isPositiveBtn) {
                    val dnsBuff = StringBuffer()
                    val isParamsValid = isParamsValid(dnsBuff)
                    //关闭了访问，dns自动清空
                    val dns = if (isAccessInternet) dnsBuff.toString().trim() else ""
                    if (isParamsValid) {
                        repository.submit(viewModel.mStateListener, viewModel.device, dns, isAccessInternet, isAccessSubnet, isSubnetChange, sources, Function {
                            //原事件返回-无法刷新device对象，只能强制关闭
                            isFinishActivity = true
                            continueBack()
                            null
                        })
                    }
                } else {
                    continueBack()
                }
            })
        } else {//没有变化，直接返回
            continueBack()
        }
    }

    /**
     * 获取输入框的dns文本
     */
    private fun getEditTextDns(): String {
        val dnsBuff = StringBuffer()
        if (isAccessInternet && isDNSEnable) {
            val dns1 = mEtFirstDNSText?.trim({ it <= ' ' }) ?: ""
            val dns2 = mEtSecondDNSText?.trim({ it <= ' ' }) ?: ""
            if (!TextUtils.isEmpty(dns1)) {
                dnsBuff.append(dns1)
            }
            if (!TextUtils.isEmpty(dns2)) {
                dnsBuff.append(",").append(dns2)
            }
        }
        return dnsBuff.toString().trim()
    }

    private var etFirstDNSRequestFocus = false
    private var etSecondDNSRequestFocus = false

    /**
     * 检查参数的合法性
     */
    private fun isParamsValid(dnsBuff: StringBuffer): Boolean {
        var isParamsValid = true
        if (isAccessInternet && isDNSEnable) {
            val dns1 = mEtFirstDNSText?.trim({ it <= ' ' }) ?: ""
            val dns2 = mEtSecondDNSText?.trim({ it <= ' ' }) ?: ""
            if (!TextUtils.isEmpty(dns1) || !TextUtils.isEmpty(dns2)) {
                val dnsRegex: Regex = Regex("^(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$")
                if (dns1.matches(dnsRegex)) {
                    dnsBuff.append(dns1)
                } else {
                    ToastUtils.showToast(R.string.pls_enter_right_dns)
                    etFirstDNSRequestFocus = true
                    notifyDataSetChanged()
                    isParamsValid = false
                    return isParamsValid
                }
                if (dns2.matches(dnsRegex)) {
                    dnsBuff.append(",").append(dns2)
                } else if (!TextUtils.isEmpty(dns2)) {
                    ToastUtils.showToast(R.string.pls_enter_right_dns)
                    etSecondDNSRequestFocus = true
                    notifyDataSetChanged()
                    isParamsValid = false
                    return isParamsValid
                }
            }
        }
        if (isAccessSubnet) {
            if (sources.size == 0) {
                ToastUtils.showToast(R.string.pls_configure_at_least_one_subnet)
                isParamsValid = false
                return isParamsValid
            }
        }
        return isParamsValid
    }

    /**
     * 返回
     */
    private fun continueBack() {
        if (isFinishActivity) {
            viewModel.toFinishActivity()
        } else {
            viewModel.toBackPress()
        }
    }

    override fun onDestory() {
        super.onDestory()
        if (editIPDialog?.isShowing == true) editIPDialog?.dismiss()
    }
}