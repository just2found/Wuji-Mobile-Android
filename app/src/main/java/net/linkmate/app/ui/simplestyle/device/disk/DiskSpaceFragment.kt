package net.linkmate.app.ui.simplestyle.device.disk


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_disk_space.*
import libs.source.common.livedata.Status
import libs.source.common.livedata.Status.SUCCESS
import net.linkmate.app.R
import net.linkmate.app.ui.adapter.DiskNodeAdapter
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskNode
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskSpaceOverview
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.utils.InputMethodUtils

class DiskSpaceFragment : TipsBaseFragment() {

    private val viewModel by viewModels<DiskSpaceModel>({ requireParentFragment() })

    private val mDeviceList = ArrayList<String>()

    override fun getLayoutResId(): Int {
        return R.layout.fragment_disk_space
    }


    override fun onResume() {
        super.onResume()
        if (!viewModel.arrayNode.isNullOrEmpty()) {
            initDiskList(viewModel.arrayNode!!)
        } else {
            viewModel.getDiskNodeInfo().observe(this, Observer {
                if (it.status == SUCCESS) {
                    viewModel.arrayNode = it.data
                    viewModel.isGetNode = true
                    viewModel.checkDiskPower()
                    viewModel.arrayNode?.let { it1 -> initDiskList(it1) }
                }
            })
        }

        if (viewModel.arrayOpt.isNullOrEmpty()) {
            viewModel.getDiskActionItem().observe(this, Observer {
                if (it.status == SUCCESS) {
                    viewModel.arrayOpt = it.data
                    viewModel.arrayOpt!!.forEach { actionItem ->
                        if (actionItem.op == DiskSpaceModel.OPT_EXTEND) {
                            extend_format_btn.visibility = View.VISIBLE
                        }
                        if (actionItem.op == DiskSpaceModel.OPT_CREATE) {
                            create_format_btn.visibility = View.VISIBLE
                        }
                    }
                }
            })
        } else {
            viewModel.arrayOpt!!.forEach {
                if (it.op == DiskSpaceModel.OPT_EXTEND) {
                    extend_format_btn.visibility = View.VISIBLE
                }
                if (it.op == DiskSpaceModel.OPT_CREATE) {
                    create_format_btn.visibility = View.VISIBLE
                }

            }
        }

        if (viewModel.arrayPower.isNullOrEmpty()) {
            viewModel.getDiskPowerStatus().observe(this, Observer {
                if (it.status == SUCCESS) {
                    viewModel.arrayPower = it.data
                    if (viewModel.checkDiskPower()) {
                        viewModel.arrayNode?.let { it1 -> initDiskList(it1) }
                    }
                }
            })
        }


        if (viewModel.diskSpaceOverview != null) {
            setDiskSpace(viewModel.diskSpaceOverview!!)
        } else {
            viewModel.getDiskStorageSpace().observe(this, Observer {
                if (it.status == SUCCESS) {
                    viewModel.diskSpaceOverview = it.data
                    setDiskSpace(it.data!!)
                }
            })
        }

    }


    override fun initView(view: View) {
        viewModel.init(devId!!)
        //如果正在格式化则
        viewModel.getDiskManageStatus().observe(this, Observer {
            if (it.status == Status.LOADING) {
                findNavController().navigate(R.id.action_space_to_load, SelectTypeFragmentArgs(devId).toBundle(), null, null)
            }
        })
        create_format_btn.setOnClickListener {
            if (!viewModel.arrayNode.isNullOrEmpty() && !viewModel.arrayOpt.isNullOrEmpty()) //只有拥有磁盘节点数的时候才能点击
                findNavController().navigate(R.id.action_space_to_manage, SelectTypeFragmentArgs(devId).toBundle(), null, null)
            else ToastUtils.showToast(getString(R.string.in_get_operation))
        }
        extend_format_btn.setOnClickListener {
            ExtendEnsureDialog(viewModel, devId!!).show(childFragmentManager, DiskManageFragment::class.java.simpleName)
        }

    }


    private fun setDiskSpace(diskSpaceOverview: DiskSpaceOverview) {
        pie_chart_cl.visibility = View.VISIBLE
        disk_space_ll.visibility = View.VISIBLE
        if (diskSpaceOverview.proportion.size == 2) {
            pie_chart.setData(diskSpaceOverview.proportion.toIntArray(), diskSpaceOverview.proportion[0].toString() + "%",
                    intArrayOf(
                            ContextCompat.getColor(requireContext(), R.color.color_0d80f8),
                            ContextCompat.getColor(requireContext(), R.color.color_e2edfa)
                    ))
            //如果不是M8的就不显示磁盘空间大小  有数据再进行展示
            available_color_v.visibility = View.VISIBLE
            available_title_tv.visibility = View.VISIBLE
            available_content_tv.visibility = View.VISIBLE

            already_color_v.visibility = View.VISIBLE
            already_title_tv.visibility = View.VISIBLE
            already_content_tv.visibility = View.VISIBLE

            disk_space_total.text = diskSpaceOverview.totalSpace
            already_content_tv.text = diskSpaceOverview.usedSpace
            available_content_tv.text = diskSpaceOverview.freeSpace
        } else if (diskSpaceOverview.proportion.size == 3) {
            pie_chart.setData(diskSpaceOverview.proportion.toIntArray(), diskSpaceOverview.proportion[1].toString() + "%",
                    intArrayOf(ContextCompat.getColor(requireContext(), R.color.color_7cbaf9),
                            ContextCompat.getColor(requireContext(), R.color.color_0d80f8),
                            ContextCompat.getColor(requireContext(), R.color.color_e2edfa)
                    )
            )
            //如果不是M8的就不显示磁盘空间大小  有数据再进行展示
            sys_title_tv.visibility = View.VISIBLE
            sys_color_v.visibility = View.VISIBLE
            sys_content_tv.visibility = View.VISIBLE

            available_color_v.visibility = View.VISIBLE
            available_title_tv.visibility = View.VISIBLE
            available_content_tv.visibility = View.VISIBLE

            already_color_v.visibility = View.VISIBLE
            already_title_tv.visibility = View.VISIBLE
            already_content_tv.visibility = View.VISIBLE

            sys_content_tv.text = diskSpaceOverview.systemSpace
            disk_space_total.text = diskSpaceOverview.totalSpace
            already_content_tv.text = diskSpaceOverview.usedSpace
            available_content_tv.text = diskSpaceOverview.freeSpace
        }
    }

    private fun initDiskList(list: List<DiskNode>) {
        mDeviceList.clear()
        if (!list.isNullOrEmpty()) {
            val s = getString(R.string.disk_manage)
            var type = getString(R.string.nothing)
            val noneStr = "none"
            list.forEach {
                mDeviceList.add(it.device)
                if (it.main == 1 && it.mode != noneStr) {
                    type = it.mode
                }
            }
            disk_manage_tv.text = "$s($type)"
            val adapter = DiskNodeAdapter(list, requireActivity())
            disk_rcv.layoutManager = LinearLayoutManager(requireActivity())
            disk_rcv.adapter = adapter
        }
    }


    class ExtendEnsureDialog(val viewModel: DiskSpaceModel, val devId: String) : DialogFragment() {
        private var boolean = false
        private lateinit var edit: EditText
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            val view: View = inflater.inflate(R.layout.dialog_fromat_ensure, null)
            view.findViewById<TextView>(R.id.format_cancel).setOnClickListener {
                dismiss()
            }
            view.findViewById<TextView>(R.id.format_ensure_title).text = getString(R.string.extend_ensure_title)

            edit = view.findViewById(R.id.format_ensure_edt)
            view.findViewById<TextView>(R.id.format_ensure_info).text = getString(R.string.extend_describe_info)
            edit.hint = getString(R.string.extend_ensure_info)
            view.findViewById<View>(R.id.format_ensure_fl).setOnClickListener {
                edit.requestFocus()
                InputMethodUtils.showKeyboard(requireContext(),edit)
            }
            view.findViewById<TextView>(R.id.format_ensure).setOnClickListener {
                when {
                    edit.text.toString().trim() != edit.hint.toString() -> {
                        ToastUtils.showToast(getString(R.string.extend_describe_info))
                    }
                    boolean -> {
                        ToastUtils.showToast(getString(R.string.send_request))
                    }
                    else -> {
                        boolean = true
                        viewModel.extendDiskActionItem().observe(this, Observer { resource ->
                            if (resource.status == SUCCESS) {
                                if (resource.data == true) {
                                    findNavController().navigate(R.id.action_space_to_load, SelectTypeFragmentArgs(devId).toBundle(), null, null)
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