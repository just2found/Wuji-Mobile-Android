package net.linkmate.app.ui.simplestyle.device.download_offline

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.entity.MultiItemEntity
import kotlinx.android.synthetic.main.fragment_download_offline_add.*
import kotlinx.android.synthetic.main.fragment_download_offline_detail.*
import kotlinx.android.synthetic.main.fragment_download_offline_detail.ivBack
import kotlinx.android.synthetic.main.fragment_download_offline_detail.toolbar_layout
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.download_offline.adapter.OdTaskDetailAdapter
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDownLoadFile
import net.linkmate.app.ui.simplestyle.device.download_offline.data.TaskDetailFolder
import net.sdvn.nascommon.BaseFragment


class DownloadOfflineDetailFragment : BaseFragment() {

    private val viewModel by viewModels<DownloadOfflineModel>({ requireParentFragment() })
    val folderList = mutableListOf<TaskDetailFolder>()//用于存放所有文件夹
    var rootFolder = TaskDetailFolder("")//新建一个空的节点
    var nowFolder: TaskDetailFolder? = null  //当前展开的文件夹

    private val mOdTaskDetailAdapter by lazy {
        val it = OdTaskDetailAdapter(mutableListOf())
        it.setOnItemClickListener { baseQuickAdapter, view, postion ->
            val item = baseQuickAdapter.getItem(postion)
            if (item is TaskDetailFolder) {
                showFolder(collapseFolders(item))
            }
        }
        val emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_empty_directory, null)
        it.emptyView = emptyView
        it
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_download_offline_detail
    }
    override fun getTopView(): View? {
        return toolbar_layout
    }

    override fun initView(view: View) {
        //对数据进行梳理，自动构成文件夹
//        viewModel.mShowItem?.btsubfiles?.let { resourceList ->
//            resourceList.forEach {
//                addToParentFolder(it)
//            }
//        }
        //对文件根节点进行处理，将根节点加入folderList
        rootFolder = collapseFolders(rootFolder)
        if (null == findFolderByDir(rootFolder.dirName)) {
            folderList.add(rootFolder)
        }

        ivBack.setOnClickListener {
            if (nowFolder == null) {
                findNavController().navigateUp()
            } else {
                val parentFolder = collapseFolders1(nowFolder!!)
                if (parentFolder == null) {
                    findNavController().navigateUp()
                } else {
                    showFolder(parentFolder)
                }
            }
        }

        save_tv.setOnClickListener {
            val startList = mutableListOf<String>()
            val suspendList = mutableListOf<String>()
            mOdTaskDetailAdapter.folderList.forEach {
                if (it.status == OfflineDownLoadFile.START) {
                    startList.add("${it.dirname}/${it.filename}")
                } else if (it.status == OfflineDownLoadFile.SUSPEND) {
                    suspendList.add("${it.dirname}/${it.filename}")
                }
            }

            devId?.let { it1 ->
                viewModel.optTaskStatus(it1, viewModel.mShowItem!!.id, startList, DownloadOfflineModel.CMD_APPOINT_START).observe(this, Observer {

                })
                viewModel.optTaskStatus(it1, viewModel.mShowItem!!.id, suspendList, DownloadOfflineModel.CMD_APPOINT_SUSPEND).observe(this, Observer {

                })
            }
            findNavController().navigateUp()
        }
        R.id.recycle_view
        recycle_view.adapter = mOdTaskDetailAdapter;
        showFolder(rootFolder)
    }


    //这个是回退找父类的一个逻辑
    private fun collapseFolders1(taskDetailFolder: TaskDetailFolder): TaskDetailFolder? {
        taskDetailFolder.getParentDir()?.let {
            val parentFolder = findFolderByDir(it)
            if (parentFolder != null && parentFolder.fileList.size == 0 && parentFolder.folderList.size == 1) {
                return collapseFolders1(parentFolder)
            }
            return parentFolder
        }
        return null
    }


    //因为主要是为了查看文件，为了防止点开一个文件夹后只有一个文件夹所以这么处理
    private fun collapseFolders(taskDetailFolder: TaskDetailFolder): TaskDetailFolder {
        if (taskDetailFolder.fileList.size == 0 && taskDetailFolder.folderList.size == 1) {
            return collapseFolders(taskDetailFolder.folderList[0])
        }
        return taskDetailFolder
    }


    private fun showFolder(taskDetailFolder: TaskDetailFolder) {
        nowFolder = taskDetailFolder;
        val list = mutableListOf<MultiItemEntity>()
        list.addAll(taskDetailFolder.folderList)
        list.addAll(taskDetailFolder.fileList)
        mOdTaskDetailAdapter.setNewData(list)
    }

    private fun findFolderByDir(dirName: String): TaskDetailFolder? {
        folderList.forEach {
            if (it.dirName == dirName) {
                return it
            }
        }
        return null
    }


    private fun addToParentFolder(folder: TaskDetailFolder) {
        val parentDirName = folder.getParentDir()
        if (TextUtils.isEmpty(parentDirName)) {
            rootFolder.folderList.add(folder)
        } else {
            var parentFolder = findFolderByDir(parentDirName!!)
            if (parentFolder == null) {
                parentFolder = TaskDetailFolder(parentDirName!!)
                folderList.add(parentFolder)
                addToParentFolder(parentFolder)
            }
            parentFolder.folderList.add(folder)
        }
    }

    private fun addToParentFolder(file: OfflineDownLoadFile) {
        var parentFolder = findFolderByDir(file.dirname)
        if (parentFolder == null) {
            parentFolder = TaskDetailFolder(file.dirname)
            folderList.add(parentFolder)
            addToParentFolder(parentFolder)
        }
        parentFolder.fileList.add(file)
    }
}