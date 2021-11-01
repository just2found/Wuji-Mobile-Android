package net.linkmate.app.ui.scan

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import cn.bingoogolapple.qrcode.core.QRCodeView
import cn.bingoogolapple.qrcode.zxing.ZXingView
import net.linkmate.app.R
import net.linkmate.app.ui.fragment.BaseFragment
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.utils.PermissionChecker
import timber.log.Timber

class CaptureFragment : BaseFragment(), QRCodeView.Delegate {
    private val scanViewModel by viewModels<ScanViewModel>({
        requireActivity()
    })
    var captureStatus: ICaptureStatus? = null
    private val mHandler: Handler = Handler()
    private var zxing_view: ZXingView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanViewModel.liveDataCaptureAction.observe(this, Observer {
            when (it) {
                CaptureActionStart -> {
                    openCapture()
                }
                CaptureActionStop -> {

                }
                CaptureActionReStart -> {
                    restartPreviewAfterDelay(1600)
                }
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_capture
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        zxing_view = view.findViewById<ZXingView>(R.id.zxing_view)
        zxing_view?.hiddenScanRect()
    }

    override fun onDestroy() {
        zxing_view?.onDestroy()
        Timber.tag("TestCameraPermission").d("frag onDestroy")
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onStart() {
        super.onStart()
        Timber.tag("TestCameraPermission").d("onStart")
        val camera = Manifest.permission.CAMERA
        PermissionChecker.checkPermission(requireContext(), Callback { strings: List<String?>? ->
            Timber.tag("TestCameraPermission").d("onGranted OpenCamera")
            openCapture()
        }, Callback { strings: List<String?>? ->
            Timber.tag("TestCameraPermission").d("onDenied finishActivity")
            requireActivity().finish()
        }, camera)
    }

    fun openCapture() {
        zxing_view?.setDelegate(this)
        zxing_view?.startCamera()
        zxing_view?.startSpotAndShowRect()
        Timber.tag("TestCameraPermission").d("frag openCapture")
    }

    override fun onStop() {
        zxing_view?.setDelegate(null)
        mHandler.removeCallbacksAndMessages(null)
        zxing_view?.stopSpotAndHiddenRect()
        zxing_view?.stopCamera()
        Timber.tag("TestCameraPermission").d("frag onStop")
        super.onStop()
    }

    override fun onScanQRCodeSuccess(result: String?) {
        vibrate()
        zxing_view?.stopSpotAndHiddenRect()
        scanViewModel.onSuccess(result)
    }

    private fun vibrate() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {

    }

    override fun onScanQRCodeOpenCameraError() {
        scanViewModel.onOpenCameraError()
        Timber.tag("TestCameraPermission").d("frag onOpenCameraError")
    }

    fun restartPreviewAfterDelay(delay: Long) {
        mHandler.postDelayed(Runnable { openCapture() }, delay)
    }

    interface ICaptureStatus {
        fun onSuccess(result: String?)
        fun onOpenCameraError()
    }
}