package net.linkmate.app.ui.nas.share

import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.SwipeItemLayout
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import net.linkmate.app.R
import net.linkmate.app.ui.nas.iface.RVItemType.*
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objecbox.SFDownload
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.fileserver.FileShareBaseResult
import net.sdvn.nascommon.fileserver.constants.EntityType
import net.sdvn.nascommon.fileserver.constants.FileServerErrorCode
import net.sdvn.nascommon.fileserver.data.DataShareDir
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import java.io.File
import java.util.*

class ShareMultiItemRvAdapter(data: List<MultiItemEntity>?, private val mActivity: FragmentActivity) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    private val mShareViewModel2: ShareViewModel2 = ViewModelProviders.of(mActivity).get(ShareViewModel2::class.java)

    init {
        addItemType(TYPE_LEVEL_0, R.layout.item_line_string)
        addItemType(TYPE_LEVEL_1, R.layout.item_share_v2)
        addItemType(TYPE_LEVEL_2, R.layout.item_share_download)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        val context = helper.itemView.context
        when (helper.itemViewType) {
            TYPE_LEVEL_0 -> {
                val header = item as HeaderMultiEntity
                helper.setText(R.id.header, header.header)
            }
            TYPE_LEVEL_1 -> convertShareElement(helper, item as ShareV2MultiEntity)
            TYPE_LEVEL_2 -> {
                val sfDownload = (item as SFDownloadEntity).sfDownload

                val toPath = sfDownload.toPath
                var tvPathText: SpannableString? = null
                if (!TextUtils.isEmpty(toPath)) {
                    tvPathText = getTvPathText(context,
                            toPath, sfDownload.toDevId)
                }
                helper.setText(R.id.txt_target_device, tvPathText ?: "")
                helper.setText(R.id.txt_target_to_path, if (sfDownload.timestamp > 0) {
                    String.format("%s : %s",
                            context.getString(R.string.create_time),
                            AppConstants.sdf.format(Date(sfDownload.timestamp * 1000)))
                } else {
                    ""
                })
                helper.itemView.setOnClickListener { showDes(helper, sfDownload.shareElementV2.target, sfDownload) }
            }
        }
    }

    private fun getTvPathText(context: Context, pathName: String,
                              showDevId: String): SpannableString {
        val sb = StringBuilder()
        val pathWithTypeName = OneOSFileType.getPathWithTypeName(pathName)
        val deviceModel = SessionManager.getInstance()
                .getDeviceModel(showDevId)
        var devMarkName: String? = null
        if (deviceModel != null) {
            devMarkName = deviceModel.devName
        }
        if (!TextUtils.isEmpty(devMarkName)) {
            sb.append(devMarkName).append(":").append(pathWithTypeName)
        } else {
            sb.append(pathWithTypeName)
        }
        return Utils.setKeyWordColor(context, R.color.primary, sb.toString(), devMarkName)
    }

    private fun convertShareElement(helper: BaseViewHolder, item: ShareV2MultiEntity) {
        val mShareElementV2 = item.shareElementV2

        val timestamp = mShareElementV2.remainPeriod
        var timestampStr = ""
        if (timestamp > 0) {
            val date = Date(timestamp * 1000)
            timestampStr = String.format("%s", AppConstants.sdf.format(date))
            if (date.time < Date().time && mShareElementV2.state != TransferState.CANCELED) {
                mShareElementV2.state = TransferState.CANCELED
                mShareViewModel2.putToDB(mShareElementV2)
            }
        }
        helper.setText(R.id.tv_share_date, timestampStr)

        helper.setGone(R.id.layout_pre_show, false)
        var owner = ""
        val paths = mShareElementV2.path
        if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
            if (paths.isNullOrEmpty() && timestamp <= 0) {
                helper.setText(R.id.tv_pre_show, R.string.tips_receive_pre_show)
                helper.setGone(R.id.layout_pre_show, true)
                helper.setGone(R.id.tv_pre_show, true)
                helper.setGone(R.id.progress_bar, false)
                helper.setText(R.id.tv_file_name, "")
            } else {
                helper.setText(R.id.tv_file_name, getShowStr(paths))
            }
            if (!(mShareElementV2.fromOwner).isNullOrEmpty())
                owner = String.format("%s", mShareElementV2.fromOwner)
            val detail = if (mShareElementV2.type == EntityType.SHARE_FILE_V2_COPY) {
                val s1 = SessionManager.getInstance().getDeviceModel(mShareElementV2.srcDevId)?.devName
                        ?: ""
                val toDevId = if (mShareElementV2.toDevId.isNullOrEmpty()) {
                    mShareElementV2.sfDownloads.takeUnless { it.isEmpty() }?.get(0)?.toDevId ?: ""
                } else {
                    mShareElementV2.toDevId
                }
                val s2 = SessionManager.getInstance().getDeviceModel(toDevId)?.devName
                        ?: ""
                val resources = helper.itemView.context.resources
                String.format("%s: %s %s: %s", resources.getString(R.string.from), s1,
                        resources.getString(R.string.to), s2)

            } else {
                owner
            }
            helper.setText(R.id.tv_user_name, detail)
            helper.setImageResource(R.id.iv_icon_left, getResIdByState(mShareElementV2.type, mShareElementV2.state))
        } else {
            helper.setText(R.id.tv_user_name, R.string.type_qrcode)
            helper.setText(R.id.tv_file_name, getShowStr(paths))
            helper.setImageResource(R.id.iv_icon_right, getResIdByState(mShareElementV2.type, mShareElementV2.state))
        }
        helper.setGone(R.id.iv_icon_left, mShareElementV2.type == EntityType.SHARE_FILE_V2_RECEIVE)
        helper.setGone(R.id.iv_icon_right, mShareElementV2.type == EntityType.SHARE_FILE_V2_SEND)

        if (helper.itemView is SwipeItemLayout) {
            helper.getView<View>(R.id.txt_delete).setOnClickListener(View.OnClickListener { v ->
                if (Utils.isFastClick(v)) return@OnClickListener
                showRemoveDialog(mShareElementV2, helper)
            })
        }
        helper.getView<View>(R.id.layout_view).setOnClickListener(View.OnClickListener { v ->
            if (Utils.isFastClick(v)) return@OnClickListener
            if (!NetworkStateManager.instance.checkNetworkWithCheckServer(true)){
                return@OnClickListener
            }
            if (!(paths.isNullOrEmpty() && timestamp <= 0)
                    && item.hasSubItem() && item.subItems.size > 1) {
                val pos = helper.adapterPosition
                if (item.isExpanded) {
                    collapse(pos)
                } else {
                    expand(pos)
                }
            } else
                onItemClick(helper, mShareElementV2)
        })
        helper.getView<View>(R.id.layout_view).setOnLongClickListener {
            if (!NetworkStateManager.instance.checkNetworkWithCheckServer(true)){
                return@setOnLongClickListener false
            }
            showRemoveDialog(mShareElementV2, helper)
            return@setOnLongClickListener true
        }

    }

    private fun showRemoveDialog(mShareElementV2: ShareElementV2, helper: BaseViewHolder) {
        mShareViewModel2.showRemoveDialog(mActivity, mShareElementV2) { (helper.itemView as SwipeItemLayout).close() }
    }

    private fun getShowStr(path: List<String>): String {
        val empty = mActivity.getString(R.string.empty_directory)
        if (path.isEmpty()) {
            return empty
        }
        if (path.size == 1) {
            var s = path[0]
            if (TextUtils.isEmpty(s)) return empty
            if (s.endsWith(File.separator)) {
                if (s.length == 1)
                    return s
                else
                    s = s.substring(0, s.length - 1)
            }
            val indexOf = s.lastIndexOf(File.separator)
            return if (indexOf >= 0) s.substring(indexOf + 1) else s
        } else {
            val sb = StringBuilder()
            for (p1 in path) {
                if (TextUtils.isEmpty(p1))
                    continue
                var p = p1
                if (File.separator != p) {
                    if (p.endsWith(File.separator)) {
                        p = p.substring(0, p.length - 1)
                    }
                    val indexOf: Int = p.lastIndexOf(File.separator)
                    if ((indexOf) >= 0) {
                        p = p.substring(indexOf + 1)
                    }
                }
                if (sb.isNotEmpty())
                    sb.append("  ")
                sb.append(p)
                if (sb.length > 150)
                    break
            }
            return sb.toString()
        }
    }


    private fun getResIdByState(state: Int, transferState: TransferState): Int {
        when (state) {
            EntityType.SHARE_FILE_V2_SEND -> {
                return if (TransferState.CANCELED == transferState) R.drawable.icon_share_end else R.drawable.icon_share_initiate
            }
            else -> return when (transferState) {
                TransferState.NONE -> if (state == EntityType.SHARE_FILE_V2_COPY) {
                    R.drawable.icon_share_copy
                } else {
                    R.drawable.icon_share_receive
                }
                TransferState.CANCELED -> R.drawable.icon_share_end
                TransferState.FAILED -> R.drawable.icon_download_failure
                TransferState.COMPLETE -> R.drawable.icon_download_end
                else -> R.drawable.icon_download_ing
            }
        }
    }

    fun onItemClick(helper: BaseViewHolder, mShareElementV2: ShareElementV2) {
        if (mShareElementV2.state == TransferState.CANCELED) {
            mShareElementV2.state = TransferState.CANCELED
            mShareElementV2.errNo = FileServerErrorCode.MSG_ERROR_NO_TASK
            showRemoveDialog(mShareElementV2, helper)
            return
        }
        if (EntityType.SHARE_FILE_V2_COPY == mShareElementV2.type) {
            val sfDownloads = mShareElementV2.sfDownloads
            showDes(helper, mShareElementV2, if (sfDownloads.isEmpty()) null else sfDownloads[0])
        } else if (EntityType.SHARE_FILE_V2_RECEIVE == mShareElementV2.type) {
            mShareViewModel2.subscribeDevice(mShareElementV2.ticket2, false, Consumer { shareElementV2 ->
                if (shareElementV2.state == TransferState.CANCELED) {
                    mShareElementV2.state = TransferState.CANCELED
                    mShareElementV2.errNo = FileServerErrorCode.MSG_ERROR_NO_TASK
                    showRemoveDialog(mShareElementV2, helper)
                    return@Consumer
                }
                if ((mShareElementV2.path).isNullOrEmpty()) {

                    val context = helper.itemView.context
                    mShareViewModel2
                            .getShareDir(mShareElementV2.srcDevId, null, mShareElementV2.ticket1,
                                    File.separator, mShareElementV2.password, context, 10, 0)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : io.reactivex.Observer<FileShareBaseResult<DataShareDir>> {
                                override fun onSubscribe(d: Disposable) {
                                    helper.setGone(R.id.layout_pre_show, true)
                                    helper.setGone(R.id.progress_bar, true)
                                    helper.setGone(R.id.tv_pre_show, false)
                                }

                                override fun onNext(baseResult: FileShareBaseResult<DataShareDir>) {
                                    if (baseResult.isSuccessful) {
                                        val dataShareDir = baseResult.result
                                        if (dataShareDir != null) {
                                            val sFiles = dataShareDir.path
                                            val paths = ArrayList<String>()
                                            for (sFile in sFiles) {
                                                val path = File.separator + sFile.name
                                                paths.add(path)
                                                sFile.path = path
                                            }
                                            mShareElementV2.path = paths
                                            mShareElementV2.remainPeriod = dataShareDir.period.toLong()
                                            mShareElementV2.fromOwner = dataShareDir.userId
                                            if (!TextUtils.isEmpty(dataShareDir.password))
                                                mShareElementV2.password = dataShareDir.password
                                            mShareViewModel2.putToDB(mShareElementV2)
                                        }
                                        val sfDownloads = mShareElementV2.sfDownloads
                                        showDes(helper, mShareElementV2, if (sfDownloads.isEmpty()) null else sfDownloads[0])
                                        notifyItemChanged(helper.adapterPosition)
                                        helper.setGone(R.id.layout_pre_show, false)
                                    } else {
                                        if (baseResult.status == FileServerErrorCode.MSG_ERROR_NO_TASK || baseResult.status == FileServerErrorCode.MSG_ERROR_CANCEL_SHARED) {
                                            mShareElementV2.errNo = baseResult.status
                                            showRemoveDialog(mShareElementV2, helper)
                                        } else
                                            ToastHelper.showToast(FileServerErrorCode.getString(baseResult.status))

                                    }
                                    onComplete()
                                }

                                override fun onError(e: Throwable) {
                                    onComplete()
                                }

                                override fun onComplete() {
                                    notifyDataSetChanged()
                                }
                            })
                } else {
                    val sfDownloads = mShareElementV2.sfDownloads
                    showDes(helper, mShareElementV2, if (sfDownloads.isEmpty()) null else sfDownloads[0])
                }
            })

        } else {
            showDes(helper, mShareElementV2, null)
        }
    }


    private fun showDes(helper: BaseViewHolder, mShareElementV2: ShareElementV2, sfDownload: SFDownload?) {
        val serverShareFileTreeHolder = ServerShareFileTreeHolder(mActivity, mShareElementV2, sfDownload, R.string.share_files_title, R.string.next)
        serverShareFileTreeHolder.showPopupCenter()
        serverShareFileTreeHolder.addDismissLister(object : ServerShareFileTreeHolder.OnDismissListener {
            override fun onDismiss() {
                notifyItemChanged(helper.adapterPosition)
            }
        })
    }

}
