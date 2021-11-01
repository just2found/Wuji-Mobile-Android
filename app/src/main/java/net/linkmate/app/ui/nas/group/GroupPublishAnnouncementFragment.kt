package net.linkmate.app.ui.nas.group

import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_group_publish_announcement.*
import libs.source.common.livedata.Status
import libs.source.common.utils.SPUtilsN
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.util.ToastUtils
import org.view.libwidget.singleClick
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [GroupPublishAnnouncementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupPublishAnnouncementFragment : NavTipsBackPressedFragment() {

    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val navArgs by navArgs<GroupOSSettingFragmentArgs>()
    private fun getGroupId(): Long {
        return navArgs.groupId
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        SPUtilsN.put("${navArgs.groupId}${navArgs.deviceid}${SPUtilsN.GROUP_ANNOUNCEMENT_TIME}",System.currentTimeMillis()/1000)
        etContent.addTextChangedListener {//显示剩余文字大小
            tvNumber.setText(String.format("%s/%s", etContent.text.length, 500))
            tvPublish.isEnabled = etContent.text.trim().isNotEmpty()
        }
        //处理输入框可滑动
        etContent.setOnTouchListener { view, motionEvent ->
            if (view.getId() === R.id.etContent) {
                view.getParent().requestDisallowInterceptTouchEvent(true)
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.getParent()
                        .requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
        etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Timber.d("onTextChanged : s=$s start=$start before=$before count=$count")
            }
        })
        tvPublish.singleClick {
            val content = etContent.text.toString().trim()
            publishAnnouncement(content)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_publish_announcement
    }

    override fun getTopView(): View? {
        return flToolbar
    }

    override fun titleColor(): Int {
        return  resources.getColor(R.color.dynamic_toolbar_color)
    }

    private fun publishAnnouncement(content: String) {
        viewModel.publishAnnouncement(navArgs.deviceid, getGroupId(), content)
            .observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.publish_success)
                    findNavController().popBackStack()
                } else if (it.status == Status.ERROR) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            })
    }
}