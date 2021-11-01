package net.linkmate.app.ui.function_ui.choicefile

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_choice_nas.*
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileInitData
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileListFunction
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.widget.FilePathPanel


//功能样式参考Layout  注意这个是文件夹不能选中的，如果要选中文件夹请用AbsChoiceFileFragment3
abstract class AbsChoiceFileFragment2 : NasFileListFunction() {

    abstract fun onEnsureClick(sharePathType: Int, path: String): Boolean//点击确定的时候

    private val mNasFileInitData2 by lazy {
        getNasFileInitData2()
    }
    abstract fun getNasFileInitData2(): NasFileInitData2
    override fun getNasFileInitData(): NasFileInitData {
        return mNasFileInitData2
    }

    private var mConfirmTv: TextView? = null;
    override fun onRootSpaceChange(isNotEnable: Boolean) {
        super.onRootSpaceChange(isNotEnable)
        setConfirmTvGone(isNotEnable)
    }

    override fun getTopView(): View? {
        return title_bar
    }

    protected fun setConfirmTvGone(gone: Boolean) {
        if (gone) {
            mConfirmTv?.visibility = View.GONE
        } else {
            mConfirmTv?.visibility = View.VISIBLE
        }
    }


    override fun initView(view: View) {
        super.initView(view)
        title_bar.setBackListener { onDismiss() }
        title_bar.setTitleText(mNasFileInitData2.mFragmentTitle)
        if (mNasFileInitData2.mShowConfirmTv) {
            mConfirmTv = title_bar.addRightTextButton(getString(R.string.confirm)) { view ->
                if (mNowType < 0) {
                    ToastUtils.showToast(R.string.hint_select_file)
                    return@addRightTextButton
                }
                if (onEnsureClick(mNowType, mNowPath!!)) {//是否已当前的的Type,mNowPath拦截处理
                    return@addRightTextButton
                }
                if (mChoiceNasFileAdapter.mSelectList.isEmpty()) {
                    ToastUtils.showToast(R.string.hint_select_file)
                } else {
                    onNext(mNowType, mChoiceNasFileAdapter.mSelectList)
                }
            }
        }
        setConfirmTvGone(isRootPath)
        add_folder_btn.setOnClickListener { createFolder() }
    }

    private fun createFolder() {
        getNasFileListModel().mSessionLiveData.value?.let {
            val fileManage = OneOSFileManage(requireActivity(), null, it, null,
                    OneOSFileManage.OnManageCallback {
                        if (it) {
                            reload()
                        }
                    }
                 ,getNasFileInitData2().getGroupId()
            )
            fileManage.manage(FileManageAction.MKDIR, if (TextUtils.isEmpty(mNowPath)) "/" else mNowPath!!, mNowType)
        }
    }

    override fun getFilePathPanel(): FilePathPanel? {
        return title_psl
    }

    override fun getMiddleTile(): View? {
        return title_psl
    }

    override fun getAddFolderBtn(): View? {
        return add_folder_btn
    }

    override fun getRecyclerView(): RecyclerView {
        return recycle_view
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_choice_nas
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!onPathBack()) {
                onDismiss()
            }
        }
    }

}