package net.linkmate.app.view

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import net.linkmate.app.R

/**
 * @author Raleigh.Luo
 * date：21/3/31 17
 * describe：
 */
class ProgressDialog : DialogFragment() {
    var onDismissListener: DialogInterface.OnDismissListener? = null
    var onShowListener: DialogInterface.OnShowListener? = null
    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): ProgressDialog {
        this.onClickListener = onClickListener
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        ivCancel?.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        arguments?.getBoolean(IS_DISPLAY_CANCEL_BUTTON)?.let {
            ivCancel?.visibility = if (it) View.VISIBLE else View.GONE
        } ?: let {
            ivCancel?.visibility = View.GONE
        }
        val ivLoadingImage = view?.findViewById<ImageView>(R.id.img)
        ivLoadingImage?.let {
            //加载gif
            Glide.with(this).asGif().load(R.drawable.loading).transition(DrawableTransitionOptions.withCrossFade()).into(it)
        }
    }

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
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(null)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        onShowListener?.onShow(null)
        return super.show(transaction, tag)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        onShowListener?.onShow(null)
        super.show(manager, tag)
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        onShowListener?.onShow(null)
        super.showNow(manager, tag)
    }
}