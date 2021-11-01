package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.arch.core.util.Function
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceSpaceRepository
import net.linkmate.app.ui.nas.helper.HdManageActivity
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.utils.ToastHelper

/** 设备空间
 * @author Raleigh.Luo
 * date：20/7/27 14
 * describe：
 */
class DeviceSpaceAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                         viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val repository = DeviceSpaceRepository()
    val TOTAL_SPACE = 0
    val SYSTEM_SPACE = 1
    val USER_SPACE = 2
    val CLEAR_SPACE = 3
    val mDatas: HashMap<Int, String> = HashMap()
    val isM8 = UiUtils.isM8(viewModel.device.getDevClass())
    private var diskCount = 0

    init {
        mDatas.put(TOTAL_SPACE, context.getString(R.string.querying))
        mDatas.put(SYSTEM_SPACE, context.getString(R.string.querying))
        mDatas.put(USER_SPACE, context.getString(R.string.querying))
        viewModel.setLoadingStatus(true)
        repository.querySpace(viewModel.mStateListener, viewModel.device.id, viewModel.device.vip, isM8,
                mDatas, Function {
            //获取到数据
            viewModel.setLoadingStatus(false)
            notifyDataSetChanged()
            null
        }, Function {
            //是否可格式化，
            it?.let {
                diskCount = it
                //占位置，不需要赋值数据
                mDatas.put(CLEAR_SPACE, "")
                notifyDataSetChanged()
            }
            null
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 3) CLEAR_SPACE else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(if (viewType == CLEAR_SPACE) R.layout.dialog_device_item_detail
                else R.layout.dialog_device_item_status, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == CLEAR_SPACE) {
            with(holder.itemView) {
                isVisible = true
                val menu = FunctionHelper.getDeviceMenu(FunctionHelper.FORMATE_HARD_DISK)
                ivDeviceDetailIcon.setImageResource(menu.icon)
                ivDeviceDetailTitle.text = menu.title
                setOnClickListener {
                    //是否已经被拦截处理
                    val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                    //没有拦截，则可以内部处理
                    if (!isInterceptor) internalItemClick(it, position)
                }
            }
        } else {
            with(holder.itemView) {
                when (position) {
                    TOTAL_SPACE -> {
                        tvStatusTitle.setText(R.string.total_space)
                        tvStatusContent.setText(mDatas.get(TOTAL_SPACE))
                    }
                    SYSTEM_SPACE -> {
                        if (isM8) {
                            tvStatusTitle.setText(R.string.system_space)
                        } else {
                            tvStatusTitle.setText(R.string.available_space)
                        }
                        tvStatusContent.setText(mDatas.get(SYSTEM_SPACE))
                    }
                    USER_SPACE -> {
                        if (isM8) {
                            tvStatusTitle.setText(R.string.user_space)
                        } else {
                            tvStatusTitle.setText(R.string.used_space)
                        }
                        tvStatusContent.setText(mDatas.get(USER_SPACE))
                    }
                    else -> {

                    }
                }
            }
            //            ivStatusEdit
        }


    }

    override fun internalItemClick(view: View, position: Int) {
        if (diskCount > 0) {
            val intent = Intent(context.requireContext(), HdManageActivity::class.java)
            intent.putExtra("count", diskCount.toString())
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.device.id)
            context.startActivity(intent)
            viewModel.toFinishActivity()
        } else {
            ToastHelper.showToast(context.getString(R.string.tip_no_sata), Toast.LENGTH_SHORT)
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}