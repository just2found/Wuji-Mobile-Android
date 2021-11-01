package net.linkmate.app.ui.simplestyle.device.disk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_disk_manage.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.utils.InputMethodUtils
import net.sdvn.nascommon.widget.CheckableImageButton


/**
 * A simple [Fragment] subclass.
 * Use the [DiskManageFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class DiskManageFragment : TipsBaseFragment() {

    private val viewModel by viewModels<DiskSpaceModel>({ requireParentFragment() })

    var lastGroupView: View? = null
    var lastCheckView: CheckableImageButton? = null
    private var isInit = false

    val mGroupClickListener by lazy {
        View.OnClickListener { groupView ->
            if (groupView == lastGroupView)
                return@OnClickListener

            lastGroupView?.let {
                it.setBackgroundResource(R.drawable.bg_device_white)
            }
            lastCheckView?.let {
                it.isChecked = false
            }

            lastGroupView = groupView
            lastGroupView?.setBackgroundResource(R.drawable.bg_device_white_stroke)
            lastCheckView = (groupView.tag as CheckableImageButton)
            lastCheckView?.isChecked = true
        }
    }

    val mOnCheckedChangeListener by lazy {
        CheckableImageButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (buttonView == lastCheckView)
                    return@OnCheckedChangeListener
                lastGroupView?.let {
                    it.setBackgroundResource(R.drawable.bg_device_white)
                }
                lastCheckView?.let {
                    it.isChecked = false
                }

                lastCheckView = buttonView
                lastGroupView = buttonView.parent as? View
                lastGroupView?.setBackgroundResource(R.drawable.bg_device_white_stroke)
                lastCheckView?.isChecked = true
            }
        }
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_disk_manage
    }


    fun containOpt(opt: String): Boolean {
        viewModel.arrayOpt?.forEach {
            if (it.mode == opt)
                return true
        }
        return false
    }

    override fun initView(view: View) {
        //因为所有的磁盘数量都支持这个
        if (containOpt(DiskSpaceModel.LVM_MODE)) lvm_cl.visibility = View.VISIBLE
        else lvm_cl.visibility = View.GONE
        if (containOpt(DiskSpaceModel.BASIC_MODE)) basic_cl.visibility = View.VISIBLE
        else basic_cl.visibility = View.GONE
        isInit = false;

        if (viewModel.arrayNode.isNullOrEmpty()) {
            viewModel.getDiskNodeInfo().observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    viewModel.arrayNode = it.data
                    viewModel.isGetNode = true
                    viewModel.checkDiskPower()
                    viewModel.arrayNode?.let { it1 -> initUi() }
                }
            })
        } else {
            initUi()
        }

        if (viewModel.arrayOpt.isNullOrEmpty()) {
            viewModel.getDiskActionItem().observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    viewModel.arrayOpt = it.data
                    initUi()
                }
            })
        } else {
            initUi()
        }

    }

    override fun onResume() {
        super.onResume()
        isInit = false
    }


    fun initUi() {
        if (isInit)
            return
        if (viewModel.arrayOpt.isNullOrEmpty() || viewModel.arrayNode.isNullOrEmpty()) {
            return
        }
        isInit = true

        var useSize = 0

        viewModel.arrayNode?.forEach {
            if (it.main != DiskSpaceModel.MAIN_UNKNOWN)
                useSize++
        }

        //这个是快速模式的取交集模式
        when (useSize) {
            2 -> {
                if (containOpt(DiskSpaceModel.RADIO0_MODE)) radio0_cl.visibility = View.VISIBLE
                if (containOpt(DiskSpaceModel.RADIO1_MODE)) radio1_cl.visibility = View.VISIBLE
            }
            3 -> {
                if (containOpt(DiskSpaceModel.RADIO5_MODE)) radio5_cl.visibility = View.VISIBLE
            }
            4 -> {
                if (containOpt(DiskSpaceModel.RADIO0_MODE)) radio0_cl.visibility = View.VISIBLE
                if (containOpt(DiskSpaceModel.RADIO1_MODE)) radio1_cl.visibility = View.VISIBLE
                if (containOpt(DiskSpaceModel.RADIO5_MODE)) radio5_cl.visibility = View.VISIBLE
                if (containOpt(DiskSpaceModel.RADIO10_MODE)) radio10_cl.visibility = View.VISIBLE
            }
            5 -> {
                if (containOpt(DiskSpaceModel.RADIO5_MODE)) radio5_cl.visibility = View.VISIBLE
            }
        }

        lvm_cl.tag = lvm_ck
        radio0_cl.tag = radio0_ck
        radio1_cl.tag = radio1_ck
        radio5_cl.tag = radio5_ck
        radio10_cl.tag = radio10_ck
        basic_cl.tag = basic_ck
        lvm_ck.tag = DiskSpaceModel.LVM_MODE
        radio0_ck.tag = DiskSpaceModel.RADIO0_MODE
        radio1_ck.tag = DiskSpaceModel.RADIO1_MODE
        radio5_ck.tag = DiskSpaceModel.RADIO5_MODE
        radio10_ck.tag = DiskSpaceModel.RADIO10_MODE
        basic_ck.tag = DiskSpaceModel.BASIC_MODE
        lvm_cl.setOnClickListener(mGroupClickListener)
        radio0_cl.setOnClickListener(mGroupClickListener)
        radio1_cl.setOnClickListener(mGroupClickListener)
        radio5_cl.setOnClickListener(mGroupClickListener)
        radio10_cl.setOnClickListener(mGroupClickListener)
        basic_cl.setOnClickListener(mGroupClickListener)
        lvm_ck.setOnCheckedChangeListener(mOnCheckedChangeListener)
        radio0_ck.setOnCheckedChangeListener(mOnCheckedChangeListener)
        radio1_ck.setOnCheckedChangeListener(mOnCheckedChangeListener)
        radio5_ck.setOnCheckedChangeListener(mOnCheckedChangeListener)
        radio10_ck.setOnCheckedChangeListener(mOnCheckedChangeListener)

        disk_manage_btn.setOnClickListener {
            lastCheckView?.let { view ->
                devId?.let { it1 ->
                    FormatEnsureDialog(viewModel, it1, view.tag as String).show(
                        childFragmentManager,
                        DiskManageFragment::class.java.simpleName
                    )
                }
            }
        }
    }


    class FormatEnsureDialog(val viewModel: DiskSpaceModel, val devId: String, val mode: String) :
        DialogFragment() {
        private var boolean = false
        private lateinit var edit: EditText
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            val view: View = inflater.inflate(R.layout.dialog_fromat_ensure, null)
            view.findViewById<TextView>(R.id.format_cancel).setOnClickListener {
                dismiss()
            }
            edit = view.findViewById(R.id.format_ensure_edt)
            view.findViewById<View>(R.id.format_ensure_fl).setOnClickListener {
                edit.requestFocus()
                InputMethodUtils.showKeyboard(requireContext(),edit)
            }
            view.findViewById<TextView>(R.id.format_ensure).setOnClickListener {
                when {
                    edit.text.toString().trim() != edit.hint.toString() -> {
                        ToastUtils.showToast(getString(R.string.format_describe_info))
                    }
                    boolean -> {
                        ToastUtils.showToast(getString(R.string.send_request))
                    }
                    else -> {
                        boolean = true
                        viewModel.createDiskActionItem(mode).observe(this, Observer { resource ->
                            if (resource.status == Status.SUCCESS) {
                                if (resource.data == true) {
                                    findNavController().navigate(
                                        R.id.action_manage_to_load,
                                        SelectTypeFragmentArgs(devId).toBundle(),
                                        null,
                                        null
                                    )
                                } else {
                                    ToastUtils.showToast(getString(R.string.operate_failed))
                                }
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showToast(resource.message)
                            }
                            dismiss()
                        })
                    }
                }
            }
            return view
        }
    }
}