package net.linkmate.app.ui.activity.circle.circleDetail.adapter.manager

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import net.linkmate.app.R
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.circle.CircleBriefActivity
import net.linkmate.app.ui.activity.circle.circleDetail.*
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.CheckStatus
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.NetsRepo

/**圈子管理
 * @author Raleigh.Luo
 * date：20/8/14 14
 * describe：
 */
class CircleManagerAdapter(context: Fragment, val viewModel: CircleDetialViewModel,
                           fragmentViewModel: CircleFragmentViewModel, val navController: NavController)
    : DialogBaseAdapter<CircleFragmentViewModel>(context, fragmentViewModel) {
    private val menus: ArrayList<FunctionHelper.DetailMenu> = ArrayList()

    init {
        //获取圈子详情
        initObserver()
    }

    private fun initObserver() {
        viewModel.circleDetail.observe(context, Observer {
            fragmentViewModel.updateViewStatusParams(headerTitle = it.networkname)
            getItemSources()
            notifyDataSetChanged()
        })
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.getTag() == null) return
        val function = view.getTag() as Int
        when (function) {
            FunctionHelper.CIRCLE_BENEFITS -> {// 查看收益
                context.requireActivity().startActivity(Intent(context.requireContext(), CircleBenefitsActivity::class.java))
            }
            FunctionHelper.CIRCLE_BRIEF -> {//圈子简介
                viewModel.circleDetail.value?.let { circle ->
                    val currentNetwork = NetsRepo.getCurrentNet()
                    //1.检测是否在当前网络
                    if (circle.networkid == currentNetwork?.netId) {
                        viewModel.circleDetail.value?.getMainENDeviceId()?.let {
                            val devId = it
                            val mainENServer = DevManager.getInstance().deviceBeans.find { it.id == devId }
                            mainENServer?.let {
                                //2.检查设备是否在线
                                if (!it.isOnline) {//设备离线
                                    ToastUtils.showToast(R.string.circle_main_en_server_offline)
                                } else {//设备在线
                                    //3.检查设备状态是否正常
                                    CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager,
                                            it, Function {
                                        if (it) {//状态正常，进入简介
                                            CircleBriefActivity.start(context.requireActivity(),
                                                    devId,
                                                    circle.networkname,
                                                    circle.getFullName(),
                                                    circle.isOwner(),
                                                    circle.networkid, FunctionHelper.CIRCLE_BRIEF)
                                        }
                                        null
                                    })
                                }
                            } ?: let {//找不到设备对象
                                ToastUtils.showToast(R.string.circle_main_en_server_offline)
                            }
                        } ?: let {//没有设置主EN
                            if (viewModel.circleDetail.value?.ownerid == CMAPI.getInstance().baseInfo.userId) {
                                ToastUtils.showToast(R.string.please_setting_main_en_device)
                            } else {
                                ToastUtils.showToast(R.string.not_find_main_en)
                            }
                        }
                    } else {//在其它网络
                        ToastUtils.showToast(R.string.switch_circle_for_circle_brief)
                    }
                }
            }
            else -> {
                navController.navigate(CircleDetialFragmentDirections.enterDetial(function))
            }

        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
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

    fun getItemSources() {
        menus.clear()
        //all
        menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_DETAIL))
        viewModel.circleDetail.value?.let {
            if (viewModel.isNomorlCircle) {//圈子正常
                if (it.ischarge ?: false) {//需求2020.11.12： 付费圈子才显示简介
                    //2021/1/8 新增需求显示条件增加 必须是nas＋isV5
                    val mainENServerId = it.getMainENDeviceId()
                    if (!TextUtils.isEmpty(mainENServerId)) {
                        val mainENServer = DevManager.getInstance().deviceBeans.find { it.id == mainENServerId }
                        if (mainENServer?.isOnline ?: false && mainENServer?.isNas ?: false) {
                            menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_BRIEF))
                        }
                    }
                }

                if (it.isOwner() || it.isManager()) {
                    //owner
                    //分享
                    menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_SHARE))
                    //owner
                    //成员管理
                    menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_MEMBER))
                    if (it.ischarge ?: false)
                    //EN设备管理
                        menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_EN_DEVICE))
                }

                //我的圈内设备
                menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_OWN_DEVICE))
                //我的付费项
                if (it.ischarge ?: false) {
                    menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_OWN_FEE_RECORDS))
                    //购买流量
                    if (viewModel.circleDetail.value?.devsepcharge ?: false == false) {
                        menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_PURCHASE_FLOW))
                    }
                }
            }

//    查看收益 暂时隐藏        //partner /owner
//            menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_BENEFITS))
        }
        //all
        menus.add(FunctionHelper.getMenu(FunctionHelper.CIRCLE_SETTING))

    }
}
