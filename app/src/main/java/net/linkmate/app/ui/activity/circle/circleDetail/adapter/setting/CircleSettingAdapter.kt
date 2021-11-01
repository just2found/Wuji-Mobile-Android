package net.linkmate.app.ui.activity.circle.circleDetail.adapter.setting

import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialFragmentDirections
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.CircleSettingFeesActivity
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.HintDialog
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.DialogUtils

/**设置
 * @author Raleigh.Luo
 * date：20/8/17 09
 * describe：
 */
class CircleSettingAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleSettingViewModel, val navController: NavController)
    : DialogBaseAdapter<CircleSettingViewModel>(context, fragmentViewModel) {
    private val menus: ArrayList<FunctionHelper.DetailMenu> = ArrayList()


    init {
        //设置头部显示圈子名称
        fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname)
        getItemSources()
        initObserver()
    }

    private fun initObserver() {
        fragmentViewModel.exitCircleResult.observe(context, Observer {
            if (it) {//退出圈子成功
                ToastUtils.showToast(R.string.exit_circle_success)
                context.activity?.setResult(FunctionHelper.CIRCLE_EXIT)
                viewModel.toFinishActivity()
            }
        })
        fragmentViewModel.alterCircleNameResult.observe(context, Observer {
            if (it) {
                NetsRepo.refreshNetList()//设置为En服务器
                        .setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener {
                            override fun onLoadComplete() {
                                viewModel.setLoadingStatus(false)
                                context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                ToastUtils.showToast(R.string.modify_succ)
                                viewModel.circleDetail.value?.networkname = fragmentViewModel.alterCircleName.value
                                fragmentViewModel.updateViewStatusParams(headerTitle = fragmentViewModel.alterCircleName.value)
                                renameDialog?.dismiss()
                            }

                            override fun onLoadStart(disposable: Disposable?) {
                            }

                            override fun onLoadError() {
                                viewModel.setLoadingStatus(false)
                                context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                ToastUtils.showToast(R.string.modify_succ)
                                viewModel.circleDetail.value?.networkname = fragmentViewModel.alterCircleName.value
                                fragmentViewModel.updateViewStatusParams(headerTitle = fragmentViewModel.alterCircleName.value)
                                renameDialog?.dismiss()
                            }

                        })

            }else{
                viewModel.setLoadingStatus(false)
            }
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_detail, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            var index = position
            val menu = menus[index]
            ivDeviceDetailIcon.setImageResource(menu.icon)
            ivDeviceDetailIcon.visibility = if (menu.icon == 0) View.GONE else View.VISIBLE
            ivDeviceDetailTitle.setText(menu.title)
            setTag(menu.function)
            setOnClickListener {
                //是否已经被拦截处理
                val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                //没有拦截，则可以内部处理
                if (!isInterceptor) internalItemClick(it, position)
            }
        }
    }

    private var renameDialog: DialogInterface? = null

    /**
     * 重命名
     */
    private fun rename() {
        DialogUtils.showEditDialog(context.requireContext(), R.string.rename,
                R.string.empty, viewModel.circleDetail.value?.networkname,
                R.string.max_name_length,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn, mContentEditText ->
            if (isPositiveBtn) {
                val newName = mContentEditText.text.toString().trim { it <= ' ' }
                if (newName.isNullOrEmpty() || newName.length > 16) {
                    AnimUtils.sharkEditText(context.requireContext(), mContentEditText)
                } else {
                    fragmentViewModel.startAlterCircleName(newName)
                    renameDialog = dialog
                }
            }
        }
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.getTag() == null) return
        val function = view.getTag() as Int
        when (function) {
            FunctionHelper.CIRCLE_ALTER_NAME -> {
                rename()
            }
            FunctionHelper.CIRCLE_EXIT -> {
//                等待同意的圈子显示到圈子列表、圈子管理列表中，作离线表示，并在简介位置显示“等待同意”
//                点击后允许操作的选项：
//                1.状态
//                2.设置-移除，移除时弹窗询问“你确定要移除此圈子吗？/确定/取消”
                if (viewModel.isNomorlCircle) {
                    hintDialog?.update(context.getString(R.string.exit_circle),
                            context.getString(R.string.exit_circle_warning), hintColor = R.color.red,
                            confrimText = context.getString(R.string.confirm),
                            cancelText = context.getString(R.string.cancel)
                    )
                } else {
                    hintDialog?.update(context.getString(R.string.remove),
                            context.getString(R.string.remove_circle_hint), hintColor = R.color.red,
                            confrimText = context.getString(R.string.confirm),
                            cancelText = context.getString(R.string.cancel)
                    )
                }

                if (hintDialog?.dialog?.isShowing != true) {
                    hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
                }
            }
            FunctionHelper.CIRCLE_SETTING_JOIN_WAY -> {
                context.requireActivity().startActivity(Intent(context.requireContext(), CircleSettingFeesActivity::class.java)
                        .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId))
            }
            else -> {
                navController.navigate(CircleDetialFragmentDirections.enterDetial(function))
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    /**
     * 创建提示Dialog
     */
    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance()
                field?.setOnClickListener(View.OnClickListener {
                    if (it.id == R.id.positive) {//确定按钮
                        if (viewModel.circleDetail.value?.isOwner() ?: false == false) {//非所有者，所有者不能退出圈子
                            //请求退出圈子
                            fragmentViewModel.startExitCircle()
                        }

                    }
                })
                field?.onDismissListener = DialogInterface.OnDismissListener {
                    viewModel.setSecondaryDialogShow(false)
                }
                field?.onShowListener = DialogInterface.OnShowListener {
                    viewModel.setSecondaryDialogShow(true)
                }
            }
            return field
        }

    fun getItemSources() {
        menus.clear()
        viewModel.circleDetail.value?.let {
            if (viewModel.isNomorlCircle) {
                if (it.isOwner() || it.isManager()) {
                    menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_ALTER_NAME))
                }
                if (it.ischarge ?: false && (it.isOwner() || it.isManager())) {
                    if (it.isOwner()) menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_SETTING_MAIN_EN_DEVICE))
                    menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_SETTING_JOIN_WAY))
                    menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_EN_SERVER_FEES))
                }
            }
            if (!it.isOwner()) {
                val function = FunctionHelper.getMenu(FunctionHelper.CIRCLE_EXIT)
                if (!viewModel.isNomorlCircle) function.title = context.getString(R.string.remove)
                menus.add(function)
            }
        }
    }
}