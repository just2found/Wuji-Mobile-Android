package net.linkmate.app.ui.activity.mine

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import kotlinx.android.synthetic.main.activity_advanced_settings.*
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Config
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.util.RomUtils
import org.view.libwidget.singleClick
import java.util.*

class AdvancedSettingsActivity : BaseActivity() {
    private var mCurrOption: Int = 0
    private lateinit var optionList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_settings)
        initTitle()
        initLeakproof()
        initDlt()
        optionList = Arrays.asList(*resources.getStringArray(R.array.secure_option))
        initAlgoLevel()
    }

    override fun getTopView(): View? {
        return itb_rl
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }
    private fun initDlt() {
        setting_switch_dlt.setVisibility(if (CMAPI.getInstance().isGlobalDltEnable) View.VISIBLE else View.GONE)
        setting_switch_dlt.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            CMAPI.getInstance().setGlobalDlt(isChecked)
        })
    }

    private fun initLeakproof() {
        setting_switch_leakproof.setChecked(CMAPI.getInstance().config.isNetBlock)
        setting_switch_leakproof.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> CMAPI.getInstance().saveConfigBoolean(Config.CONFIG_B_INET_AUTO_BLOCK, isChecked) })
        tv_settings_inet_access_settings.setVisibility(if (Build.VERSION.SDK_INT >= 24 && !RomUtils.isEMUI()) View.VISIBLE else View.GONE)
        tv_settings_inet_access_settings.setOnClickListener(View.OnClickListener {
            try {
                val vpnIntent = Intent()
                vpnIntent.action = "android.net.vpn.SETTINGS"
                startActivity(vpnIntent)
            } catch (ignore: Exception) {
            }
        })
    }

    private fun initTitle() {
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_tv_title.setText(R.string.advanced_settings)
        itb_iv_left.setVisibility(View.VISIBLE)
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.singleClick {  onBackPressed() }
    }

    private fun initAlgoLevel() {
        mCurrOption = CMAPI.getInstance().baseInfo.algoLevel - 1
        if (mCurrOption > optionList?.size - 1) mCurrOption = optionList.size - 1
        if (mCurrOption < 0) mCurrOption = 0
        setting_ail_safe_option.setTips(optionList.get(mCurrOption))
        setting_ail_safe_option.setOnClickListener { showOptionsPickerView() }
    }


    private fun showOptionsPickerView() {
        val pvOptions: OptionsPickerView<String> = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, option2, options3, v ->
            val str: String = optionList.get(options1)
            setting_ail_safe_option.setTips(str)
            val result =  CMAPI.getInstance().setOptionAlgoLevel(options1 + 1)
            if (result ==Constants.CE_SUCC) {
                mCurrOption = options1
            }
        })
                .setCancelText(getString(R.string.cancel))
                .setSubmitText(getString(R.string.confirm))
                .setSelectOptions(mCurrOption)
                //设置可见数量为3,避免被部分手机导航栏挡住
                .setItemVisibleCount(3)
                .build<String>()
        pvOptions.setPicker(optionList, null, null)
        pvOptions.show()
    }

    override fun onResume() {
        super.onResume()
        val baseinfo = CMAPI.getInstance().baseInfo
        if (CMAPI.getInstance().isGlobalDltEnable) {
            setting_switch_dlt.setVisibility(View.VISIBLE)
        } else {
            setting_switch_dlt.setVisibility(View.GONE)
        }
        if (baseinfo.vipFeature and Constants.VIP_FEATURE_CHANGE_ALGO != 0) {
            setting_ail_safe_option.setEnabled(true)
        } else {
            setting_ail_safe_option.setEnabled(false)
        }
        setting_switch_dlt.setChecked(CMAPI.getInstance().isGlobalDlt)
    }
}