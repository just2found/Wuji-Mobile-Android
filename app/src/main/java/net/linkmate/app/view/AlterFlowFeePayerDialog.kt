package net.linkmate.app.view

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.dialog_alter_flow_fee_payer.*
import kotlinx.android.synthetic.main.dialog_hint.*
import net.linkmate.app.R

/**
 * @author Raleigh.Luo
 * date：21/4/23 10
 * describe：更改流量付费方弹框
 */
class AlterFlowFeePayerDialog : DialogFragment() {
    //Int chargetype 收费方式 1-使用者付费 2-拥有者付费
    var callBackFunction: Function<Int, Void>? = null

    var onDismissListener: DialogInterface.OnDismissListener? = null
    var onShowListener: DialogInterface.OnShowListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_alter_flow_fee_payer, null);
    }

    private val CHARGE_TYPE = "chargetype"
    fun update(chargetype: Int) {
        arguments = bundleOf(CHARGE_TYPE to chargetype)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //必须在onCreate方法中设置
        setStyle(STYLE_NO_FRAME, R.style.DialogTheme2)
    }

    override fun onResume() {
        view?.let {
            with(it) {
                mRadioGroup.setOnCheckedChangeListener(null)
                arguments?.getInt(CHARGE_TYPE)?.let {
                    mRadioGroup.check(if (it == 2) R.id.rbOwnerToPay else R.id.rbUserToPay)
                }
                mRadioGroup.setOnCheckedChangeListener { radioGroup, id ->
                    //返回chargetype 收费方式 1-使用者付费 2-拥有者付费
                    if (id == R.id.rbOwnerToPay) {
                        callBackFunction?.apply(2)
                    } else {
                        callBackFunction?.apply(1)
                    }
                }
                btnCancel.setOnClickListener {
                    dismiss()
                }
            }
        }
        val params = dialog?.window?.attributes
        val d = getActivity()?.getWindowManager()?.getDefaultDisplay()
        d?.let {
            val point = Point()
            d.getSize(point)
            //设置宽为屏幕3/4
            params?.width = point.x
            params?.gravity = Gravity.BOTTOM
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