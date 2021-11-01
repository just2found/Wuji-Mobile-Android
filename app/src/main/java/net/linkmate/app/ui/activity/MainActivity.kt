package net.linkmate.app.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import libs.source.common.AppExecutors
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.manager.PrivilegeManager
import net.linkmate.app.manager.PrivilegeManager.PrivilegeObserver
import net.linkmate.app.ui.fragment.ads.AdsViewModel
import net.linkmate.app.ui.fragment.main.HomeFragment
import net.linkmate.app.ui.fragment.main.MeFragment
import net.linkmate.app.ui.fragment.main.MsgFragment
import net.linkmate.app.ui.fragment.main.StoreFragment
import net.linkmate.app.ui.simplestyle.MainDrawerDelegate
import net.linkmate.app.ui.simplestyle.MainViewModel
import net.linkmate.app.ui.simplestyle.dynamic.DynamicFragment
import net.linkmate.app.ui.viewmodel.*
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.VIPDialogUtil
import net.linkmate.app.view.TipsBar
import net.sdvn.common.internet.protocol.UpdateInfo
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.iface.EventListener
import net.sdvn.nascommon.model.eventbus.DevHDAddNewUsers
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class MainActivity : BaseActivity(), PrivilegeObserver {
    protected val mViewModel: MainViewModel by viewModels()
    private val systemMessageViewModel by viewModels<SystemMessageViewModel>()
    private val shareViewModel2 by viewModels<ShareViewModel2>()
    private val transferCountViewModel by viewModels<TransferCountViewModel>()
    private val adsViewModel by viewModels<AdsViewModel>()

    private val bubbles: MutableMap<Int, TextView> = hashMapOf()
    private lateinit var mHomeFragment: HomeFragment
    private lateinit var mStoreFragment: StoreFragment

    //    private AppFragment mAppFragment;
    private lateinit var mMsgFragment: MsgFragment
    private lateinit var mMeFragment: MeFragment
    private lateinit var mDynamicFragment: DynamicFragment
    private var messagesCount = 0
    private var transfersCount = 0
    private var shareCount = 0
    private var mCurrentFragment: Fragment? = null

    private var torrentClientServiceIBinder: IBinder? = null
    private var mMessenger: Messenger? = null
    private val torrentClientServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            torrentClientServiceIBinder = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMessenger = Messenger(service)
            Timber.d("startTorrentClient 1")

            mMessenger?.send(Message.obtain(null, 110))
            torrentClientServiceIBinder = service
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        this.getWindow().getDecorView().setBackground(null);
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
// 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        setContentView(R.layout.activity_main)
        //抽屉效果
        MainDrawerDelegate(this, mDrawerLayout, mNavigationView, vStart).addDrawerListener()
        initFragments()
        initNavigation()

        systemMessageViewModel
                .messageCountLiveData
                .observe(this, Observer { integer: Int ->
                    messagesCount = integer
                    setTabMsgCount(PAGER_POSITION_MSG, messagesCount + shareCount + transfersCount)
                })
        transferCountViewModel
                .transferCountLiveData
                .observe(this, Observer { integer: Int ->
                    transfersCount = integer
                    setTabMsgCount(PAGER_POSITION_MSG, messagesCount + shareCount + transfersCount)
                })
        val observer = Observer<MutableList<ShareElementV2>> {
            shareCount = it?.size ?: 0
            Timber.d("shareCount :${shareCount}")
            setTabMsgCount(PAGER_POSITION_MSG, messagesCount + shareCount + transfersCount)
        }

        //动态消息数量
        mViewModel.dynamicMessageCount.observe(this, Observer {
            val count = Math.max(0, it)
            setTabMsgCount(PAGER_POSITION_DYNAMIC, count)
        })
        val updateViewModel = ViewModelProvider(this).get(UpdateViewModel::class.java)
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
        LoginManager.getInstance().loginedData.observeForever(Observer {
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
        if (mCurrentFragment == null) {
            showFragment(mHomeFragment)
        }
        val curLocale = resources.configuration.locale
        Timber.d("language ${curLocale.country} ${curLocale.language} $curLocale")

        AppExecutors.instance.networkIO().execute(Runnable {
            autoLogin()
        })
        adsViewModel.shouldShowHome(this)
    }


    private fun initFragments() {
        var homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment::class.java.name) as HomeFragment?
        if (homeFragment == null)
            homeFragment = HomeFragment()
        mHomeFragment = homeFragment
        var storeFragment = supportFragmentManager.findFragmentByTag(StoreFragment::class.java.name) as StoreFragment?
        if (storeFragment == null)
            storeFragment = StoreFragment()
        mStoreFragment = storeFragment
//        mAppFragment = (AppFragment) getSupportFragmentManager().findFragmentByTag(AppFragment.class.getName());
//        if (mAppFragment == null)
//            mAppFragment = new AppFragment();
        var msgFragment = supportFragmentManager.findFragmentByTag(MsgFragment::class.java.name) as MsgFragment?
        if (msgFragment == null)
            msgFragment = MsgFragment()
        mMsgFragment = msgFragment
        var meFragment = supportFragmentManager.findFragmentByTag(MeFragment::class.java.name) as MeFragment?
        if (meFragment == null)
            meFragment = MeFragment()
        mMeFragment = meFragment

        var dynamicFragment = supportFragmentManager.findFragmentByTag(DynamicFragment::class.java.name) as DynamicFragment?
        if (dynamicFragment == null)
            dynamicFragment = DynamicFragment()
        mDynamicFragment = dynamicFragment

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

    override fun getTipsBar(): TipsBar? {
        return if (mHomeFragment != null) mHomeFragment.getHomeTipsBar() else null
    }

    private fun initNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
//        navView.isItemHorizontalTranslationEnabled = true
//        navView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
//        navView.itemIconSize = (resources.displayMetrics.density * 18).roundToInt()
        navView.itemIconTintList = null
        navView.setItemIconSizeRes(R.dimen.common_20)
        navView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showFragment(mHomeFragment)
                }
                R.id.navigation_store -> {
                    showFragment(mStoreFragment)
                }
                R.id.navigation_dynamic -> {
                    showFragment(mDynamicFragment)
                }
                R.id.navigation_msg -> {
                    showFragment(mMsgFragment)
                }
                R.id.navigation_me -> {
                    showFragment(mMeFragment)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (mCurrentFragment == fragment) {
            return
        }
        val beginTransaction = supportFragmentManager.beginTransaction()
        if (mCurrentFragment != null) {
            beginTransaction.hide(mCurrentFragment!!)
        }
        if (!fragment.isAdded) {
            beginTransaction.add(R.id.main_content_container, fragment, fragment::class.java.name)
        } else {
            beginTransaction.show(fragment)
        }
        mCurrentFragment = fragment
        beginTransaction.commitNowAllowingStateLoss()
    }

    private fun initMsgCount() {
        setTabMsgCount(PAGER_POSITION_HOME, 0)
        setTabMsgCount(PAGER_POSITION_STORE, 0)
        setTabMsgCount(PAGER_POSITION_ME, 0)
        setTabMsgCount(PAGER_POSITION_MSG, 0)
        setTabMsgCount(PAGER_POSITION_DYNAMIC, mViewModel.dynamicMessageCount.value ?: 0)
    }

    fun setTabMsgCount(index: Int, count: Int) {
        var textView = bubbles[index]
        if (count > 0) {
            if (textView == null) {
                //获取整个的NavigationView
                val menuView = nav_view.getChildAt(0)
                //调节图标位置
                val topPadding = Dp2PxUtils.dp2px(this, 3)
                val itemView = menuView.findViewById<ViewGroup>(index)
                //加载我们的角标View，新创建的一个布局
                val badge = LayoutInflater.from(this).inflate(R.layout.tab_msg_badge, itemView, false)
                //添加到Tab上
                itemView.addView(badge)

                itemView.setPadding(0, topPadding, 0, 0)
                textView = badge.findViewById<TextView>(R.id.tv_msg_count)
                bubbles[index] = textView
            }
            textView?.text = count.toString()
            textView?.isVisible = true
        } else {
            textView?.isVisible = false
        }
    }

    fun onLoginStatusChange(loggedin: Boolean) {
//        if (loggedin) {
//            VIPDialogUtil.showVIPNotify(this);
//        }

        if (!loggedin) {
            transfersCount = 0
            shareCount = 0
            messagesCount = 0
            initMsgCount()
        }
    }

    override fun onEstablished() {
//        if (mHomeFragment != null) mHomeFragment.initData()
//        if (mMeFragment != null) mMeFragment.initData()
        viewModel.hasLoggedin.value = true
    }

    override fun getViewModel(): BaseViewModel {
        return mViewModel
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

    companion object {
        //    public static final int PAGER_POSITION_STORE = 1;
//    public static final int PAGER_POSITION_APP = 2;
//    public static final int PAGER_POSITION_MSG = 3;
//    public static final int PAGER_POSITION_ME = 4;
        const val PAGER_POSITION_HOME = R.id.navigation_home
        const val PAGER_POSITION_STORE = R.id.navigation_store
        const val PAGER_POSITION_MSG = R.id.navigation_msg
        const val PAGER_POSITION_ME = R.id.navigation_me
        const val PAGER_POSITION_DYNAMIC = R.id.navigation_dynamic
        private val TAG = MainActivity::class.java.simpleName

    }
}