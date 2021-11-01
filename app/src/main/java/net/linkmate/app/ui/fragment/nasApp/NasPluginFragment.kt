package net.linkmate.app.ui.fragment.nasApp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_tool_app.*
import kotlinx.coroutines.delay
import net.linkmate.app.R
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.activity.nasApp.aria.AriaActivity
import net.linkmate.app.view.adapter.PluginAdapter
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSPluginInfo
import net.sdvn.nascommon.model.oneos.api.app.OneOSAppManageAPI
import net.sdvn.nascommon.model.oneos.api.app.OneOSAppManageAPI.OnManagePluginListener
import net.sdvn.nascommon.model.oneos.api.app.OneOSListAppAPI
import net.sdvn.nascommon.model.oneos.api.app.OneOSListAppAPI.OnListPluginListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.NasLanAccessViewModel
import net.sdvn.nascommon.widget.kyleduo.SwitchButton
import java.util.*

class NasPluginFragment : Fragment() {
    private val mPlugList: MutableList<OneOSPluginInfo> = ArrayList()
    private var mAdapter: PluginAdapter? = null
    private var mDevId: String? = null
    private val lanAccessViewModel: NasLanAccessViewModel by viewModels<NasLanAccessViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tool_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = arguments
        if (arguments != null) mDevId = arguments.getString(AppConstants.SP_FIELD_DEVICE_ID)
        initViews(view)
    }

    override fun onPause() {
        super.onPause()
        list_app?.hiddenRight()
    }

    override fun onResume() {
        super.onResume()
        pluginsFromServer
    }


    private fun initViews(view: View) {
        val mEmptyView = view.findViewById<View>(R.id.layout_empty)
        mEmptyView.isVisible = false
        list_app?.setEmptyView(mEmptyView)
        list_app?.setRightViewWidth(Utils.dipToPx(70f))
        mAdapter = PluginAdapter(requireContext(), list_app.getRightViewWidth(), mPlugList)
        list_app?.setAdapter(mAdapter)
        list_app?.setOnItemClickListener { arg0, arg1, arg2, arg3 ->
            lanAccessViewModel.getLoginSession(mDevId!!, object : GetSessionListener() {
                override fun onSuccess(url2: String, loginSession: LoginSession) {
                    lifecycleScope.launchWhenResumed {
                        var intent: Intent? = null
                        val info = mPlugList[arg2]
                        if (info.isTitle) {
                            return@launchWhenResumed
                        }
                        if ("aria2".equals(info.pack, ignoreCase = true) && info.isOn) {
                            intent = Intent(requireContext(), AriaActivity::class.java)
                            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, mDevId)
                        }
                        if ("transmission".equals(info.pack, ignoreCase = true) && info.isOn) {
                            val url = OneOSAPIs.PREFIX_HTTP + loginSession.ip
                            intent = Intent(requireContext(), WebViewActivity::class.java)
                            var region = "en"
                            if (UiUtils.isHans()) {
                                region = "zh-CN"
                            } else if (UiUtils.isHant()) {
                                region = "zh-TW"
                            }
                            intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_URL, url + info.url + "?lang=" + region)
                            intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_TITLE, info.name)
                            intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT, false)
                        }
                        intent?.let { startActivity(it) }
                    }
                }
            })
        }
        mAdapter!!.setOnClickListener { view1, info ->
            when (view1.id) {
                R.id.app_uninstall -> lanAccessViewModel.getLoginSession(mDevId!!, object : GetSessionListener() {
                    override fun onSuccess(url: String, loginSession: LoginSession) {
                        if (!loginSession.isAdmin) {
                            ToastHelper.showToast(R.string.please_login_onespace_with_admin)
                        } else {
                            showOperatePluginDialog(info, true)
                        }
                    }
                })
                R.id.btn_state -> {
                    val mBtn = view1 as SwitchButton
                    // 屏蔽非主动点击事件
                    if (info.isOn != mBtn.isChecked) {
                        lanAccessViewModel.getLoginSession(mDevId!!, object : GetSessionListener() {
                            override fun onSuccess(url: String, loginSession: LoginSession) {
                                if (!loginSession.isAdmin) {
                                    ToastHelper.showToast(R.string.please_login_onespace_with_admin)
                                    refreshAdapter()
                                } else {
                                    showOperatePluginDialog(info, false)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    private fun showOperatePluginDialog(info: OneOSPluginInfo, isUninstall: Boolean) {
        lifecycleScope.launchWhenResumed {
            var title: String? = null
            title = if (isUninstall) {
                resources.getString(R.string.confirm_uninstall_plugin)
            } else {
                if (info.isOn) {
                    resources.getString(R.string.confirm_close_plugin)
                } else {
                    resources.getString(R.string.confirm_open_plugin)
                }
            }
            title += " " + info.name + " ?"
            val resources = resources
            DialogUtils.showConfirmDialog(activity, resources.getString(R.string.tips), title,
                    resources.getString(R.string.confirm), resources.getString(R.string.cancel)
            ) { dialog, isPositiveBtn ->
                if (isPositiveBtn) {
                    doOperatePluginToServer(info, isUninstall)
                } else {
                    list_app?.hiddenRight()
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private val pluginsStatusFromServer: Unit
        get() {
            lanAccessViewModel.getLoginSession(mDevId!!, object : GetSessionListener(false) {
                override fun onSuccess(url: String, loginSession: LoginSession) {
                    for (info in mPlugList) {
                        if (info.isTitle) {
                            continue
                        }
                        val manageAPI = OneOSAppManageAPI(loginSession)
                        manageAPI.setOnManagePluginListener(object : OnManagePluginListener {
                            override fun onStart(url: String) {}
                            override fun onSuccess(url: String, pack: String, cmd: String, ret: Boolean) {
                                for (plug in mPlugList) {
                                    if (plug.isTitle) {
                                        continue
                                    }
                                    if (plug.pack == pack) {
                                        plug.stat = if (ret) OneOSPluginInfo.State.ON else OneOSPluginInfo.State.OFF
                                        break
                                    }
                                }
                                refreshAdapter()
                            }

                            override fun onFailure(url: String, pack: String, errorNo: Int, errorMsg: String) {
                                for (plug in mPlugList) {
                                    if (plug.isTitle) {
                                        continue
                                    }
                                    if (plug.pack == pack) {
                                        plug.stat = OneOSPluginInfo.State.UNKNOWN
                                        break
                                    }
                                }
                                refreshAdapter()
                            }
                        })
                        manageAPI.state(info.pack)
                    }
                }
            })
        }
    private val pluginsFromServer: Unit
        get() {
            lanAccessViewModel.getLoginSession(mDevId!!, object : GetSessionListener(false) {
                override fun onSuccess(url: String, loginSession: LoginSession) {
                    val listAppAPI = OneOSListAppAPI(loginSession)
                    listAppAPI.setOnListPluginListener(object : OnListPluginListener {
                        override fun onStart(url: String) {
                        }

                        override fun onSuccess(url: String, plugins: ArrayList<OneOSPluginInfo>) {
                            mPlugList.clear()
                            lifecycleScope.launchWhenResumed {
                                mPlugList.add(OneOSPluginInfo(true,
                                        resources.getString(R.string.file_service)))
                                addPlugin(mPlugList, "Samba", plugins)
                                addPlugin(mPlugList, "NFS", plugins)
                                addPlugin(mPlugList, "AFP", plugins)
                                addPlugin(mPlugList, "FTP", plugins)
                                addPlugin(mPlugList, "Rsync", plugins)
                                mPlugList.add(OneOSPluginInfo(true,
                                        resources.getString(R.string.download_tool)))
                                addPlugin(mPlugList, "Transmission", plugins)
                                addPlugin(mPlugList, "Aria2c", plugins)
                                mPlugList.add(OneOSPluginInfo(true,
                                        resources.getString(R.string.multimedia_service)))
                                addPlugin(mPlugList, "DLNA", plugins)
                                list_app?.hiddenRight()
                                refreshAdapter()
                                pluginsStatusFromServer
                            }
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        }
                    })
                    listAppAPI.list()
                }
            })
        }

    private fun refreshAdapter() {
        if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
        layout_empty.isVisible = mPlugList.isNullOrEmpty()
    }

    private fun addPlugin(mPlugList: MutableList<OneOSPluginInfo>, name: String, plugins: ArrayList<OneOSPluginInfo>) {
        for (plugin in plugins) {
            if (name == plugin.name) {
                mPlugList.add(plugin)
            }
        }
    }

    private var loading = 0
    private fun doOperatePluginToServer(info: OneOSPluginInfo, isUninstall: Boolean) {
        lanAccessViewModel.getLoginSession(mDevId!!, object : GetSessionListener() {
            override fun onSuccess(url: String, loginSession: LoginSession) {
                val manageAPI = OneOSAppManageAPI(loginSession)
                manageAPI.setOnManagePluginListener(object : OnManagePluginListener {
                    override fun onStart(url: String) {
//                        activity.showLoading(loading);
                    }

                    override fun onSuccess(url: String, pack: String, cmd: String, ret: Boolean) {
                        refreshListDelayed()
                    }

                    override fun onFailure(url: String, pack: String, errorNo: Int, errorMsg: String) {
                        refreshListDelayed()
                    }
                })
                if (isUninstall) {
                    loading = R.string.uninstalling_plugin
                    manageAPI.delete(info.pack)
                } else if (info.isOn) {
                    loading = R.string.closing_plugin
                    manageAPI.off(info.pack)
                } else {
                    loading = R.string.opening_plugin
                    manageAPI.on(info.pack)
                }
            }
        })
    }

    private fun refreshListDelayed() {
        lifecycleScope.launchWhenResumed {
            delay(2000)
            if (isResumed) pluginsFromServer
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(devId: String?): NasPluginFragment {
            val fragment = NasPluginFragment()
            val args = Bundle()
            args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            fragment.arguments = args
            return fragment
        }
    }
}