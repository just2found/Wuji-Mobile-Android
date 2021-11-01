package net.sdvn.common.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Network
import net.sdvn.common.IntrDBHelper
import net.sdvn.common.data.remote.NetRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.BindNetsInfo
import net.sdvn.common.internet.protocol.entity.BindNetModel
import net.sdvn.common.vo.NetworkModel
import net.sdvn.common.vo.NetworkModel_
import timber.log.Timber
import java.util.*

/** 

Created by admin on 2020/10/18,00:17

 */
object NetsRepo {

    private val mNetRemoteDataSource = NetRemoteDataSource()

    @JvmStatic
    fun saveData(list: List<BindNetModel>?) {
        val userId = IntrDBHelper.getUserId()
        val box = getBox()
        val localDevList = box.query()
                .equal(NetworkModel_.userId, userId)
                .build()
                .find()
        list?.map { newData ->
            val iterator = localDevList.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (Objects.equals(next.netId, newData.networkId)) {
                    iterator.remove()
                    return@map transform(userId, newData, next)
                }
            }
            return@map transform(userId, newData)
        }?.toList().also {
            box.put(it)
        }
        box.remove(localDevList)
    }

    fun transform(userId: String, newData: BindNetModel, old: NetworkModel? = null): NetworkModel {
        val model = old ?: NetworkModel()
        model.userId = userId
        model.apply {
            netId = newData.networkId
            netName = newData.networkName
            addTime = newData.addTime
            ownerId = newData.ownerId
            firstName = newData.firstname
            lastName = newData.lastname
            loginName = newData.loginname
            nickname = newData.nickname
            netStatus = newData.networkStatus
            srvProvide = newData.isSrvProvide
            userLevel = newData.userLevel
            userStatus = newData.userStatus
            isDevSepCharge = newData.isDevSepCharge
            flowStatus = newData.flowStatus
            isCharge = newData.isCharge
            mainENDeviceId = newData.mainENDeviceId
        }
        return model
    }

    fun getData2(): LiveData<List<NetworkModel>> {
        val box = getBox()
        val build = box.query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId())
                .equal(NetworkModel_.netStatus, 0)
                .equal(NetworkModel_.userStatus, 0)
                .order(NetworkModel_.userLevel)
                .order(NetworkModel_.netId)
                .build()
        return ObjectBoxLiveData(build).switchMap {
            val enableNets = lastEnableNets.toList()
            for (networkModel in it) {
                for (enableNet in enableNets) {
                    if (networkModel.netId == enableNet.id) {
                        networkModel.isCurrent = enableNet.isCurrent
                        if (networkModel.isCurrent) {
                            currentNet = networkModel
                        }
                        break
                    }
                }
            }
            MutableLiveData(it.sortedWith(Comparator<NetworkModel> { o1, o2 ->
                val status1 = (o1.isCharge == true && o1.isDevSepCharge == false &&
                        (o1.flowStatus == 1 || o1.flowStatus == -1))
                val status2 = (o2.isCharge == true && o2.isDevSepCharge == false &&
                        (o2.flowStatus == 1 || o2.flowStatus == -1))
                when {
                    o1.isCurrent -> {
                        -1
                    }
                    o2.isCurrent -> {
                        1
                    }
                    o1.userStatus != o2.userStatus -> {//待同意的在最后
                        if (o1.userStatus == 0) -1 else 1
                    }
                    status1 != status2 -> {
                        if (status1) 1 else -1
                    }
                    o1.userLevel != o2.userLevel -> {
                        if (o1.userLevel <= o2.userLevel) -1 else 1
                    }
                    else -> {
                        if (o2.netId <= o1.netId) 1 else 0
                    }
                }
            }))
        }
    }

    fun getData(): LiveData<List<NetworkModel>> {
        val box = getBox()
        val build = box.query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId())
                .equal(NetworkModel_.netStatus, 0)
                .`in`(NetworkModel_.userStatus, intArrayOf(0, 1))//正常＋待同意
                .order(NetworkModel_.userLevel)
                .order(NetworkModel_.netId)
                .build()
        return ObjectBoxLiveData(build).switchMap {
            val enableNets = lastEnableNets.toList()
            for (networkModel in it) {
                for (enableNet in enableNets) {
                    if (networkModel.netId == enableNet.id) {
                        networkModel.isCurrent = enableNet.isCurrent
                        if (networkModel.isCurrent) {
                            currentNet = networkModel
                        }
                        break
                    }
                }
            }
            MutableLiveData(it.sortedWith(Comparator<NetworkModel> { o1, o2 ->
                val status1 = (o1.isCharge == true && o1.isDevSepCharge == false &&
                        (o1.flowStatus == 1 || o1.flowStatus == -1))
                val status2 = (o2.isCharge == true && o2.isDevSepCharge == false &&
                        (o2.flowStatus == 1 || o2.flowStatus == -1))
                when {
                    o1.isCurrent -> {
                        -1
                    }
                    o2.isCurrent -> {
                        1
                    }
                    o1.userStatus != o2.userStatus -> {//待同意的在最后
                        if (o1.userStatus == 0) -1 else 1
                    }
                    status1 != status2 -> {
                        if (status1) 1 else -1
                    }
                    o1.userLevel != o2.userLevel -> {
                        if (o1.userLevel <= o2.userLevel) -1 else 1
                    }
                    else -> {
                        if (o2.netId <= o1.netId) 1 else 0
                    }
                }
            }))
        }
    }

    fun getOwnData(): LiveData<List<NetworkModel>> {
        val box = getBox()
        val build = box.query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId())
                .equal(NetworkModel_.netStatus, 0)
                .equal(NetworkModel_.userStatus, 0)
                .equal(NetworkModel_.ownerId, IntrDBHelper.getUserId())
                .order(NetworkModel_.netId)
                .build()
        return ObjectBoxLiveData(build).switchMap {
            val enableNets = lastEnableNets.toList()
            for (networkModel in it) {
                for (enableNet in enableNets) {
                    if (networkModel.netId == enableNet.id) {
                        networkModel.isCurrent = enableNet.isCurrent
                        if (networkModel.isCurrent) {
                            currentNet = networkModel
                        }
                        break
                    }
                }
            }
            MutableLiveData(it.sortedWith(getCurrentComparator()))
        }
    }

    fun getNetwork(networkId: String): NetworkModel? {
        return getBox().query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId())
                .equal(NetworkModel_.netId, networkId)
                .build()
                .findFirst()
    }

    fun getOwnNetwork(networkId: String): NetworkModel? {
        val box = getBox()
        val build = box.query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId())
                .equal(NetworkModel_.netId, networkId)
                .build()
        return build.findFirst()
    }

    private fun getBox(): Box<NetworkModel> {

        return IntrDBHelper.getBoxStore().boxFor(NetworkModel::class.java)
    }

    private val listener: ResultListener<BindNetsInfo> = object : ResultListener<BindNetsInfo> {
        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {}
        override fun success(tag: Any?, data: BindNetsInfo) {
            if (data.data != null) {
                saveData(data.data!!.list)
                updateCurrentNetwork()
            }
        }
    }

    private fun updateCurrentNetwork() {
        lastEnableNets.find { it.isCurrent }?.let {
            currentNet = transformNetwork(it)
        }
    }

    fun init() {
        lastEnableNets.clear()
    }

    private var lastEnableNets: MutableList<Network> = mutableListOf()
    private var currentNet: NetworkModel? = null

    fun updateEnable(networkList: List<Network>) {
        if (lastEnableNets.isEmpty()) {
            lastEnableNets.addAll(networkList.also {
                it.find {
                    it.isCurrent
                }?.let {
                    currentNet = transformNetwork(it)
                }
            })
            Timber.d("RefreshENServer")
            refreshNetList() // lastEnableNets.isEmpty()
        } else {
            val oldData = lastEnableNets.toMutableList()
            var isChangedList = 0
            networkList.forEach {
                val iterator = oldData.iterator()
                var isFoundOld = false
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (it.id == next.id) {
                        iterator.remove()
                        isFoundOld = true
                    }
                }
                if (isFoundOld) {
                    isChangedList++
                }
                if (it.isCurrent) {
                    currentNet = transformNetwork(it)
                }
            }
            lastEnableNets.clear()
            lastEnableNets.addAll(networkList.sortedWith(getDefComparator()))
            if (oldData.count() > 0 //网络移除
                    || isChangedList < networkList.count()//有网络新增
            ) {
                refreshNetList()//网络数量变更
            }
        }
        currentNet?.let {
            InNetDevRepo.refreshInNetENDevices(it.netId)
        }

    }

    fun refreshNetList(): HttpLoader {
        return mNetRemoteDataSource.getBindNetsInfo(this.listener)
    }


    fun transformNetwork(network: Network): NetworkModel {
        val findFirst = getBox().query()
                .equal(NetworkModel_.netId, network.id)
                .equal(NetworkModel_.userId, AccountRepo.getUserId())
                .build()
                .findFirst()
        return (findFirst ?: NetworkModel()).apply {
            netId = network.id
            netName = network.name
            isCurrent = network.isCurrent()
            ownerId = network.uid
        }
    }

    fun getCurrentNet(): NetworkModel? {
        return currentNet
    }

    fun getDefComparator() = object : Comparator<Network> {
        override fun compare(o1: Network, o2: Network): Int {
            val userId = AccountRepo.getUserId()
            if (o1.uid == o2.uid) {
                return o1.name.compareTo(o2.name)
            }
            if (userId == o1.uid) return -1
            return if (userId == o2.uid) 1 else o1.uid.compareTo(o2.uid)
        }
    }

    fun getCurrentComparator() = Comparator<NetworkModel> { o1, o2 ->
        when {
            o1.isCurrent -> {
                -1
            }
            o2.isCurrent -> {
                1
            }
            else -> {
                0
            }
        }
    }

    fun getCurrentNetwork(): LiveData<NetworkModel> {
        val box = getBox()
        val build = box.query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId() ?: "")
                .equal(NetworkModel_.netStatus, 0)
                .equal(NetworkModel_.userStatus, 0)
                .build()
        return ObjectBoxLiveData(build).switchMap {
            CMAPI.getInstance().baseInfo.netid?.let {
                MutableLiveData<NetworkModel>(getOwnNetwork(it))
            } ?: let {
                MutableLiveData<NetworkModel>(currentNet)
            }
        }
    }
}