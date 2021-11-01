package net.linkmate.app.data.model

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import net.sdvn.nascommon.utils.AnimUtils

class InputFloatTextWatcher(val et: EditText) : TextWatcher {
    var deleteLastChar = false// 是否需要删除末尾

    override fun afterTextChanged(s: Editable?) {
        if (s.isNullOrEmpty()) return
        if (deleteLastChar) {
            // 设置新的截取的字符串
            et.setText(s.toString().substring(0, s.toString().length - 1));
            // 光标强制到末尾
            et.setSelection(et.getText().length)
            AnimUtils.sharkEditText(et)
        }
        // 以小数点开头，前面自动加上 "0"
        if (s.toString().startsWith(".")) {
            et.setText("0$s");
            et.setSelection(et.getText().length);
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.isNullOrEmpty()) {
            return
        }
        if (s.toString().contains(".")) {
            // 如果点后面有超过三位数值,则删掉最后一位
            val length = s.length - s.toString().lastIndexOf(".");
            // 说明后面有三位数值
            deleteLastChar = length >= 4;
        }
    }
}