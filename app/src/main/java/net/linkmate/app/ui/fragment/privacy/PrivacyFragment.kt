package net.linkmate.app.ui.fragment.privacy

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.include_layout_pos_and_neg2.button_neg
import kotlinx.android.synthetic.main.include_layout_pos_and_neg2.button_pos
import kotlinx.android.synthetic.main.privacy_fragment.include_tool_bar
import kotlinx.android.synthetic.main.privacy_fragment_new.*
import libs.source.common.livedata.Resource
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.fragment.BaseFragment
import net.sdvn.nascommon.iface.OnResultListener
import org.view.libwidget.MagicTextViewUtil


class PrivacyFragment : BaseFragment() {

    var resultListener: OnResultListener<Resource<*>>? = null
    fun setOnResultListener(resultListener: OnResultListener<Resource<*>>) {
        this.resultListener = resultListener
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
//        itb_iv_left.visibility = View.GONE
//        itb_tv_title.setText(R.string.privacy_policy)
//        checkbox.setOnCheckedChangeListener { buttonView, isChecked -> button_pos.isEnabled = isChecked }
//        button_pos.isEnabled = checkbox.isChecked
//        checkbox.setText(R.string.agree_privacy)
//        button_pos.setText(R.string.agree)
//        button_neg.setText(R.string.disagree)
        button_pos.setOnClickListener { resultListener?.onResult(Resource.success("agree")) }
        button_neg.setOnClickListener { resultListener?.onResult(Resource.error("user cancel", "disagree")) }

        val agreement = getString(R.string.sub_agreement)
        val privatePolicy = getString(R.string.privacy_policy)
        val color = resources.getColor(R.color.link_blue)
        val strTips = tips.text.split("\$P\$")
        MagicTextViewUtil.getInstance(tips)
                .append(strTips[0])
                .append(agreement, color, true) { showTermsAndConditions() }
                .append(strTips[1])
                .append(privatePolicy, color, true) { showPrivacyPolicy() }
                .append(strTips[2])
                .show()
    }

    private fun showPrivacyPolicy(): Unit? {
        val privacyPolicy = getString(R.string.privacy_policy)
        val privacyPolicyUrl = MyConstants.getPrivacyUrlByLanguage(activity)//getString(R.string.privacy_policy_url);
        activity?.let { WebViewActivity.open(it, privacyPolicy, privacyPolicyUrl) }
        return null
    }

    private fun showTermsAndConditions(): Unit? {
        val agreement = getString(R.string.sub_agreement)
        val agreementUrl = MyConstants.getAgreementUrlByLanguage(activity)//getString(R.string.subscriber_agreement_url);
        activity?.let { WebViewActivity.open(it, agreement, agreementUrl) }
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.privacy_fragment_new
    }

    override fun getTopView(): View {
        return include_tool_bar
    }
}
