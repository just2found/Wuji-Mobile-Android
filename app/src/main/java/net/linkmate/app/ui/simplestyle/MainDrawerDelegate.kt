package net.linkmate.app.ui.simplestyle

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.layout_nav_theme.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.ui.activity.ThemeActivity
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.UIUtils

/**
 * @author Raleigh.Luo
 * date：21/1/25 15
 * describe：
 */
class MainDrawerDelegate(private val activity: Activity, private val drawerLayout: DrawerLayout, private val navigationView: NavigationView, private val guidlineStart: View) {
    private var theme = ThemeActivity.SIMPLE_STYLE

    init {
        val layoutParams = navigationView.layoutParams
        layoutParams.width = Dp2PxUtils.getScreenWidth(MyApplication.getContext())*2 / 5
        navigationView.layoutParams = layoutParams
        navigationView.setBackgroundResource(R.color.black)

        val header = navigationView.inflateHeaderView(R.layout.layout_nav_theme)
        val headerLayoutParams = header.layoutParams
        val outSize = Point()
        activity.getWindowManager().getDefaultDisplay().getSize(outSize)
        //需用getRealSize 处理全屏问题
        headerLayoutParams.height = outSize.y
        header.layoutParams = headerLayoutParams


        with(header) {
            tvStatus.setPaddingRelative(0,UIUtils.getStatueBarHeight(activity),0,0)
            theme = MyApplication.getContext().getSharedPreferences(ThemeActivity.THEME_STYLE_TABLE, Context.MODE_PRIVATE).getString(ThemeActivity.THEME_STYLE, ThemeActivity.SIMPLE_STYLE)
            when (theme) {
                ThemeActivity.OLDER_STYLE -> {
                    tvFirstStyle.setStartDrawable(context.getDrawable(R.drawable.ic_radio_button_unchecked_black_24dp))
                    tvSecondStyle.setStartDrawable(context.getDrawable(R.drawable.check_red))
                    ivFirstStyle.setBackgroundResource(0)
                    ivSecondStyle.setBackgroundResource(R.drawable.bg_theme_choice_main)
                }
                else -> {
                    tvFirstStyle.setStartDrawable(context.getDrawable(R.drawable.check_red))
                    tvSecondStyle.setStartDrawable(context.getDrawable(R.drawable.ic_radio_button_unchecked_black_24dp))
                    ivSecondStyle.setBackgroundResource(0)
                    ivFirstStyle.setBackgroundResource(R.drawable.bg_theme_choice_main)
                }
            }

            ivFirstStyle.setOnClickListener {
                if (theme != ThemeActivity.SIMPLE_STYLE) {
                    theme = ThemeActivity.SIMPLE_STYLE
                    MyApplication.getContext().getSharedPreferences(ThemeActivity.THEME_STYLE_TABLE,Context.MODE_PRIVATE).edit().putString(ThemeActivity.THEME_STYLE, theme).apply()
                    ivSecondStyle.setBackgroundResource(0)
                    ivFirstStyle.setBackgroundResource(R.drawable.bg_theme_choice_main)
                    tvFirstStyle.setStartDrawable(context.getDrawable(R.drawable.check_red))
                    tvSecondStyle.setStartDrawable(context.getDrawable(R.drawable.ic_radio_button_unchecked_black_24dp))
                    ThemeActivity.enterMainInstance(activity)
                    activity.finish()
                }
            }
            ivSecondStyle.setOnClickListener {
                if (theme != ThemeActivity.OLDER_STYLE) {
                    theme = ThemeActivity.OLDER_STYLE
                    MyApplication.getContext().getSharedPreferences(ThemeActivity.THEME_STYLE_TABLE,Context.MODE_PRIVATE).edit().putString(ThemeActivity.THEME_STYLE, theme).apply()
                    ivFirstStyle.setBackgroundResource(0)
                    ivSecondStyle.setBackgroundResource(R.drawable.bg_theme_choice_main)
                    tvFirstStyle.setStartDrawable(context.getDrawable(R.drawable.ic_radio_button_unchecked_black_24dp))
                    tvSecondStyle.setStartDrawable(context.getDrawable(R.drawable.check_red))
                    ThemeActivity.enterMainInstance(activity)
                    activity.finish()
                }
            }
        }
    }


    fun addDrawerListener() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            /** 当侧滑菜单正在滑动时触发的方法
            第一个参数：正在滑动的侧滑菜单
            第二个参数：菜单滑动的宽度的百分比
             **/

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //获得侧滑菜单的宽度
                val drawerViewWidth = drawerView.measuredWidth
                //整体移动
                guidlineStart.setPaddingRelative((drawerViewWidth * slideOffset).toInt(), 0, 0, 0)
//                setDrawerLeftEdgeSize(this@MainActivity, mDrawerLayout, 1f)

            }

            override fun onDrawerClosed(drawerView: View) {
                guidlineStart.setPaddingRelative(0, 0, 0, 0)
            }

            override fun onDrawerOpened(drawerView: View) {
            }

        })
    }
}