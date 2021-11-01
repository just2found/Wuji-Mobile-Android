package net.linkmate.app.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_theme.*
import net.linkmate.app.R

/** 切换主题
 * @author Raleigh.Luo
 * date：20/12/2 11
 * describe：
 */
class ThemeActivity : AppCompatActivity() {
    private var theme = SIMPLE_STYLE

    companion object {
        const val SIMPLE_STYLE = "simple_style"
        const val OLDER_STYLE = "older_style"
        const val THEME_STYLE = "theme_style"
        const val THEME_STYLE_TABLE = "theme_style_table"

        @JvmStatic
        fun checkTheme(context: Context) {
            val theme = context.getSharedPreferences(ThemeActivity.THEME_STYLE_TABLE,Context.MODE_PRIVATE).getString(ThemeActivity.THEME_STYLE, null)
            if (TextUtils.isEmpty(theme)) {
                context.startActivity(Intent(context, ThemeActivity::class.java))
            }
        }

        @JvmStatic
        fun enterMainInstance(context: Context){
            val theme = context.getSharedPreferences(ThemeActivity.THEME_STYLE_TABLE,Context.MODE_PRIVATE).getString(ThemeActivity.THEME_STYLE, null)
            when (theme) {
                OLDER_STYLE -> {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                else -> {
                    val intent = Intent(context, net.linkmate.app.ui.simplestyle.MainActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)
        initView()
    }


    private fun initView() {
        val layoutParams = ivFirstStyle.layoutParams
        layoutParams.width = (0.5f * resources.displayMetrics.widthPixels).toInt()
        layoutParams.height = (0.5f * resources.displayMetrics.widthPixels * 53 / 60).toInt()
        ivFirstStyle.layoutParams = layoutParams
        val layoutParams2 = ivSecondStyle.layoutParams
        layoutParams2.width = (0.5f * resources.displayMetrics.widthPixels).toInt()
        layoutParams2.height = (0.5f * resources.displayMetrics.widthPixels * 53 / 60).toInt()
        ivSecondStyle.layoutParams = layoutParams2

        theme = getSharedPreferences(THEME_STYLE_TABLE, Context.MODE_PRIVATE).getString(ThemeActivity.THEME_STYLE, SIMPLE_STYLE)
        when (theme) {
            OLDER_STYLE -> {
                llFirstStyle.setBackgroundResource(0)
                llSecondStyle.setBackgroundResource(R.drawable.bg_theme_choice)
            }
            else -> {
                llSecondStyle.setBackgroundResource(0)
                llFirstStyle.setBackgroundResource(R.drawable.bg_theme_choice)
            }
        }
        llFirstStyle.setOnClickListener {
            llSecondStyle.setBackgroundResource(0)
            llFirstStyle.setBackgroundResource(R.drawable.bg_theme_choice)
            theme = SIMPLE_STYLE
        }

        llSecondStyle.setOnClickListener {
            llFirstStyle.setBackgroundResource(0)
            llSecondStyle.setBackgroundResource(R.drawable.bg_theme_choice)
            theme = OLDER_STYLE
        }


        btnConfirm.setOnClickListener {
            getSharedPreferences(THEME_STYLE_TABLE,Context.MODE_PRIVATE).edit().putString(THEME_STYLE, theme).apply()
            enterMainInstance(this)
            finish()
        }

        ivBack.setOnClickListener {
            getSharedPreferences(THEME_STYLE_TABLE,Context.MODE_PRIVATE).edit().putString(THEME_STYLE, theme).apply()
            finish()
        }
    }

}