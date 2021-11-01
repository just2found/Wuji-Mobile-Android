package net.linkmate.app.ui.simplestyle.dynamic.detial

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.activity_dynamic_detail_simplestyle.*
import kotlinx.android.synthetic.main.activity_dynamic_detail_simplestyle.toolbar
import kotlinx.android.synthetic.main.item_dynamic_detail_simplestyle.*
import kotlinx.android.synthetic.main.popwindow_comment.*
import libs.source.common.livedata.Status
import libs.source.common.utils.InputMethodUtils
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.CommentEvent
import net.linkmate.app.ui.simplestyle.dynamic.delegate.ImageDisplayDelegate
import net.linkmate.app.ui.simplestyle.dynamic.getScreenLocationY
import net.linkmate.app.util.SoftKeyBoardListener
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.ForbidLinearLayoutManager
import net.linkmate.app.view.TipsBar

/** 动态详情
 * @author Raleigh.Luo
 * date：20/11/25 13
 * describe：
 */
class DynamicDetailActivity : BaseActivity() {
    private lateinit var portraitDisplayDelegate: ImageDisplayDelegate
    private lateinit var adapter: DynamicDetailAdapter
    private val viewModel: DynamicDetailViewModel by viewModels()

    companion object {
        const val DYNAMIC_ID = "dynamic_id"

        //滑动到指定评论id,null表示不处理
        const val SCROLL_COMMENT = "scroll_comment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_detail_simplestyle)
        initNoStatusBar()
        initSoftKeyBoardListener()
        initView()
        initEvent()
        initObserver()
    }

    /**
     * 状态栏修改
     */
    private fun initNoStatusBar() {
        //修改状态栏颜色
        window.setStatusBarColor(getResources().getColor(R.color.dynamic_toolbar_color));
        // 修改状态栏字体：深色SYSTEM_UI_FLAG_LIGHT_STATUS_BAR/浅色SYSTEM_UI_FLAG_LAYOUT_STABLE
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.decorView.systemUiVisibility = option
    }

    private fun initView() {
        if (intent.hasExtra(SCROLL_COMMENT))
            mReplayCommentEvent = intent.getSerializableExtra(SCROLL_COMMENT) as CommentEvent?
        initToolbar()
        initRecyclerView()
        vBottomLine.visibility = View.INVISIBLE

        tvContent.setMaxLines(Integer.MAX_VALUE);//设置文本的最大行数，为整数的最大数值
        tvExpandOrFold.visibility = View.GONE
        portraitDisplayDelegate = ImageDisplayDelegate.create(ivPortrait)
        viewModel.init(intent.getStringExtra(AppConstants.SP_FIELD_NETWORK), intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID),
                intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_IP))
        viewModel.getDynamic(intent.getLongExtra(DYNAMIC_ID, -1))
    }

    //上级页面传值，回复指定评论
    private var mReplayCommentEvent: CommentEvent? = null

    //是否初始化过指定评论
    private var isInitReplayCommentEvent = false

    private fun refresh() {
        viewModel.getDynamic()?.let {
            if (isInitReplayCommentEvent == false) {
                isInitReplayCommentEvent = true
                mReplayCommentEvent?.dynamicAutoIncreaseId = it.autoIncreaseId
                viewModel.initReplayCommentEvent(mReplayCommentEvent)
            }

            //有附件时top 14dip
            val paddingTop = if (it.AttachmentsPO.size > 0) (resources.getDimensionPixelSize(R.dimen.common_12) + resources.getDimensionPixelSize(R.dimen.common_2)) else 0
            itemRecyclerView.setPaddingRelative(0, paddingTop, 0, 0)

            flProgressBar.visibility = View.GONE
            //TODO加载头像
//            portraitDisplayDelegate.loadImage(imageURL3)
//            portraitDisplayDelegate.setDefaultListener(this, imageURL3)
            ivPortrait.setImageResource(R.drawable.icon_default_user_new)
            tvName.setText(it.Username)

            tvContent.visibility = if (TextUtils.isEmpty(it.Content)) View.GONE else View.VISIBLE
            tvContent.setText(it.Content)
            clImagePanel.imageUrls = it.MediasPO
            adapter.notifyDataSetChanged()
            true
        }
    }

    private fun initEvent() {
        mNestedScrollView.setOnTouchListener(onTouchListener)
        //还原评论框
        itemRecyclerView.setOnTouchListener(onTouchListener)
        tvContent.setOnTouchListener(onTouchListener)
        ivPortrait.setOnTouchListener(onTouchListener)
        clImagePanel.setOnTouchListener(onTouchListener)
        btnSend.setOnClickListener {
            mCommentSizeBeforeComment = viewModel.getDynamic()?.CommentsPO?.size ?: 0
            //发布评论
            viewModel.startPublishComment(etComment.text.toString(), viewModel.getDynamic()?.ID, viewModel.getDynamic()?.autoIncreaseId)
            //恢复评论框
            etComment.setText("")
            etComment.setHint(R.string.comment)
            InputMethodUtils.hideKeyboard(this)
        }
        etComment.addTextChangedListener {
            btnSend.isEnabled = if (etComment.text.trim().length > 0) true else false
        }
    }

    //评论前，评论的数量评论后，需滑动到底部，且数据动态刷新后
    private var mCommentSizeBeforeComment = 0

    /**
     * 评论后，需滑动到底部，且数据动态刷新后
     */
    private fun scrollToBottomAfterComment() {
        //发布成功，刷新时，真实评论数量>评论前数量
        if (mCommentSizeBeforeComment > 0 && (viewModel.getDynamic()?.CommentsPO?.size
                        ?: 0) > mCommentSizeBeforeComment) {
            mCommentSizeBeforeComment = 0
            //延迟刷新，数据刷新时，会有延迟，所以这里延迟半秒
            mNestedScrollView.postDelayed({
                mNestedScrollView.scrollTo(0, vBottomLine.getScreenLocationY())
            }, 500)

        }
    }

    /**
     * 还原评论框
     */
    private val onTouchListener = View.OnTouchListener { view, motionEvent ->
        viewModel.recoveryCommentDialog()
        false
    }

    private fun initToolbar() {
        itemRecyclerView.layoutManager = ForbidLinearLayoutManager(this)
        toolbar.setNavigationOnClickListener {
            viewModel.recoveryCommentDialog()
            finish()
        }
//        toolbar.inflateMenu(R.menu.friend_circle_header_menu_simplestyle)
//        toolbar.menu.findItem(R.id.add).icon = resources.getDrawable(R.drawable.icon_dynamic_more)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {//发布
                    viewModel.recoveryCommentDialog()
                }
            }
            true
        }
    }

    private fun initObserver() {
        flProgressBar.visibility = View.GONE
        viewModel.dynamic.observe(this, Observer {
            if (it.size > 0) {
                refresh()
                //评论后数据更新，滑动到评论底部
                scrollToBottomAfterComment()
            } else {//动态被删除
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
        viewModel.dynamicResult.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    if (viewModel.dynamic.value == null) {
                        mProgressBar.visibility = View.VISIBLE
                        flProgressBar.visibility = View.VISIBLE
                    }
                }
                Status.SUCCESS -> {
                    if (it.data?.code == DynamicQueue.SUCCESS_CODE && (it.data?.data?.size
                                    ?: 0) == 0) {//动态已被删除
                        ToastUtils.showToast(R.string.the_dynamic_is_deleted)
                        tvTips.visibility = View.VISIBLE
                        mProgressBar.visibility = View.GONE
                    } else {
                        refresh()
                    }
                }
                Status.ERROR -> {//错误，空白背景＋提示
                    mProgressBar.visibility = View.GONE
                    ToastUtils.showToast(R.string.en_server_cant_connected)
                }
            }
        })


        viewModel.toastText.observe(this, Observer {
            ToastUtils.showToast(it)
        })
        viewModel.recoveryCommentDialog.observe(this, Observer {
            viewModel.clearCommentEvent()
            etComment.setText("")
            etComment.setHint(R.string.comment)
            InputMethodUtils.hideKeyboard(this, etComment)
        })
        viewModel.commentEvent.observe(this, Observer {
            it?.let {
                //弹出虚拟键盘
                showCommentInputKeyboard(it.hint)
            }
        })
        viewModel.defualtReplayCommentScreenY.observe(this, Observer {
            //从与我相关消息界面，带的指定评论
            val commentLocactionY: Int = it
            //相对输入框弹框位置 移动距离，y>0 向上滑
            val y = commentLocactionY - commentPopupRoot.getScreenLocationY()
            mNestedScrollView.scrollBy(0, y)
        })

        viewModel.deleteCommentResult.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    showLoading()

                }
                Status.ERROR -> {
                    dismissLoading()
                    ToastUtils.showToast(R.string.en_server_cant_connected)
                }
                Status.SUCCESS -> {
                    dismissLoading()
                    if (it.data?.code == DynamicQueue.DELETED_CODE) {//动态已被删除
                        ToastUtils.showToast(R.string.the_dynamic_is_deleted)
                    } else if (it.data?.code != DynamicQueue.SUCCESS_CODE) {
                        ToastUtils.showToast(R.string.en_server_cant_connected)
                    }
                }
            }
        })


        viewModel.hasDeletedDynamic.observe(this, Observer {
            setResult(Activity.RESULT_OK)
            finish()
        })
    }


    /**
     * 监听 输入框键盘弹起
     */
    private fun initSoftKeyBoardListener() {
        val kbLinst = SoftKeyBoardListener(this)
        kbLinst.setOnSoftKeyBoardChangeListener(object : SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
            override fun keyBoardShow(softkeyBoardHeight: Int) {
                scrollToRelativePosition()
            }

            override fun keyBoardHide(h: Int) {
            }
        })
    }

    private fun scrollToRelativePosition() {
        viewModel.commentEvent.value?.let {//回复状态：弹出键盘有延迟，必须在软键盘真正弹出后
            if (viewModel.isTouchCommentEventing.value == true) {
                //列表滑动到评论弹框对应项位置，且在评论输入框上方
                //项底部的 Y位置 在屏幕中的位置
                //评论触发位于项底部，回复位于评论项底部
                val commentLocactionY: Int = it.screenLocationY
                //相对输入框弹框位置 移动距离，y>0 向上滑
                val y = commentLocactionY - commentPopupRoot.getScreenLocationY()
                mNestedScrollView.scrollBy(0, y)
                //恢复，区分滑动事件，还原评论框
                viewModel.endTouchCommentEvent()
            }
        } ?: let {//评论状态：键盘弹起时，评论状态自动滑动到底部
            if (etComment.hint == null || etComment.hint.toString() == getString(R.string.comment)) {
                val y = vBottomLine.getScreenLocationY() - commentPopupRoot.getScreenLocationY()
                if (y > 0) {//滑动到底部，需上滑才调用
                    //评论框弹起，且为评论，滑动到底部,y>0 向上滑
                    mNestedScrollView.scrollBy(0, y)
                }
            }
        }
    }

    /**
     * 弹出输入框虚拟键盘
     */
    private fun showCommentInputKeyboard(hint: String) {
        etComment.setHint(hint)
        etComment.setFocusable(true)
        etComment.setFocusableInTouchMode(true)
        if (viewModel.isTouchCommentEventing.value == true) {//区分第一次进入带入了指定评论项的情况，该情况不弹出虚拟键盘
            etComment.requestFocus()
            InputMethodUtils.showKeyboard(this, etComment)
        }
    }

    private fun getMarginStart(): Int {
        return getItemMarginStart() + ivPortrait.layoutParams.width + tvName.marginStart
    }

    private fun getItemMarginStart(): Int {
        return (guidelineStart.layoutParams as ConstraintLayout.LayoutParams).guideBegin
    }


    private fun initRecyclerView() {
        adapter = DynamicDetailAdapter(this, viewModel, mReplayCommentEvent?.commentId, getMarginStart(), getItemMarginStart())
        itemRecyclerView.adapter = adapter
        val layoutParams = itemRecyclerView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        itemRecyclerView.layoutParams = layoutParams

    }

    override fun getTopView(): View? {
        return null
    }


    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }


    override fun onResume() {
        DynamicQueue.isDynamicDisplayed = true
        super.onResume()
    }

    override fun onPause() {
        DynamicQueue.isDynamicDisplayed = false
        super.onPause()
    }
}