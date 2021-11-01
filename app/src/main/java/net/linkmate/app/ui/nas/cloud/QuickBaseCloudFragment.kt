package net.linkmate.app.ui.nas.cloud


import android.os.Bundle
import android.view.animation.Animation
import android.widget.LinearLayout
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objecbox.DeviceSettings
import net.sdvn.nascommon.iface.DisplayMode
import net.sdvn.nascommon.iface.OnBackPressedListener
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.widget.FilePathPanel
import net.sdvn.nascommon.widget.SearchPanel
import java.util.*

/**
 * Created by yun
 */
abstract class QuickBaseCloudFragment : TipsBaseFragment(),
        SearchPanel.OnSearchActionListener, OnBackPressedListener {

    protected var mOrderLayout: LinearLayout? = null
    protected var mPathPanel: FilePathPanel? = null
    protected var mSlideInAnim: Animation? = null
    protected var mSlideOutAnim: Animation? = null

    protected var isListShown = true
    protected var mOrderType = FileOrderTypeV2.time_desc
    var mFileType = OneOSFileType.PRIVATE

    //    protected var mLoginSession: LoginSession? = null
    protected var mUserSettings: DeviceSettings? = null
    protected var mFileList = ArrayList<OneOSFile>()
    protected var mSelectedList = ArrayList<DataFile>()
    //    var curPath: String? = OneOSFileType.PRIVATE.serverTypeName
    protected var mLastClickPosition = 0
    protected var mLastClickItem2Top = 0
    protected var isSelectionLastPosition = false
    protected var mDevId: String? = null

    private var compositeDisposable: CompositeDisposable? = null

    protected fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable!!.add(disposable)
    }

    protected fun dispose() {
        if (compositeDisposable != null) compositeDisposable!!.dispose()
    }

    protected fun initLoginSession() {
        val arguments = arguments
        if (arguments != null) {
            val devId = arguments.getString(AppConstants.SP_FIELD_DEVICE_ID)
            this.mDevId = devId
        } else {
            Logger.LOGE(TAG, " loginsession is null")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLoginSession()
    }


    override fun onResume() {
        super.onResume()
//        initLoginSession()
    }

    override fun onDestroy() {
        dispose()
        super.onDestroy()
    }

    abstract fun setFileType(type: OneOSFileType, path: String?, mode: DisplayMode)

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    abstract override fun onBackPressed(): Boolean

    abstract fun autoPullToRefresh()

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
        if (isAvailable) {
            if (view != null) {
//                view?.postDelayed({ initLoginSession() },2000 )
            }
        }
    }

    companion object {
        private val TAG = QuickBaseCloudFragment::class.java.simpleName
    }

    abstract fun setMultiModel(isSetMultiModel: Boolean, position: Int?): Boolean
}