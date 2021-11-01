package net.linkmate.app.ui.fragment.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.include_msg_bar.*
import kotlinx.android.synthetic.main.include_msg_layout.*
import kotlinx.android.synthetic.main.include_title_bar.*
import kotlinx.android.synthetic.main.item_popup_msg.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.ui.activity.message.SystemMessageActivity
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.ui.fragment.SystemMsgFragment
import net.linkmate.app.ui.nas.share.ShareActivity
import net.linkmate.app.ui.nas.torrent.TorrentActivity
import net.linkmate.app.ui.nas.transfer.TransferActivity
import net.linkmate.app.ui.viewmodel.SystemMessageViewModel
import net.linkmate.app.ui.viewmodel.TransferCountViewModel
import net.linkmate.app.util.MySPUtils
import net.linkmate.app.view.TipsBar
import net.sdvn.common.vo.SdvnMessageModel
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import timber.log.Timber

class MsgFragment() : BaseFragment(), View.OnClickListener {
    private val systemMessageViewModel by activityViewModels<SystemMessageViewModel>()
    private val shareViewModel2 by activityViewModels<ShareViewModel2>()
    private val transferCountViewModel by activityViewModels<TransferCountViewModel>()

    private var tipsBar: TipsBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        systemMessageViewModel.messageCountLiveData
                .observe(this, Observer { newCount: Int -> onMessagesListChanged(newCount) })
        systemMessageViewModel.messagesLiveData
                .observe(this, Observer { refreshMsgContent(it) })
        transferCountViewModel
                .transferCountLiveData
                .observe(this, Observer { newCount: Int -> onTransferCountChanged(newCount) })
        LoginManager.getInstance().loginedData.observeForever(Observer {
            if (it) {
                shareViewModel2.shareElementV2sInComplete
                        .observe(this, shareV2Observer)
                systemMessageViewModel.observerMessageInit()
            } else {
                shareViewModel2.shareElementV2sInComplete
                        .removeObserver(shareV2Observer)
                systemMessageViewModel.clearMsgModelLiveData()
            }

        })
    }

    private val shareV2Observer = Observer<MutableList<ShareElementV2>> { shareElementV2s ->
        val newCount = shareElementV2s?.size ?: 0
        Timber.d("shareCount :%s", newCount)
        onShareCountChanged(newCount)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_msg
    }

    override fun getTopView(): View? {
        return (itb_rl)
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        tipsBar = view.findViewById(R.id.tipsBar)
        itb_iv_right?.setImageResource(R.drawable.icon_setting_white)
        itb_tv_title?.setText(R.string.msg)
        val layoutParams = itb_tv_title.layoutParams as LinearLayout.LayoutParams
        layoutParams.marginStart = resources.getDimensionPixelSize(R.dimen.common_16)
        itb_tv_title.layoutParams = layoutParams
        itb_iv_right?.visibility = View.GONE
        initEvent()
        val tag = SystemMsgFragment::class.java.name
        val systemMsgFragment = childFragmentManager.findFragmentByTag(tag) ?: SystemMsgFragment()
        childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, systemMsgFragment, tag)
                .commitNowAllowingStateLoss()
    }

    override fun getHomeTipsBar(): TipsBar? {
        return tipsBar
    }

    private fun refreshMsgContent(list: List<SdvnMessageModel>?) {
        if (!list.isNullOrEmpty()) {
            iml_tv_msg_content?.text = list[0].message
        }
    }

    private fun initEvent() {
        home_msg_system_msg?.setOnClickListener(this)
        iml_rl_system_msg_expand?.setOnClickListener(this)
        iml_rl_file_share_expand?.setOnClickListener(this)
        home_msg_file_share?.setOnClickListener(this)
        iml_rl_upload_download_expand?.setOnClickListener(this)
        home_msg_rl_upload_download?.setOnClickListener(this)
        iml_rl_torrents_expand?.let {
            it.setOnClickListener(this)
            it.isVisible = true
        }
        home_msg_torrents?.let {
            it.setOnClickListener(this)
            it.isVisible = true
        }
    }

    override fun onClick(view: View) {
        if (Utils.isNotFastClick(view)) {
            if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
                when (view.id) {
                    R.id.iml_rl_system_msg_expand, R.id.home_msg_system_msg -> startActivity(Intent(context, SystemMessageActivity::class.java))
                    R.id.iml_rl_file_share_expand, R.id.home_msg_file_share -> startActivity(Intent(context, ShareActivity::class.java))
                    R.id.iml_rl_upload_download_expand, R.id.home_msg_rl_upload_download -> startActivity(Intent(context, TransferActivity::class.java))
                    R.id.iml_rl_torrents_expand, R.id.home_msg_torrents -> {
                        TorrentActivity.startActivityWithId(requireContext(), null,null, true)
                    }
                }
            } else {
                LoginManager.getInstance().showDialog(context)
            }
        }
    }

    private fun onMessagesListChanged(newCount: Int) {
        iml_tv_msg_count?.visibility = if (newCount > 0) View.VISIBLE else View.GONE
        iml_tv_msg_count?.text = String.format("%d", newCount)
        home_msg_tv_msg_count?.visibility = if (newCount > 0) View.VISIBLE else View.GONE
        home_msg_tv_msg_count?.text = String.format("%d", newCount)
        if (newCount <= 0) {
            iml_tv_msg_content?.setText(R.string.no_msg)
        }
    }

    private fun onTransferCountChanged(newCount: Int) {
        iml_tv_trans_count?.visibility = if (newCount > 0) View.VISIBLE else View.GONE
        iml_tv_trans_count?.text = String.format("%d", newCount)
        home_msg_tv_trans_count?.visibility = if (newCount > 0) View.VISIBLE else View.GONE
        home_msg_tv_trans_count?.text = String.format("%d", newCount)
    }

    private fun onShareCountChanged(newCount: Int) {
        iml_tv_msg_share_count?.visibility = if (newCount > 0) View.VISIBLE else View.GONE
        iml_tv_msg_share_count?.text = String.format("%d", newCount)
        home_msg_tv_share_count?.visibility = if (newCount > 0) View.VISIBLE else View.GONE
        home_msg_tv_share_count?.text = String.format("%d", newCount)
    }
}