package net.linkmate.app.ui.nas.images

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_image_preview.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.fragment.BackPressedFragment
import net.linkmate.app.ui.nas.FullScreenToggleHelper
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.oneos.DataFile
import org.view.libwidget.singleClick
import timber.log.Timber


/**
 *
 * @Description: 图片预览基类
 * @Author: todo2088
 * @CreateDate: 2021/2/5 22:00
 */
abstract class BaseImagePreviewFragment : BackPressedFragment() {
    private var position: Int = -1
    private var fullScreenMode: Boolean = false
    protected val navArgs by navArgs<BaseImagePreviewFragmentArgs>()
    private var isLoadMore = false
    private lateinit var fullScreenToggleHelper: FullScreenToggleHelper
    private val imageAdapter: ImagePreviewAdapter by lazy {
        ImagePreviewAdapter(getPhotosViewModel(), requireContext()).apply {
            setOnItemClickListener { baseQuickAdapter, view, i ->
                fullScreenToggleHelper.toggleSystemUI()
            }
            setPreLoadNumber(AppConstants.PAGE_SIZE / 10)
            setOnLoadMoreListener({
                getPhotosViewModel().loadImageMore()
                isLoadMore = true
            }, view_page_2)
        }

    }

    //加载更多
    abstract fun getPhotosViewModel(): IPhotosViewModel<out DataFile>

    abstract fun getCurrentPosition(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: 2021/5/27  fragment 1.3.x   sharedElementEnterTransition 导致白屏
//        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.change_image_transform)
        getPhotosViewModel().liveDataPicFiles.observe(this, Observer {
            Timber.d("${it.status} ${it.code} ${it.message} ${it.data?.size}")
            when (it.status) {
                Status.SUCCESS -> {
                    if (isLoadMore) {
                        imageAdapter.updateData(it.data)
                        isLoadMore = false
                    }
                }
                Status.ERROR -> {
                    if (it.code != null) {

                    }
                }
            }
        })

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_image_preview
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        fullScreenToggleHelper = FullScreenToggleHelper(requireActivity(), getTopView(), Callback {
            if (it) {
                resetNoStatusBar()
            } else {
                initNoStatusBar()
            }
        })
        fullScreenToggleHelper.setupSystemUI()
        btn_back.singleClick {
            onBackPressed()
        }
        val lm = object : LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false) {
            override fun scrollToPosition(position: Int) {
                super.scrollToPosition(position)
                this@BaseImagePreviewFragment.position = position
                Timber.d(" scrollToPosition : $position ")
            }

            override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
                super.smoothScrollToPosition(recyclerView, state, position)
                this@BaseImagePreviewFragment.position = position
                Timber.d(" smoothScrollToPosition : $position ")
            }
        }
        val snapHelper = PagerSnapHelper()
        view_page_2.layoutManager = lm
        snapHelper.attachToRecyclerView(view_page_2)
        view_page_2.adapter = imageAdapter
        imageAdapter.updateData(getPhotosViewModel().getPagesPicModel().files)
        val position1 = getCurrentPosition()
        view_page_2.scrollToPosition(position1)
        view_page_2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val view1 = snapHelper.findSnapView(lm) ?: return
                val position = lm.getPosition(view1)
                Timber.d(" find position : $position ")
                if (lastPosition == position) {
                    return;
                }
                lastPosition = position;
                Timber.d(" lastPosition : $position ")
                updateIndex(position, getTotal())
            }
        })
        updateIndex(position1, getTotal())
    }


    private var lastPosition = 0

    open fun getTotal(): Int {
        return getPhotosViewModel().getPagesPicModel().total
    }


    fun updateIndex(position: Int, total: Int) {
        text_index.text = "${position + 1}/$total"
    }

    fun getDevId(): String {
        return navArgs.deviceid
    }

    override fun onBackPressed(): Boolean {
        return findNavController().popBackStack()
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }

    override fun onDestroyView() {
        fullScreenToggleHelper.resetSystemUI()
        super.onDestroyView()
    }


    override fun getTopView(): View? {
        return layout_title
    }

}