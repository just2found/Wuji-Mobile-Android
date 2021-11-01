package net.linkmate.app.ui.activity.circle.circleDetail.adapter.share

import android.graphics.Bitmap
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_device_item_qrcode.view.*
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import kotlinx.android.synthetic.main.dialog_device_item_switch.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.MessageManager
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.ShareUtil
import net.sdvn.cmapi.util.ClipboardUtils

/** 圈子分享
 * @author Raleigh.Luo
 * date：20/8/14 15
 * describe：
 */
class CircleShareAdapter(context: Fragment, val viewModel: CircleDetialViewModel,
                         fragmentViewModel: CircleShareViewModel) : DialogBaseAdapter<CircleShareViewModel>(context, fragmentViewModel) {


    // 分享码
    private var mShareCode: String? = null

    // 分享提示
    private var mShartTips: String? = null

    // 分享码图片
    private var mShareBitmap: Bitmap? = null

    private var isEnableShare = true

    private var isJoinConfirm = viewModel.circleDetail.value?.join_confirm ?: false

    init {
        //设置头部显示圈子名称
        fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname, bottomIsEnable = false)
        initObserver()
        if (isEnableShare) fragmentViewModel.startRequestRemoteSource()
    }

    private fun initObserver() {
        viewModel.cancelRequest.observe(context, Observer {
            if (it){ //用户请求取消了
                isJoinConfirm = viewModel.circleDetail.value?.join_confirm ?: false
                notifyDataSetChanged()
            }

        })
        fragmentViewModel.shareCodeResult.observe(context, Observer {
            viewModel.setLoadingStatus(true)
            mShareCode = it.sharecode
            //生成二维码图片
            ShareUtil.generateQRCode(context, MyConstants.EVENT_CODE_CIRCLE_NETWORK, mShareCode, it.expireTime, object : ShareUtil.QRCodeResult {
                override fun onGenerated(bitmap: Bitmap?, tips: String?) {
                    fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                    mShartTips = tips
                    mShareBitmap = bitmap
                    viewModel.setLoadingStatus(false)
                    notifyDataSetChanged()
                    MessageManager.getInstance().quickDelay()
                }
            })
        })
        fragmentViewModel.setNetworkConfirmResult.observe(this@CircleShareAdapter.context, Observer {
            if (it) {
                viewModel.circleDetail.value?.join_confirm = isJoinConfirm
            } else {
                isJoinConfirm = viewModel.circleDetail.value?.join_confirm ?: false
            }
            //            notifyItemChanged(3)
            notifyItemChanged(2)
        })
    }

    override fun getItemViewType(position: Int): Int {
//        if (!isEnableShare) {
//            return TYPE_SWITCH
//        } else {
        when (position) {
            0 -> {
                return TYPE_QRCODE
            }
//                2, 3 -> {
            2 -> {
                return TYPE_SWITCH
            }
            else -> {
                return TYPE_STATUS
            }
        }
//        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = R.layout.dialog_device_item_status
        when (viewType) {
            TYPE_QRCODE -> {
                layout = R.layout.dialog_device_item_qrcode
            }
            TYPE_SWITCH -> {
                layout = R.layout.dialog_device_item_switch
            }
        }
        val view = LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
//        return if (isEnableShare) 4 else 1
        return 3
    }

    private var mIvQRCode: View? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_QRCODE -> {
                with(holder.itemView) {
                    ivQRCode.setImageBitmap(mShareBitmap)
                    tvTitle.setText(context.getString(R.string.pls_scan_qrcode, context.getString(R.string.app_name)))
                    tvContent.setText(mShartTips)
                    mIvQRCode = panelQRCode
                }
            }
            TYPE_SWITCH -> {
                with(holder.itemView) {
//                    if (position == 0 || position == 2) {//启用分享
//                        mSwitch.setChecked(isEnableShare)
//                        mSwitch.text = context.getString(R.string.enable_share)
//                        mSwitch.setOnCheckedChangeListener { compoundButton, b ->
//                            viewModel.savedEnableShareState(b, object : Function<Boolean, Void?> {
//                                override fun apply(isSuccess: Boolean?): Void? {
//                                    mSwitch.setChecked(viewModel.device.getHardData()?.getEnableshare()
//                                            ?: false)
//                                    if(isSuccess?:false){
//                                        if(mSwitch.isChecked){
//                                            getDeviceShareCode()
//                                        }else{
//                                            //按钮不可用
//                                            fragmentViewModel.setBottomButtonEnable(false)
//                                            ////释放图片
//                                            onDestory()
//                                            notifyDataSetChanged()
//                                        }
//                                    }
//                                    return null
//                                }
//                            })
//                        }
//                    } else {//添加设备需要验证
                    mSwitch.setOnCheckedChangeListener(null)
                    mSwitch.text = context.getString(R.string.circle_tips_share_need_auth)
                    mSwitch.setChecked(isJoinConfirm)
                    mSwitch.setOnCheckedChangeListener { compoundButton, b ->
                        isJoinConfirm = b
                        fragmentViewModel.startSetNetworkConfirm(b)
                    }
//                    }
                }
            }
            else -> {
                with(holder.itemView) {
                    if (position == 1) {//分享码
                        tvStatusTitle.text = context.getString(R.string.share_code)
                        tvStatusContent.text = mShareCode
                        ivStatusEdit.setImageResource(R.drawable.icon_copy_blue)
                        ivStatusEdit.visibility = View.VISIBLE
                        this.setOnClickListener {
                            //是否已经被拦截处理
                            val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                            //没有拦截，则可以内部处理
                            if (!isInterceptor) internalItemClick(it, position)
                        }
                    }
                }
            }
        }
    }

    private var retryTimes = 0
    override fun internalItemClick(view: View, position: Int) {
        if (position == 1) {//复制分享码
            ClipboardUtils.copyToClipboard(context.requireContext(), mShareCode)
            ToastUtils.showToast(context.getString(R.string.Copied).toString() + mShareCode)
        } else {//分享二维码
            retryTimes = 0
            //保存二维码到本地
            if (mIvQRCode != null && mShareCode != null) {
                saveImageToFile(view, position)
            }
        }
    }

    private val handler = Handler()
    private fun saveImageToFile(view: View, position: Int) {
        //保存二维码到本地
        ShareUtil.saveImageToFile(mIvQRCode, context, mShareCode, object : ShareUtil.SaveImageResult {
            override fun onSuccess() {
                retryTimes = 0
                ShareUtil.shareFile(context.requireContext(), mShareCode)
            }

            override fun onError() {
                if (retryTimes < 5) {
                    retryTimes++
                    //重试
                    handler.postDelayed(Runnable {
                        notifyDataSetChanged()
                        saveImageToFile(view, position)
                    }, 500)

                }

            }
        })
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    override fun onDestory() {
        //释放图片
        mShareBitmap?.recycle()
        mShareBitmap = null
    }
}