package net.linkmate.app.ui.viewmodel

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.objectbox.android.ObjectBoxDataSource
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.query.Query
import net.linkmate.app.R
import net.linkmate.app.util.DialogUtil
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.IntrDBHelper
import net.sdvn.common.data.remote.MsgRemoteDataSource
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.ProcessNewsHttpLoader
import net.sdvn.common.internet.protocol.ProcessNewsErrorResult
import net.sdvn.common.internet.protocol.entity.SdvnMessage
import net.sdvn.common.repo.AccountRepo
import net.sdvn.common.repo.EnMbPointMsgRepo
import net.sdvn.common.repo.SdvnMsgRepo
import net.sdvn.common.vo.*
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.FileUtils
import org.view.libwidget.singleClick
import java.util.*


class SystemMessageViewModel : ViewModel()//, MessageManager.MessagesListObserver
{
    private var msgModelLiveData: MediatorLiveData<List<MsgModel<*>>>? = null
    private var objectBoxLiveData: ObjectBoxLiveData<MsgCommonModel>? = null
    private var objectBoxLiveData1: ObjectBoxLiveData<SdvnMessageModel>? = null
    private var objectBoxLiveData2: ObjectBoxLiveData<EnMbPointMsgModel>? = null
    private var objectBoxLiveData4: ObjectBoxLiveData<MsgCommonModel>? = null
    private var objectBoxLiveData5: ObjectBoxLiveData<SdvnMessageModel>? = null
    private var objectBoxLiveData6: ObjectBoxLiveData<EnMbPointMsgModel>? = null
    private var enMpointMsgLiveData: LiveData<PagedList<EnMbPointMsgModel>?>? = null
    val messagesLiveData = MediatorLiveData<List<SdvnMessageModel>?>()
    val messagesCommonLiveData = MediatorLiveData<List<MsgCommonModel>?>()
    val messageCountLiveData = MediatorLiveData<Int>()
    private var _userId: String? = null

    fun observerMessageInit() {
        val userId = AccountRepo.getUserId()
        if (!userId.isNullOrEmpty() && _userId != userId) {
            _userId = userId
            val query = SdvnMsgRepo.boxMsgCommon()
                    .query()
                    .equal(MsgCommonModel_.userId, userId)
                    .equal(MsgCommonModel_.wasRead, false)
                    .equal(MsgCommonModel_.display, true)
                    .equal(MsgCommonModel_.expired, false)
                    .orderDesc(MsgCommonModel_.timestamp)
                    .build()
            val query1 = IntrDBHelper.getBoxStore().boxFor(SdvnMessageModel::class.java)
                    .query()
                    .equal(SdvnMessageModel_.userId, userId)
                    .equal(SdvnMessageModel_.wasRead, false)
                    .equal(SdvnMessageModel_.display, true)
                    .equal(SdvnMessageModel_.expired, false)
                    .orderDesc(SdvnMessageModel_.timestamp)
                    .build()
            val query2 = IntrDBHelper.getBoxStore().boxFor(EnMbPointMsgModel::class.java)
                    .query()
                    .equal(EnMbPointMsgModel_.userId, userId)
                    .equal(EnMbPointMsgModel_.wasRead, false)
                    .equal(EnMbPointMsgModel_.display, true)
                    .equal(EnMbPointMsgModel_.expired, false)
                    .orderDesc(EnMbPointMsgModel_.timestamp)
                    .build()

            objectBoxLiveData = ObjectBoxLiveData(query)
            objectBoxLiveData1 = ObjectBoxLiveData(query1)
            objectBoxLiveData2 = ObjectBoxLiveData(query2)
            messageCountLiveData.addSource(objectBoxLiveData1!!) {
                val size = objectBoxLiveData?.value?.size ?: 0
                val size2 = objectBoxLiveData2?.value?.size ?: 0
                messageCountLiveData.postValue(it.size + size + size2)
            }
            messageCountLiveData.addSource(objectBoxLiveData2!!) {
                val size = objectBoxLiveData?.value?.size ?: 0
                val size1 = objectBoxLiveData1?.value?.size ?: 0
                messageCountLiveData.postValue(it.size + size + size1)
            }
            messageCountLiveData.addSource(objectBoxLiveData!!) {
                val size1 = objectBoxLiveData1?.value?.size ?: 0
                val size2 = objectBoxLiveData2?.value?.size ?: 0
                messageCountLiveData.postValue(it.size + size1 + size2)
            }

            messagesLiveData.addSource(objectBoxLiveData1!!) {
                messagesLiveData.postValue(it)
            }
            messagesCommonLiveData.addSource(objectBoxLiveData!!) {
                messagesCommonLiveData.postValue(it)
            }
        }
    }

    fun getEnMsgModeLiveDataPaged(userId: String): LiveData<PagedList<EnMbPointMsgModel>?> {
        val boxFor = IntrDBHelper.getBoxStore().boxFor(EnMbPointMsgModel::class.java)
        if (enMpointMsgLiveData == null || !Objects.equals(_userId, userId)) {
            val query: Query<EnMbPointMsgModel> = boxFor.query()
                    .equal(EnMbPointMsgModel_.userId, userId)
                    .equal(EnMbPointMsgModel_.wasRead, false)
                    .or()
                    .equal(EnMbPointMsgModel_.display, true)
                    .orderDesc(EnMbPointMsgModel_.timestamp)
                    .build()
            // build LiveData
            enMpointMsgLiveData = LivePagedListBuilder(
                    ObjectBoxDataSource.Factory<EnMbPointMsgModel>(query),
                    20 /* page size */
            ).build()
            _userId = userId
        }
        return enMpointMsgLiveData!!
    }

    fun getMsgModelLiveData(userId: String): LiveData<List<MsgModel<*>>> {
        if (msgModelLiveData == null || _userId != userId) {
            _userId = userId
            msgModelLiveData = MediatorLiveData<List<MsgModel<*>>>()
            val query = SdvnMsgRepo.boxMsgCommon()
                    .query()
                    .equal(MsgCommonModel_.userId, userId)
                    .equal(MsgCommonModel_.wasRead, false)
                    .or()
                    .equal(MsgCommonModel_.display, true)
                    .orderDesc(MsgCommonModel_.timestamp)
                    .build()
            val query1 = IntrDBHelper.getBoxStore().boxFor(SdvnMessageModel::class.java)
                    .query()
                    .equal(SdvnMessageModel_.userId, userId)
                    .equal(SdvnMessageModel_.wasRead, false)
                    .or()
                    .equal(SdvnMessageModel_.display, true)
                    .orderDesc(SdvnMessageModel_.timestamp)
                    .build()
            val query2 = IntrDBHelper.getBoxStore().boxFor(EnMbPointMsgModel::class.java)
                    .query()
                    .equal(EnMbPointMsgModel_.userId, userId)
                    .equal(EnMbPointMsgModel_.wasRead, false)
                    .or()
                    .equal(EnMbPointMsgModel_.display, true)
                    .orderDesc(EnMbPointMsgModel_.timestamp)
                    .build()

            objectBoxLiveData4 = ObjectBoxLiveData(query)
            objectBoxLiveData5 = ObjectBoxLiveData(query1)
            objectBoxLiveData6 = ObjectBoxLiveData(query2)

            msgModelLiveData!!.addSource(objectBoxLiveData4!!) {
                val value2 = objectBoxLiveData5!!.value
                val value1 = objectBoxLiveData6!!.value
                val list = mutableListOf<MsgModel<*>>()
                if (it != null) {
                    list.addAll(it)
                }
                if (value1 != null) {
                    list.addAll(value1)
                }
                if (value2 != null) {
                    list.addAll(value2)
                }
                msgModelLiveData?.postValue(list.sortedByDescending { it.timestamp })
            }
            msgModelLiveData!!.addSource(objectBoxLiveData5!!) {
                val value2 = objectBoxLiveData4!!.value
                val value = objectBoxLiveData6!!.value
                val list = mutableListOf<MsgModel<*>>()
                if (it != null) {
                    list.addAll(it)
                }
                if (value != null) {
                    list.addAll(value)
                }
                if (value2 != null) {
                    list.addAll(value2)
                }
                msgModelLiveData?.postValue(list.sortedByDescending { it.timestamp })

            }
            msgModelLiveData!!.addSource(objectBoxLiveData6!!) {
                val value1 = objectBoxLiveData4!!.value
                val value = objectBoxLiveData5!!.value
                val list = mutableListOf<MsgModel<*>>()
                if (it != null) {
                    list.addAll(it)
                }
                if (value != null) {
                    list.addAll(value)
                }
                if (value1 != null) {
                    list.addAll(value1)
                }
                msgModelLiveData?.postValue(list.sortedByDescending { it.timestamp })
            }

        }
        return msgModelLiveData!!
    }

//    override fun onMessagesListChanged(newCount: Int, messages: MutableList<SdvnMessage>?) {
//        messageCountLiveData.postValue(newCount)
//        messagesLiveData.postValue(messages)
//    }


    fun markAllMsgRead(list: List<MsgModel<*>>?) {
        list?.let {
            val sdvnMsgs = mutableListOf<SdvnMessageModel>()
            val enMsgs = mutableListOf<EnMbPointMsgModel>()
            val msgs = mutableListOf<MsgCommonModel>()

            for (msgModel in it) {
                msgModel.isWasRead = true
                if (msgModel is SdvnMessageModel) {
                    sdvnMsgs.add(msgModel)
                } else if (msgModel is EnMbPointMsgModel) {
                    enMsgs.add(msgModel)
                } else if (msgModel is MsgCommonModel) {
                    msgs.add(msgModel)
                }
            }
            SdvnMsgRepo.updateCommonData(msgs)
            SdvnMsgRepo.saveData(sdvnMsgs)
            EnMbPointMsgRepo.saveData(enMsgs)
        }
    }

    fun removeMsg(context: Context, data: MsgModel<*>) {
        DialogUtils.showConfirmDialog(context, 0, R.string.waring_remove_title, R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
            dialog.dismiss()
            if (isPositiveBtn) {
                if (data is EnMbPointMsgModel) {
                    data.isDisplay = false
                    data.isWasRead = true
                    EnMbPointMsgRepo.saveData(data)
                } else if (data is SdvnMessageModel) {
                    data.isDisplay = false
                    data.isWasRead = true
                    SdvnMsgRepo.saveData(data)
                } else if (data is MsgCommonModel) {
                    data.isDisplay = false
                    data.isWasRead = true
                    SdvnMsgRepo.updateCommonData(data)
                }
            }

        }
    }

    fun processSystemMsg(context: Context, model: SdvnMessageModel) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_process_system_msg, null)
        view.findViewById<TextView>(R.id.tv_name).setText(model.username)
        view.findViewById<TextView>(R.id.tv_content).setText(model.message)
        if (model.timestamp > 0)
            view.findViewById<TextView>(R.id.tv_date).setText(FileUtils.fmtTimeByZone(model.timestamp))
        val status = model.status
        view.findViewById<TextView>(R.id.tv_status).setText(getStByStatus(status))
        val positive = view.findViewById<TextView>(R.id.positive)
        val negative = view.findViewById<TextView>(R.id.negative)
        val viewGroupNegative = view.findViewById<View>(R.id.group_negative)
        val viewGroupPositive = view.findViewById<View>(R.id.group_positive)
        val loadingView = view.findViewById<View>(R.id.layout_loading)
        val showCustomDialog = DialogUtils.showCustomDialog(context, view)
        showCustomDialog.setCancelable(true)
        showCustomDialog.setCanceledOnTouchOutside(true)
        when (status) {
            SdvnMessage.MESSAGE_STATUS_AGREE,
            SdvnMessage.MESSAGE_STATUS_DISAGREE -> {
                viewGroupPositive.isVisible = false
                viewGroupNegative.isVisible = true
                negative.setText(R.string.ok)
                negative.setOnClickListener {
                    showCustomDialog.dismiss()
                }
            }
            SdvnMessage.MESSAGE_STATUS_WAIT -> {
                viewGroupPositive.isVisible = true
                viewGroupNegative.isVisible = true
                positive.setText(R.string.agree)
                positive.setOnClickListener(process(loadingView, model, showCustomDialog, SdvnMessage.MESSAGE_STATUS_AGREE))
                negative.setText(R.string.disagree)
                negative.setOnClickListener(process(loadingView, model, showCustomDialog, SdvnMessage.MESSAGE_STATUS_DISAGREE))
            }
        }

    }

    private fun process(loadingView: View, model: SdvnMessageModel, showCustomDialog: Dialog, status: String): (View) -> Unit {
        return {
            loadingView.isVisible = true
            processMessage(model, status, Callback {
                loadingView.isVisible = false
                showCustomDialog.dismiss()
            })
        }
    }

    private fun processMC(loadingView: View, model: MsgCommonModel, showCustomDialog: Dialog, status: Boolean, auth: String? = null) {
        loadingView.isVisible = true
        processMessageMC(model, status, auth, Callback {
            loadingView.isVisible = false
            if (auth == null || it) {
                showCustomDialog.dismiss()
            }
        })
    }

    private fun processMessageMC(model: MsgCommonModel, process: Boolean, auth: String? = null, callback: Callback<Boolean>) {
        MsgRemoteDataSource().processMessages(model.msgId, process, AccountRepo.getTicket(), auth, object : ResultListener<GsonBaseProtocol?> {
            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                var changed = false
                when (baseProtocol.result) {
                    SdvnHttpErrorNo.EC_MSG_HAS_ALREADY_AGREE -> {
                        changed = true
                        model.confirmAck = MsgCommonModel.MESSAGE_STATUS_AGREE
                    }
                    SdvnHttpErrorNo.EC_MSG_HAS_ALREADY_DISAGREE -> {
                        changed = true
                        model.confirmAck = MsgCommonModel.MESSAGE_STATUS_DISAGREE
                    }
                    SdvnHttpErrorNo.EC_MSG_NO_NEED_PROCESSED -> {
                        changed = true
                        model.isWasRead = true
                    }
                    SdvnHttpErrorNo.EC_MSG_HAS_ALREADY_EXPIRED -> {
                        changed = true
                        model.expired = true
                    }
                }
                if (changed) {
                    SdvnMsgRepo.updateCommonData(model)
                }
                ToastUtils.showError(baseProtocol.result)
                callback.result(false)
            }

            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                SdvnMsgRepo.updateCommonData(model.apply {
                    this.confirmAck = if (process) {
                        MsgCommonModel.MESSAGE_STATUS_AGREE
                    } else {
                        MsgCommonModel.MESSAGE_STATUS_DISAGREE
                    }
                })
                callback.result(true)
            }
        })
    }

    private fun processMessage(data: SdvnMessageModel, process: String, callback: Callback<Boolean>) {
        val loader = ProcessNewsHttpLoader(ProcessNewsErrorResult::class.java);
        loader.setParams(data.getMsgId(), process);
        loader.executor(object : ResultListener<GsonBaseProtocol> {

            override fun success(tag: Any?, data1: GsonBaseProtocol?) {
                data.status = process
                SdvnMsgRepo.saveData(data)
                callback.result(true)
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                var result = false
                if (baseProtocol is ProcessNewsErrorResult) {
                    val errorResult = baseProtocol;
                    if (errorResult.result == SdvnHttpErrorNo.EC_NEWS_HAS_BEEN_PROCESSED && errorResult.process != null) {
                        data.status = errorResult.process
                        result = true
                        SdvnMsgRepo.saveData(data)
                    } else if (errorResult.result == SdvnHttpErrorNo.EC_NEWSID_NOT_FIND) {
                        data.isDisplay = false
                        SdvnMsgRepo.saveData(data);
                    }
                }
                ToastUtils.showError(baseProtocol?.result ?: 0);
                callback.result(result)
            }
        });
    }

    fun clearMsgModelLiveData() {
        _userId = null
        objectBoxLiveData?.let {
            messageCountLiveData.removeSource(it)
            objectBoxLiveData = null
        }
        objectBoxLiveData1?.let {
            messageCountLiveData.removeSource(it)
            objectBoxLiveData1 = null
        }
        objectBoxLiveData2?.let {
            messageCountLiveData.removeSource(it)
            objectBoxLiveData2 = null
        }
        msgModelLiveData?.let { msgModelLiveData ->
            objectBoxLiveData4?.let {
                msgModelLiveData.removeSource(it)
                objectBoxLiveData4 = null
            }
            objectBoxLiveData5?.let {
                msgModelLiveData.removeSource(it)
                objectBoxLiveData5 = null
            }
            objectBoxLiveData6?.let {
                msgModelLiveData.removeSource(it)
                objectBoxLiveData6 = null
            }
            msgModelLiveData.postValue(null)
        }
        messagesLiveData.postValue(null)
        messagesCommonLiveData.postValue(null)
        messageCountLiveData.postValue(0)
        msgModelLiveData = null
    }

    override fun onCleared() {
//        MessageManager.getInstance().deleteMessagesListObserver(this)
        clearMsgModelLiveData()
        super.onCleared()
    }

    fun processMsgCommon(context: Context, model: MsgCommonModel) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_process_system_msg, null)
        view.findViewById<TextView>(R.id.tv_name).setText(model.title)
        view.findViewById<TextView>(R.id.tv_content).setText(model.content)
        if (model.timestamp > 0)
            view.findViewById<TextView>(R.id.tv_date).setText(FileUtils.fmtTimeByZone(model.timestamp / 1000))
        val status = model.confirmAck
        view.findViewById<TextView>(R.id.tv_status).setText(if (model.expired) {
            R.string.expired
        } else {
            if (model.isNeedConfirm()) {
                getStByConfirmAck(model.confirmAck)
            } else {
                R.string.empty
            }
        })
        val positive = view.findViewById<TextView>(R.id.positive)
        val negative = view.findViewById<TextView>(R.id.negative)
        val viewGroupNegative = view.findViewById<View>(R.id.group_negative)
        val viewGroupPositive = view.findViewById<View>(R.id.group_positive)
        val loadingView = view.findViewById<View>(R.id.layout_loading)
        val showCustomDialog = DialogUtils.showCustomDialog(context, view)
        showCustomDialog.setCancelable(true)
        showCustomDialog.setCanceledOnTouchOutside(true)
        if (!model.expired && model.isNeedConfirm) {
            when (status) {
                MsgCommonModel.MESSAGE_STATUS_AGREE,
                MsgCommonModel.MESSAGE_STATUS_DISAGREE -> {
                    viewGroupPositive.isVisible = false
                    viewGroupNegative.isVisible = true
                    negative.setText(R.string.ok)
                    negative.setOnClickListener {
                        showCustomDialog.dismiss()
                    }
                }
                MsgCommonModel.MESSAGE_STATUS_WAIT -> {
                    viewGroupPositive.isVisible = true
                    viewGroupNegative.isVisible = true
                    positive.setText(R.string.agree)
                    positive.singleClick {
                        if (model.confirm == MsgModel.CONFIRM_WITH_PWD) {
                            DialogUtil.showSimpleEditDialog(context, R.string.verify_pwd, R.string.hint_enter_pwd, R.string.confirm, { v, strEdit, dialog, _ ->
                                if (TextUtils.isEmpty(strEdit)) {
                                    AnimUtils.sharkEditText(context, v)
                                } else {
                                    dialog.dismiss()
                                    processMC(loadingView, model, showCustomDialog, true, strEdit)
                                }
                            }, { _, _, dialog, _ ->
                                dialog.dismiss()
                            })
                        } else {
                            processMC(loadingView, model, showCustomDialog, true)
                        }
                    }
                    negative.setText(R.string.disagree)
                    negative.singleClick { processMC(loadingView, model, showCustomDialog, false) }
                }
            }
        } else {
            viewGroupPositive.isVisible = false
            viewGroupNegative.isVisible = true
            negative.setText(R.string.ok)
            negative.setOnClickListener {
                showCustomDialog.dismiss()
            }
        }
    }

}


@StringRes
fun getStByStatus(status: String): Int {
    return when (status) {
        SdvnMessage.MESSAGE_STATUS_AGREE -> R.string.agreed
        SdvnMessage.MESSAGE_STATUS_DISAGREE -> R.string.disagreed
        SdvnMessage.MESSAGE_STATUS_WAIT -> R.string.to_be_confirmed
        else -> R.string.to_be_confirmed
    }
}

@StringRes
fun getStByConfirmAck(status: Int): Int {
    return when (status) {
        MsgCommonModel.MESSAGE_STATUS_AGREE -> R.string.agreed
        MsgCommonModel.MESSAGE_STATUS_DISAGREE -> R.string.disagreed
        MsgCommonModel.MESSAGE_STATUS_WAIT -> R.string.to_be_confirmed
        else -> R.string.to_be_confirmed
    }
}