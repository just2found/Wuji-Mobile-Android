package net.linkmate.app.ui.function_ui.choicefile

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_choice_file1.*
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileInitData
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileListFunction
import net.linkmate.app.ui.function_ui.view.PathSegmentLayout
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.widget.FilePathPanel
import net.sdvn.nascommon.widget.FileSelectPanel


//功能样式参考Layout
abstract class AbsChoiceFileFragment1 : NasFileListFunction() {

    //新增需要子类实现的
    abstract fun onChangeToPath()//用户选择修保存路径

    abstract fun getNasFileInitData1(): NasFileInitData1

    private val mNasFileInitData1 by lazy {
        getNasFileInitData1()
    }

    override fun getNasFileInitData(): NasFileInitData {
        return mNasFileInitData1
    }

    //设置目标路径的显示信息
    private fun setTargetPath(sharePathType: Int, path: String) {
        val headStr = when (sharePathType) {
            SharePathType.USER.type -> {
                getString(R.string.root_dir_name_private)
            }
            SharePathType.PUBLIC.type -> {
                getString(R.string.root_dir_name_public)
            }
            SharePathType.SAFE_BOX.type -> {
                getString(R.string.root_dir_name_safe_box)
            }
            SharePathType.GROUP.type -> {
                mNasFileInitData1.getGroupId()?.let {
                    GroupsKeeper.findGroup(mNasFileInitData1.getDeviceId(),
                        it
                    )?.name
                }
                    ?:""
            }
            else -> {
                ""
            }
        }
        target_path_tv.text = "$headStr:$path"
    }


    //给父类实现的
    override fun getLayoutResId(): Int {
        return R.layout.fragment_choice_file1
    }

    override fun getTopView(): View? {
        return title_bar
    }

    override fun getFilePathPanel(): FilePathPanel? {
        return title_psl
    }

    override fun getMiddleTile(): View? {
        return title_psl
    }

    override fun getAddFolderBtn(): View? {
        return null
    }

    override fun getRecyclerView(): RecyclerView {
        return recycle_view
    }

    override fun initView(view: View) {
        super.initView(view)
        mTipsBar = tipsBar
        title_bar.setBackListener { onDismiss() }
        title_bar.setTitleText(mNasFileInitData1.mFragmentTitle)
        initStatusBarPadding(layout_select_top_panel)
        mChoiceNasFileAdapter.mSelectLiveData.observe(this, Observer {
            if (it.isNotEmpty()) {
                setSelectVisibility(true)
            }
            updateSelect(it)
        })
        setTargetPath(mNasFileInitData1.mTargetSharePathType, mNasFileInitData1.mTargetSharePath)

        target_path_tv.setOnClickListener {
            onChangeToPath()
        }
        ok_tv.setOnClickListener {
            if (mChoiceNasFileAdapter.mSelectList.isNullOrEmpty()) {
                ToastUtils.showToast(R.string.tip_select_file)
            } else {
                onNext(mNowType, mChoiceNasFileAdapter.mSelectList)
            }
        }
        layout_select_top_panel.setOnSelectListener( object : FileSelectPanel.OnFileSelectListener {
            override fun onSelect(isSelectAll: Boolean) {
                mChoiceNasFileAdapter.selectAll(isSelectAll)
            }

            override fun onDismiss() {
                mChoiceNasFileAdapter.selectAll(false)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mIsSelectModel) {
                setSelectVisibility(false)
            } else if (!onPathBack()) {
                onDismiss()
            }
        }
    }


    private var mIsSelectModel = false
    private fun setSelectVisibility(boolean: Boolean) {
        mIsSelectModel = boolean
        if (layout_select_top_panel.visibility != View.VISIBLE && boolean) {
            layout_select_top_panel.visibility = View.VISIBLE
        } else if (layout_select_top_panel.visibility == View.VISIBLE && !boolean) {
            layout_select_top_panel.visibility = View.GONE
        }

    }


    private fun updateSelect(selectedList: List<OneOSFile>) {
        layout_select_top_panel.updateCount(mChoiceNasFileAdapter.data.size, selectedList.size)
    }
}