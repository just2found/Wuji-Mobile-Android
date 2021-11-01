package net.linkmate.app.ui.simplestyle.dynamic

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.arch.core.util.Function
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.fragment_pls_login.view.*
import kotlinx.android.synthetic.main.item_dynamic_empty_layout.view.*
import kotlinx.android.synthetic.main.item_dynamic_simplestyle.view.*
import kotlinx.android.synthetic.main.layout_footer_view.view.*
import kotlinx.android.synthetic.main.layout_loading_view.view.*
import kotlinx.android.synthetic.main.layout_loading_view.view.vfootRoot
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.dynamic.delegate.ImageDisplayDelegate
import net.linkmate.app.ui.simplestyle.dynamic.delegate.TextShrinkDelegate
import net.linkmate.app.view.ForbidLinearLayoutManager
import net.linkmate.app.view.ViewHolder
import net.linkmate.app.ui.simplestyle.dynamic.CircleStatus.*


/** 动态列表适配器
 * @author Raleigh.Luo
 * date：20/11/21 11
 * describe：
 */
class DynamicAdapter(val context: FragmentActivity, val viewModel: DynamicViewModel,
                     val recyclerView: RecyclerView, val emptyOperateCallback: Function<Int, Void>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DEFAULT_TYPE = 0//正常数据
    private val NO_DATA_TYPE = -1//无数据
    private val NO_LOGIN_TYPE = -2//未登录
    private val LOADING_TYPE = -3 //正在加载数据

    /**
     * 获取屏幕信息
     */
    private fun getScreenHeight(context: Context): Int {
        // 获取屏幕信息
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            NO_LOGIN_TYPE -> {
                val layout = R.layout.fragment_pls_login
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                //高度为屏幕一半
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                with(view) {
                    pls_login_btn_login.setOnClickListener {
                        context.startActivity(android.content.Intent(context, net.linkmate.app.ui.activity.LoginActivity::class.java))
                    }
                }
                return ViewHolder(view)
            }
            LOADING_TYPE -> {
                val view =
                        LayoutInflater.from(context).inflate(R.layout.layout_loading_view, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                with(view) {
                    val footLayoutParams = vfootRoot.layoutParams as FrameLayout.LayoutParams
                    footLayoutParams.setMargins(0, context.resources.getDimensionPixelSize(R.dimen.common_icon64), 0, 0)
                    vfootRoot.layoutParams = footLayoutParams
                    Glide.with(context).asGif().load(R.drawable.loading).transition(DrawableTransitionOptions.withCrossFade()).into(ivLoadingImage)
                }
                return ViewHolder(view)
            }
            DEFAULT_TYPE -> {
                val getView = { layout: Int ->
                    val view =
                            LayoutInflater.from(context).inflate(layout, null, false)
                    view.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    view
                }
                return DynamicViewHolder(context, viewModel, getView(R.layout.item_dynamic_simplestyle), recyclerView)
            }
            else -> {
                val view =
                        LayoutInflater.from(context).inflate(R.layout.item_dynamic_empty_layout, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                view.setBackgroundResource(android.R.color.transparent)
                return ViewHolder(view)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        val status = viewModel.circleStatus.value
        if (viewModel.checkLoggedin() == false) {//未登录
            return NO_LOGIN_TYPE
        } else if (status == NONE || status == WITHOUT_NETWORK || (status == NOMARL && getDataCount() == 0 && (viewModel.refreshDynamicResult.value?.status == Status.LOADING
                        || viewModel.loginResult.value?.status == Status.LOADING))) {
            //为默认，或页面无数据&正在请求，显示Loading界面
            return LOADING_TYPE
        } else if (viewModel.isDisplayData()) {
            //请求失败,主EN离线，已有数据为主
            return if (getDataCount() == 0) NO_DATA_TYPE else DEFAULT_TYPE
        } else {
            return NO_DATA_TYPE
        }
    }


    fun updateItems() {
        /**
         * 先加载关系
         */
        viewModel.loadingManyRelated(Function {
            val count = itemCount
            if (itemCount == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeChanged(0, itemCount, arrayListOf(1))
            }
            null
        })
    }

    /**
     * 刷新最后一项,加载更多动画／
     */
    fun notifyLoadingAnim() {
        notifyItemChanged(itemCount - 1, viewModel.dynamicList.value)
    }

    private fun getDataCount(): Int {
        return viewModel.dynamicList.value?.size ?: 0
    }

    override fun getItemCount(): Int {
        val emptyCount = 1
        if (viewModel.isDisplayData()) {
            return if (getDataCount() == 0) emptyCount else getDataCount()
        } else {
            return emptyCount
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == DEFAULT_TYPE) {
            with(holder.itemView) {
                val item = viewModel.dynamicList.value?.get(position)
                item?.let {
                    clImagePanel.imageUrls = item.MediasPO
                    if (tvName.text.toString() != item.Username) tvName.setText(item.Username)
                    val mHolder = holder as DynamicViewHolder
                    mHolder.adapter.updateItems(position, item)
                    //TODO加载头像
//                        mHolder.portraitDisplayDelegate.loadImage(imageURL3)
//                    mHolder.portraitDisplayDelegate.setDefaultDisplayLargeImageListener(context)
                    if (ivPortrait.getTag() != R.drawable.icon_default_user_new) {
                        ivPortrait.setImageResource(R.drawable.icon_default_user_new)
                        ivPortrait.setTag(R.drawable.icon_default_user_new)
                    }
                    ivPortrait.setOnClickListener {//进入详情
                        viewModel.accessDynamicDetail(item.ID)
                    }
                    val key = item.autoIncreaseId + (item.ID ?: 0L) + (item.CreateAt ?: 0L)
                    mHolder.textShrinkDelegate.setText(key.hashCode(), item.Content)

                    setOnClickListener {//进入详情
                        viewModel.accessDynamicDetail(item.ID)
                    }

                    //评论和附件都为空时，不显示底部间距
                    val visibility = if (item.AttachmentsPO.size == 0 && item.CommentsPO.size == 0) View.GONE else View.VISIBLE
                    if (vBottomLineGap.visibility != visibility)
                        vBottomLineGap.visibility = visibility

                    if (position == (itemCount - 1) && viewModel.isLoadToEnd.value == true) {//最后一项加载到底了
                        iFooter.visibility = View.VISIBLE
                        tvLoadingText.setText(R.string.brvah_load_end)
                        mProgressBar.visibility = View.GONE
                    } else if (position == (itemCount - 1) && viewModel.isLoading.value == true) {//最后一项正在加载更多
                        iFooter.visibility = View.VISIBLE
                        tvLoadingText.setText(R.string.loading)
                        mProgressBar.visibility = View.VISIBLE
                    } else {//隐藏底部加载框
                        iFooter.visibility = View.GONE
                    }
                }
            }
        } else if (getItemViewType(position) == NO_DATA_TYPE) {
            updateEmpty(holder)
        }
    }


    /**
     * 局部刷新
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    private fun updateEmpty(holder: RecyclerView.ViewHolder) {
        with(holder.itemView) {
            viewModel.circleStatus.value?.let {
                if (it.titleRes != 0) {
                    //无数据，且请求出错
                    tvHint.visibility = View.VISIBLE
                    if (viewModel.isRequestFailed()) {
                        //请求失败,EN服务器连接异常,空页面才显示
//                        REQUEST_FAILED(7,R.string.en_server_cant_connected,R.string.try_again)
                        tvHint.setText(R.string.en_server_cant_connected)
                    } else {
                        tvHint.setText(it.titleRes)
                    }

                } else {
                    tvHint.visibility = View.GONE
                }
                if (it.operateTextRes != 0) {
                    //无主EN需圈子onwner且收费圈子才能设置
                    val isOwner = viewModel.currentNetwork.value?.isOwner ?: false
                    val isChargeCircle = viewModel.currentNetwork.value?.isCharge ?: false
                    if (it != WITHOUT_DEVICE_SERVER || (isOwner && isChargeCircle)) {//没有主EN服务器，owner添加EN
                        btnOperate.visibility = View.VISIBLE
                        if (viewModel.isRequestFailed()) {
                            //无数据，且请求出错
                            //请求失败,EN服务器连接异常,空页面才显示
                            btnOperate.setText(R.string.try_again)
                        } else {
                            btnOperate.setText(it.operateTextRes)
                        }

                    } else {
                        btnOperate.visibility = View.GONE
                    }
                } else {
                    btnOperate.visibility = View.GONE
                }
                val type = it.type
                btnOperate.setOnClickListener {
                    emptyOperateCallback.apply(type)
                }
                true
            } ?: let {
                tvHint.visibility = View.GONE
                btnOperate.visibility = View.GONE
                btnOperate.setOnClickListener(null)
            }
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        //释放图片
        if (holder is DynamicViewHolder) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }


    /**
     * 注意：保存文本状态集合的key一定要是唯一的，如果用position。
     * 如果使用position作为key，则删除、增加条目的时候会出现显示错乱
     */

    class DynamicViewHolder(val context: FragmentActivity, val viewModel: DynamicViewModel, view: View, val recyclerView: RecyclerView) : RecyclerView.ViewHolder(view) {
        val adapter: DynamicItemAapter
        val textShrinkDelegate: TextShrinkDelegate
        val portraitDisplayDelegate: ImageDisplayDelegate
        val mItemRecyclerView: RecyclerView

        init {
            adapter = DynamicItemAapter(context, viewModel)
            with(itemView) {
                textShrinkDelegate = TextShrinkDelegate.create(tvContent, tvExpandOrFold, recyclerView)
                //加入Viewmodel数组中，便于清除对象
                viewModel.textShrinkDelegatesManager.add(textShrinkDelegate)

                portraitDisplayDelegate = ImageDisplayDelegate.create(ivPortrait)
                mItemRecyclerView = itemRecyclerView
                mItemRecyclerView.isNestedScrollingEnabled = false
                mItemRecyclerView.layoutManager = ForbidLinearLayoutManager(context)
                itemRecyclerView.adapter = adapter
            }
        }

        /**
         * 释放图片
         */
        fun clear() {
            with(itemView) {
                clImagePanel.clear()
            }

        }
    }
}