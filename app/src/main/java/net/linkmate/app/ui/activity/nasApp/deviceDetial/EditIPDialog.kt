package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.ip_dialog_edit.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.sdvn.nascommon.utils.InputMethodUtils

/**
 * @author Raleigh.Luo
 * date：20/7/30 16
 * describe：
 */
class EditIPDialog(context: Context): Dialog(context, R.style.DialogTheme) {
    private var tvPositive:TextView?=null
    private var tvNegative:TextView?=null
    var title:String?=null
        set(value) {
            if(tvTitle!=null){
                tvTitle.text = value
            }
            field=value
        }
    var ip:String?=null
        set(value) {
            if(etIP!=null){
                etIP.setText(value)
            }
            field=value
        }
    var mask:String?=null
        set(value) {
            if(etMark!=null){
                etMark.setText(value)
            }
            field=value
        }
    var deleteBtnVisibility = View.GONE
        set(value) {
            if(tvDelete!=null){
                tvDelete.visibility = value
                vDeleteLine.visibility = value
            }
            field=value
        }
    var positiveText:String?=null
        set(value) {
            tvPositive?.setText(value)
            field
        }
    var negativeText:String?=null
        set(value) {
            tvNegative?.setText(value)
            field
        }
    // 记录编辑位置
    var position:Int=-1
    var onClickListener: View.OnClickListener?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LayoutInflater.from(getContext()).inflate(R.layout.ip_dialog_edit, null))

        etIP.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                tvIP.visibility = if (etIP.text.trim().length > 0) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        etMark.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                tvMark.visibility = if (etMark.text.trim().length > 0) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        tvTitle.text = title
        etIP.setText(ip)
        etMark.setText(mask)
        tvDelete.visibility = deleteBtnVisibility
        vDeleteLine.visibility = deleteBtnVisibility
        InputMethodUtils.showKeyboard(context, etIP, 200)
        tvPositive = findViewById(R.id.positive)
        tvNegative = findViewById(R.id.negative)
        if (positiveText != null) {
            tvPositive?.text = positiveText
            tvPositive?.visibility = View.VISIBLE

            if (negativeText != null) {
                tvNegative?.text = negativeText

            }

        }
        tvPositive?.setOnClickListener {
            onClickListener?.onClick(it)
        }
        tvNegative?.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        tvDelete?.setOnClickListener {
            onClickListener?.onClick(it)
        }
        setCancelable(true)
        setCanceledOnTouchOutside(true)

    }
    fun getIPContext(): String? {
        val sIp: String = etIP.getText().toString().trim({ it <= ' ' })
        val r=Regex("^(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$")
        if (sIp.matches(r)) {
            val sMask: String = getMaskContent()?:""
            if (!TextUtils.isEmpty(sMask)) {
                val ipSplit = sIp.split("\\.").toTypedArray()
                val maskSplit = sMask.split("\\.").toTypedArray()
                if (ipSplit.size == 4 && maskSplit.size == 4) {
                    val sb = StringBuilder()
                    for (i in 0..3) {
                        val ipBlock = Integer.valueOf(ipSplit[i]) and Integer.valueOf(maskSplit[i])
                        sb.append(ipBlock)
                        if (i <= 2) sb.append(".")
                    }
                    return sb.toString()
                }
            }
            return sIp
        }
        return null
    }
    fun getMaskContent(): String? {
        val s: String = etMark.getText().toString().trim({ it <= ' ' })
        val r=Regex(MyConstants.REGEX_MASK)
        return if (s.matches(r)) {
            s
        } else try {
            var integer = Integer.valueOf(s)
            if (1 <= integer && integer <= 32) {
                val mask = java.lang.StringBuilder()
                val binary = java.lang.StringBuilder()
                var i = 32
                var count = 0
                while (i > 0) {
                    if (count == 8) {
                        count = 0
                        mask.append(Integer.valueOf(binary.toString(), 2))
                        mask.append(".")
                        binary.delete(0, binary.length)
                    }
                    if (integer > 0) {
                        binary.append(1)
                    } else {
                        binary.append(0)
                    }
                    i--
                    integer--
                    count++
                }
                mask.append(Integer.valueOf(binary.toString(), 2))
                return mask.toString()
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}