package net.linkmate.app.ui.simplestyle

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main_simplestyle.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.manager.PrivilegeManager
import net.linkmate.app.ui.activity.ThemeActivity
import net.linkmate.app.ui.fragment.ads.AdsViewModel
import net.linkmate.app.ui.fragment.main.MeFragment
import net.linkmate.app.ui.fragment.main.MsgFragment
import net.linkmate.app.ui.simplestyle.circle.CircleFragment
import net.linkmate.app.ui.simplestyle.dynamic.DynamicFragment
import net.linkmate.app.ui.simplestyle.home.HomeFragment
import net.linkmate.app.ui.viewmodel.*
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.VIPDialogUtil
import net.linkmate.app.view.TipsBar
import net.sdvn.common.internet.protocol.UpdateInfo
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.iface.EventListener
import net.sdvn.nascommon.model.eventbus.DevHDAddNewUsers
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class MainActivity : BaseActivity(), PrivilegeManager.PrivilegeObserver {
    //用于监听取消dialog事件
    protected val mViewModel: MainViewModel by viewModels()

    //是否正在滑动页面，与bottomNavigation事件互斥
    private var isViewPagerScrolling = false

    //是否正在点击底部按钮，与ViewPager事件互斥
    private var isbottomNavigationScrolling = false

    private val systemMessageViewModel by viewModels<SystemMessageViewModel>()
    private val shareViewModel2 by viewModels<ShareViewModel2>()
    private val transferCountViewModel by viewModels<TransferCountViewModel>()
    private val adsViewModel by viewModels<AdsViewModel>()
    private var messagesCount = 0
    private var transfersCount = 0
    private var shareCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeActivity.checkTheme(this)
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        setContentView(R.layout.activity_main_simplestyle)
//        val navHostFragment = Navigation.findNavController(this, R.id.navHostFragment)
//        bottomNavigation.setupWithNavController(navHostFragment)
        initView()
        initObserver()
        checkUpateApp()
        checkDeviceUpdate()
        autoLogin()
        adsViewModel.shouldShowHome(this)
    }

    //检查更新App
    private fun checkUpateApp() {
        val updateViewModel = ViewModelProviders.of(this).get(UpdateViewModel::class.java)
        UpdateViewModel().checkAppVersion(false)?.observe(this, Observer {
            if (it.status === Status.SUCCESS) {
                val updateResult: UpdateInfo? = it.data
                if (updateResult != null) {
                    if (updateResult.isSuccessful && updateResult.isEnabled) {
                        val hash = SPUtils.getValue(this, UpdateViewModel.KEY_LAST_CHECK_UPDATE_HASH, "")
                        if (updateResult.files[0].hash == hash) {
                            updateViewModel.beginUpdate(updateResult, this)
                        } else if (!TextUtils.isEmpty(updateResult.version)) {
                            updateViewModel.showUpdateDialog(updateResult, this@MainActivity)
                        }
                    }
                }
            }
        })
    }

    /**
     * 检查设备固件升级
     */
    private fun checkDeviceUpdate() {
        val mM8CheckUpdateViewModel = ViewModelProvider(this).get(M8CheckUpdateViewModel::class.java)
        mM8CheckUpdateViewModel.updateInfosLiveData.observe(this, Observer {
            if (!EmptyUtils.isEmpty(it)) {
                mM8CheckUpdateViewModel.showM8Upgrade(this, supportFragmentManager, ArrayList(it))
            }
        })
        mM8CheckUpdateViewModel.updateResultsLiveData.observe(this, Observer {
            if (!EmptyUtils.isEmpty(it)) {
                if (it.size == 1) {

                    mM8CheckUpdateViewModel.showDeviceItemUpgradeResult(this, it[0])
                } else {
                    mM8CheckUpdateViewModel.showM8UpgradeResults(this, supportFragmentManager, it)
                }
            }
        })

        val mDeviceViewModel = ViewModelProviders.of(this).get(DeviceViewModel::class.java)
        mDeviceViewModel.liveDevices.observe(this, Observer {
            mM8CheckUpdateViewModel.refreshDeviceUpdateInfo(it)
        })
    }


    private var selectedBottomNavigationMenuId = R.id.navigation_friend_circle
    private fun initView() {
        viewPager.adapter = ViewPagerFragmentStateAdapter(this)
        viewPager.offscreenPageLimit = 5
        //true:滑动，false：禁止滑动
        viewPager.setUserInputEnabled(false);
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!isbottomNavigationScrolling) {
                    isViewPagerScrolling = true
                    super.onPageSelected(position)
                    val id = bottomNavigationMenuId.get(position)
                    bottomNavigation.selectedItemId = id
                    isViewPagerScrolling = false
                }

            }

        })
        //设置图标大小
        bottomNavigation.setItemIconSizeRes(R.dimen.common_20)
        removeBottomNavigationLongClickToast()
        //去掉不显示图片默认颜色
        bottomNavigation.itemIconTintList = null
        bottomNavigation.setOnNavigationItemSelectedListener {
            if (!isViewPagerScrolling) {
                isbottomNavigationScrolling = true
                //it.order与menu 配置文件的orderInCategory属性对应  main_bottom_nav_menu_simplestyle
                viewPager.setCurrentItem(it.order, false)
                isbottomNavigationScrolling = false
            }
            //取消上一项
            cancelCheckBottomNavigationItem()
            //选中当前
            checkBottomNavigationItem(it)
            true
        }
        //抽屉效果
        MainDrawerDelegate(this, mDrawerLayout, mNavigationView, vStart).addDrawerListener()
    }

    override fun getViewModel(): BaseViewModel {
        return mViewModel
    }

    /**
     * 底部按钮id
     */
    private val bottomNavigationMenuId = arrayOf(R.id.navigation_home, R.id.navigation_circle, R.id.navigation_friend_circle, R.id.navigation_message, R.id.navigation_mine)

    /**
     * 底部正常图标
     */
    private val bottomNavigationUnCheckedResId = arrayOf(R.drawable.ic_home_simplestyle, R.drawable.ic_circle_simplestyle, R.drawable.ic_dynamic_simplestyle,
            R.drawable.ic_message_simplestyle, R.drawable.ic_mine_simplestyle)

    /**
     * 底部被选中图标
     */
    private val bottomNavigationCheckedResId = arrayOf(R.drawable.ic_home_simplestyle_focus, R.drawable.ic_circle_simplestyle_focus, R.drawable.ic_dynamic_simplestyle_focus,
            R.drawable.ic_message_simplestyle_focus, R.drawable.ic_mine_simplestyle_focus)

    /**
     * 根据id获取位置
     */
    private fun getPositionById(id: Int): Int {
        var position = 0
        for (index in 0 until bottomNavigationMenuId.size) {
            if (bottomNavigationMenuId[index] == id) {
                position = index
                break
            }
        }
        return position
    }

    /**
     * 移除长按底部Toast效果
     */
    private fun removeBottomNavigationLongClickToast() {
        with(bottomNavigation.getChildAt(0)) {
            //配置顶部与底部距离，减少文字与图标间的间距
            val paddingTop = resources.getDimensionPixelSize(R.dimen.common_2)
            for (i in 0 until bottomNavigationMenuId.size) {
                val view = findViewById<View>(bottomNavigationMenuId[i])
                view.setPaddingRelative(0,paddingTop,0,paddingTop)
                view.setOnLongClickListener { true }
            }
        }
    }

    /**
     * 取消上一个选中
     */
    private fun cancelCheckBottomNavigationItem() {
        var unSelectedIcon = 0
        for (i in 0 until bottomNavigationMenuId.size) {
            if (bottomNavigationMenuId.get(i) == selectedBottomNavigationMenuId) {
                unSelectedIcon = bottomNavigationUnCheckedResId[i]
                break
            }
        }
        bottomNavigation.menu.findItem(selectedBottomNavigationMenuId).icon = getDrawable(unSelectedIcon)
    }

    /**
     * 选中当前项
     */
    private fun checkBottomNavigationItem(menuItem: MenuItem) {
        selectedBottomNavigationMenuId = menuItem.itemId
        var selectedIcon = 0
        for (i in 0 until bottomNavigationMenuId.size) {
            if (bottomNavigationMenuId.get(i) == selectedBottomNavigationMenuId) {
                selectedIcon = bottomNavigationCheckedResId[i]
                break
            }
        }
        menuItem.icon = getDrawable(selectedIcon)
    }


    private fun initObserver() {
        systemMessageViewModel
                .messageCountLiveData
                .observe(this, Observer { integer: Int ->
                    messagesCount = integer
                    setTabMsgCount(getPositionById(R.id.navigation_message), messagesCount + shareCount + transfersCount)
                })
        transferCountViewModel
                .transferCountLiveData
                .observe(this, Observer { integer: Int ->
                    transfersCount = integer
                    setTabMsgCount(getPositionById(R.id.navigation_message), messagesCount + shareCount + transfersCount)
                })
        //动态消息数量
        mViewModel.dynamicMessageCount.observe(this, Observer {
            val count = Math.max(0, it)
            setTabMsgCount(getPositionById(R.id.navigation_friend_circle), count)
        })
        val observer = Observer<MutableList<ShareElementV2>> {
            shareCount = it?.size ?: 0
            Timber.d("shareCount :${shareCount}")
            setTabMsgCount(getPositionById(R.id.navigation_message), messagesCount + shareCount + transfersCount)
        }
        val updateViewModel = ViewModelProviders.of(this).get(UpdateViewModel::class.java)
        updateViewModel.checkAppVersion(false)?.observe(this, Observer {
            if (it.status === Status.SUCCESS) {
                val updateResult: UpdateInfo? = it.data
                if (updateResult != null) {
                    if (updateResult.isSuccessful && updateResult.isEnabled) {
                        val hash = SPUtils.getValue(this, UpdateViewModel.KEY_LAST_CHECK_UPDATE_HASH, "")
                        if (updateResult.files[0].hash == hash) {
                            updateViewModel.beginUpdate(updateResult, this)
                        } else if (!TextUtils.isEmpty(updateResult.version)) {
                            updateViewModel.showUpdateDialog(updateResult, this@MainActivity)
                        }
                    }
                }
            }
        })
        LoginManager.getInstance().loginedData.observe(this, Observer {
            onLoginStatusChange(it)
            if (it) {
                shareViewModel2.shareElementV2sInComplete
                        .observe(this, observer)
                systemMessageViewModel.observerMessageInit()
            } else {
                shareViewModel2.shareElementV2sInComplete
                        .removeObserver(observer)
                systemMessageViewModel.clearMsgModelLiveData()
            }
            sorsTorrentService(this, it)

        })
    }

    fun onLoginStatusChange(loggedin: Boolean) {
        if (!loggedin) {
            transfersCount = 0
            shareCount = 0
            messagesCount = 0
            initMsgCount()
        }
    }

    private fun initMsgCount() {
        for (id in bottomNavigationMenuId) {
            if (mViewModel.dynamicMessageCount.value != null && id == R.id.navigation_friend_circle) {
                setTabMsgCount(getPositionById(id), mViewModel.dynamicMessageCount.value ?: 0)
            } else {
                setTabMsgCount(getPositionById(id), 0)
            }
        }
    }

    private val bubbles: MutableMap<Int, TextView> = hashMapOf()
    private fun setTabMsgCount(index: Int, count: Int) {
        var textView = bubbles[index]
        if (count > 0) {
            if (textView == null) {
                //获取整个的NavigationView
                val menuView = bottomNavigation.getChildAt(0)
                //调节图标位置
                val id = bottomNavigationMenuId.get(index)

                val itemView = menuView.findViewById<ViewGroup>(id)
                //加载我们的角标View，新创建的一个布局
                val badge = LayoutInflater.from(this).inflate(R.layout.tab_msg_badge, itemView, false)
                //添加到Tab上
                itemView.addView(badge)
//                itemView.setPaddingRelative(0, 0, 0, 0)
                textView = badge.findViewById<TextView>(R.id.tv_msg_count)
                bubbles[index] = textView
            }
            textView?.text = count.toString()
            textView?.isVisible = true
        } else {
            textView?.isVisible = false
        }
    }


    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    override fun onStart() {
        super.onStart()
        initMsgCount()
        EventBus.getDefault().register(this)
        PrivilegeManager.getInstance().addPrivilegeObserver(this)
        showPrivilege()
        LoginManager.getInstance().loginedData.value?.let {
            onLoginStatusChange(it)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        PrivilegeManager.getInstance().deletePrivilegeObserver(this)
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN_ORDERED)
    fun observerDevHDAddNewUsers(devHDAddNewUsers: DevHDAddNewUsers) {
        UserUpdateViewModel(devHDAddNewUsers, this, object : EventListener<String> {
            override fun onStart(url: String) {
                showLoading()
            }

            override fun onSuccess(url: String, data: String?) {
                dismissLoading()
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                dismissLoading()
            }
        }).invoke()
    }

    override fun showPrivilege() {
        if (!PrivilegeManager.getInstance().isPrompted &&
                PrivilegeManager.getInstance().expiringBeans.size >= 1) {
            VIPDialogUtil.showVIPNotify(this)
            PrivilegeManager.getInstance().isPrompted = true
        }
    }


    internal class ViewPagerFragmentStateAdapter(@NonNull fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 5
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> CircleFragment()
                2 -> DynamicFragment()
                3 -> MsgFragment()
                else -> MeFragment()
            }
        }
    }

    override fun onEstablished() {
        super.onEstablished()
        viewModel.hasLoggedin.value = true
    }

    private var timesBackPressed: Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - timesBackPressed > 2000) {
            ToastUtils.showToast(getString(R.string.press_again_to_exit))
            timesBackPressed = System.currentTimeMillis()
        } else {
            val intentHome = Intent(Intent.ACTION_MAIN)
            intentHome.addCategory(Intent.CATEGORY_HOME)
            startActivity(intentHome)
        }
    }


}

