package net.linkmate.app.data.model

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import net.linkmate.app.R
import java.util.regex.Pattern

class PasswdTextWatcher(private val etPwd: EditText, private val compile: Pattern) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        if (!compile.matcher(s).matches()) {
            etPwd.error = etPwd.resources.getString(R.string.password_must_contains_num_letter_char)
        }
    }

    init {
        etPwd.setOnClickListener { etPwd.error = null }
    }
}