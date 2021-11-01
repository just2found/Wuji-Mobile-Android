package net.linkmate.app.ui.simplestyle.dynamic.related

import android.os.Bundle

import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_dynamic_related.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar

/**与我相关
 * @author Raleigh.Luo
 * date：21/2/3 16
 * describe：
 */
class RelatedActivity : BaseActivity() {
    private lateinit var adapter: RelatedAdapter
    private val viewModel: RelatedViewModel by viewModels()
    private var isOperateClear = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_related)
        initObserver()
        initNoStatusBar()
        toolbar.inflateMenu(R.menu.related_header_menu_simplestyle)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear -> {//清空
                    if (viewModel.relateds.value != null && (viewModel.relateds.value?.size
                                    ?: 0) > 0) {
                        showLoading()
                        isOperateClear = true
                        viewModel.clearDB()
                    }
                }
            }
            true
        }
        adapter = RelatedAdapter(this, viewModel)
        recyclerView.adapter = adapter

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

        toolbar.setPadding(toolbar.paddingLeft, UIUtils.getStatueBarHeight(this),
                toolbar.paddingRight, toolbar.paddingBottom)
    }

    private fun initObserver() {
        viewModel.getRelatedListResult.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    flProgressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
                Status.ERROR -> {
                    recyclerView.visibility = View.VISIBLE
                    flProgressBar.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
                Status.LOADING -> {
                    recyclerView.visibility = View.GONE
                    flProgressBar.visibility = View.VISIBLE
                }
            }
        })
        viewModel.relateds.observe(this, Observer {
            if (isOperateClear) {//清空
                dismissLoading()
                isOperateClear = false
                adapter.notifyDataSetChanged()
            }

            if (it != null && it.size > 0) {//加载数据
                flProgressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }
        })

    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    override fun getTopView(): View? {
        return null
    }
}