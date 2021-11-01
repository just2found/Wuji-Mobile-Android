package net.linkmate.app.ui.nas.safe_box

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.weline.repo.api.NetworkResponseConstant.SAFE_LOCK_STATUS
import io.weline.repo.api.V5HttpErrorNo
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxControlActivity
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.INITIALIZATION
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.LOGIN_TYPE
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.SAFE_BOX_TYPE_KEY
import net.linkmate.app.ui.nas.safe_box.list.SafeBoxNasFileActivity
import net.linkmate.app.util.ToastUtils
import org.view.libwidget.log.L

/**
create by: 86136
create time: 2021/4/28 20:30
Function description:
 */

class SafeBoxEntrance(val devId: String) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_progress, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //必须在onCreate方法中设置
        setStyle(STYLE_NO_FRAME, R.style.ProgressDialogTheme)
    }

    private val IS_DISPLAY_CANCEL_BUTTON = "is_Display_Cancel_Button"

    /*
     * 更新UI
     */
    fun update(isDisplayCancelButton: Boolean) {
        arguments = bundleOf(IS_DISPLAY_CANCEL_BUTTON to isDisplayCancelButton)
        view?.findViewById<View>(R.id.dialog_ib_cancel)?.let {
            it?.visibility = if (isDisplayCancelButton) View.VISIBLE else View.GONE
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        view?.findViewById<TextView>(R.id.tipTextView)?.setText(R.string.loading)
        val ivCancel = view?.findViewById<View>(R.id.dialog_ib_cancel)

        arguments?.getBoolean(IS_DISPLAY_CANCEL_BUTTON)?.let {
            ivCancel?.visibility = if (it) View.VISIBLE else View.GONE
        } ?: let {
            ivCancel?.visibility = View.GONE
        }
        val ivLoadingImage = view?.findViewById<ImageView>(R.id.img)
        ivLoadingImage?.let {
            //加载gif
            Glide.with(this).asGif().load(R.drawable.loading)
                .transition(DrawableTransitionOptions.withCrossFade()).into(it)
        }
    }


    private val viewModel by viewModels<SafeBoxModel>()


    //1上锁，0解锁
    override fun onResume() {
        val params = dialog?.window?.attributes
        val d = getActivity()?.getWindowManager()?.getDefaultDisplay()
        d?.let {
            val point = Point()
            d.getSize(point)
            //设置宽为屏幕3/4
            params?.width = point.x
            params?.height = point.y
            dialog?.window?.attributes = params
        }
        super.onResume()
        L.i(devId, "onResume", "SafeBoxEntrance", "nwq", "2021/5/13");
        viewModel.querySafeBoxStatus(devId).observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                if (it.data?.question.isNullOrEmpty()) {
                    context?.startActivity(
                        Intent(context, SafeBoxControlActivity::class.java)
                            .putExtra(
                                io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                devId
                            )
                            .putExtra(SAFE_BOX_TYPE_KEY, INITIALIZATION)
                    )
                } else if (it.data?.lock == SAFE_LOCK_STATUS) {
                    context?.startActivity(
                        Intent(context, SafeBoxControlActivity::class.java)
                            .putExtra(
                                io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                devId
                            )
                            .putExtra(SAFE_BOX_TYPE_KEY, LOGIN_TYPE)
                    )
                } else {
                    context?.startActivity(
                        Intent(context, SafeBoxNasFileActivity::class.java)
                            .putExtra(
                                io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                devId
                            ) //是否是EN服务器
                    )
                }
            } else if (it.status == Status.ERROR) {
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
            }
            dismiss()
        })


    }

}