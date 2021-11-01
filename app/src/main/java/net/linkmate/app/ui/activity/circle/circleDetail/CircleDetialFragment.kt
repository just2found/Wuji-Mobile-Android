package net.linkmate.app.ui.activity.circle.circleDetail

import android.content.Context
import android.graphics.Point
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_device_layout.*
import kotlinx.android.synthetic.main.include_dialog_device_bottom.*
import kotlinx.android.synthetic.main.include_dialog_device_header.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FullyLinearLayoutManager
import net.sdvn.common.vo.BriefModel


/**
 * @author Raleigh.Luo
 * date：20/8/13 17
 * describe：
 */
open class CircleDetialFragment : Fragment() {

    //activity生命周期viewMdole
    val viewModel: CircleDetialViewModel by activityViewModels()
    lateinit var fragmentViewModel: CircleFragmentViewModel
    protected var adapter: DialogBaseAdapter<CircleFragmentViewModel>? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_device_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.setLoadingStatus(false)
        ivHeaderImage.setImageResource(R.drawable.icon_circle_add)
        val params by navArgs<CircleDetialFragmentArgs>()
        adapter = FunctionHelper.initFragmentPanel(params.function, this, viewModel, findNavController())
        fragmentViewModel = adapter!!.fragmentViewModel
        fragmentViewModel.function = params.function

        initEvent()
        initObserver()
        recyclerView.layoutManager = FullyLinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        //不使用缓存
        recyclerView.setItemViewCacheSize(-1)
        adapter?.onCheckedChangeListener = onCheckedChangeListener
        adapter?.onItemClickListener = onItemClickListener
        adapter?.onItemLongClickListener = onItemLongClickListener
        initHeaderStyle()
        ivHeaderClose.setOnClickListener {
            //没有被拦截
            if (adapter?.interceptFinishActivity() == false)
                viewModel.toFinishActivity()
        }
        ivHeaderBack.setOnClickListener {
            //没有被拦截
            if (adapter?.interceptBackPressed() == false)
                viewModel.toBackPress()
        }
        //点击顶部空白位置关闭页面
        vEmpty.setOnClickListener {
            viewModel.toFinishActivity()
        }
        //点击顶部空白位置关闭页面
        vEmptyItemHeight.setOnClickListener {
            viewModel.toFinishActivity()
        }

    }

    /**
     * 临时 初始化头部样式
     */
    private fun initHeaderStyle() {
        when (fragmentViewModel.function) {
            FunctionHelper.CIRCLE_SETTING_FEES_DETIAL -> {//加入方式详情
                tvHeaderTitle.setText("")
            }
            FunctionHelper.CIRCLE_EN_DEVICE_DETAIL -> {//en设备详情
                //去掉头部加粗样式
                tvHeaderTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
            }
            FunctionHelper.CIRCLE_MEMBER_DETAIL -> {
                //去掉头部加粗样式
                tvHeaderTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
            }
        }
    }

    private fun initEvent() {
        btnBottomConfirm.setOnClickListener {
            adapter?.internalItemClick(it, -1)
        }
        btnBottomAdd.setOnClickListener {
            adapter?.internalItemClick(it, -1)
        }
        flBottom.setOnClickListener {
            adapter?.internalItemClick(it, -1)
        }
    }

    private fun initObserver() {
        fragmentViewModel.viewStatusParams.observe(viewLifecycleOwner, Observer {
            initViewStatus(it)
        })
        viewModel.circleBrief.observe(viewLifecycleOwner, Observer {
            var brief: BriefModel? = null
            if (it != null && it.size > 0) {
                brief = it.get(0)
            }
            viewModel.loadBrief(viewModel.startGetCircleBrief.value ?: "",
                    brief, ivImage = ivHeaderImage, defalutImage = R.drawable.icon_circle_add,
                    isLoadOneDeviceBrief = true)
        })
    }

    /**
     * 初始化头部副标题和底部按钮
     */
    private fun initViewStatus(statusParams: FunctionHelper.ViewStatusParams) {
        setBottomPanelStatus(statusParams)
        tvHeaderTitle.text = statusParams.headerTitle
        tvHeaderDescribe.text = statusParams.headerDescribe
        tvHeaderDescribe.visibility = if (TextUtils.isEmpty(statusParams.headerDescribe)) View.GONE else View.VISIBLE
        ivHeaderBack.visibility = statusParams.headBackButtonVisibility

        if (statusParams.headerIcon == 0) {
            ivHeaderImage.visibility = View.GONE
        } else if (statusParams.headerIcon == -1) {//显示默认的
            ivHeaderImage.visibility = View.VISIBLE
        } else {
            ivHeaderImage.visibility = View.VISIBLE
            ivHeaderImage.setImageResource(statusParams.headerIcon)
        }

    }

    /**
     * 初始化底部按钮
     */
    private fun setBottomPanelStatus(params: FunctionHelper.ViewStatusParams) {
        flBottom.visibility = if (TextUtils.isEmpty(params.bottomTitle)) View.GONE else View.VISIBLE
        btnBottomConfirm.isEnabled = params.bottomIsEnable
        btnBottomAdd.isEnabled = params.bottomAddIsEnable
        flBottom.isEnabled = params.bottomIsEnable
        params.bottomTitle?.let {
            if (params.bottomIsFullButton) {//蓝色按钮
                btnBottomConfirm.text = params.bottomTitle
                btnBottomConfirm.visibility = View.VISIBLE
                tvBottom.visibility = View.GONE
            } else {
                tvBottom.text = params.bottomTitle
                tvBottom.visibility = View.VISIBLE
                btnBottomConfirm.visibility = View.GONE
            }
        }
        params.bottomAddTitle?.let {
            btnBottomAdd.text = params.bottomAddTitle
            btnBottomAdd.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(params.bottomTitle)) {//底部有两个按钮时，得重新设置下高度
                val wm = context
                        ?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val defaultDisplay: Display = wm.getDefaultDisplay()
                val point = Point()
                defaultDisplay.getSize(point)
                recyclerView.mHeight = Math.ceil((point.y * 3.toDouble() / 5.toDouble())).toInt()
            }
        }
    }

    private val onItemClickListener = object : DialogBaseAdapter.OnItemClickListener {
        override fun onClick(view: View, position: Int): Boolean {
            /**
             * 返回true: 可拦截adapter适配内部点击事件响应
             * 返回false：不拦截
             */
            return false
        }

    }
    private val onCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(p0: CompoundButton, isChecked: Boolean) {
        }

    }

    private val onItemLongClickListener = object : DialogBaseAdapter.OnItemLongClickListener {
        override fun onLongClick(view: View, position: Int): Boolean {
            /**
             * 返回true: 可拦截adapter适配内部点击事件响应
             * 返回false：不拦截
             */
            return false
        }
    }

    override fun onDestroy() {
        adapter?.onDestory()
        super.onDestroy()

    }
}