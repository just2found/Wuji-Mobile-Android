package net.linkmate.app.ui.simplestyle.device.remove_duplicate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.RdDuplicateFilesResult
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.NasRepository
import org.json.JSONArray

/** 

Created by admin on 2020/8/20,16:05

 */
class RemoveDuplicateModel : ViewModel() {

    companion object {
        const val UNKWON_TYPE = 1
        const val LONGER_NAME_TYPE = 1
        const val SHORTER_NAME_TYPE = 2
        const val LONGER_PATH_TYPE = 3
        const val SHORTER_PATH_TYPE = 4
        const val EARLIER_TIME_TYPE = 5
        const val LATER_TIME_TYPE = 6

        const val dup = "dup"//文件去重
        const val SHARE_PATH_TYPE = 0//路径类型(0为用户目录，1为配置目录，2公共目录，3全局目录)
        const val ALL_TYPE =
            "all"   //需要去重的文件类型(支持pic/video/audio/txt/doc/pdf/xls/zip/bt/ppt)，或为”show”，默认为”all”
        const val SHOW_TYPE = "show"
        const val PAGE = 0
        const val NUM = 100//每一页的大小
    }

    private var nasRepository: NasRepository =
        NasRepository(SessionManager.getInstance().userId, LibApp.instance.getAppExecutors())

    //这里面是选文件夹
    private var mScreenType = LONGER_NAME_TYPE//记录当前筛选条件
    val folderList = mutableListOf<OneOSFile>()//用于选择路劲的文件夹

    val selectedFolderList = mutableListOf<OneOSFile>() //选中的文件路径

    val selectTypeLiveData by lazy { MutableLiveData<Int>() }
    var mLoginSession: LoginSession? = null

    //这个是选择文件的操作
    val screenTypeLiveData = MutableLiveData<Int>()
    fun executeFilterFile(screenType: Int) {
        //  mScreenType=screenType
        screenTypeLiveData.postValue(screenType)
    }


    fun deleteFile(devId: String, jsonArray: JSONArray): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                V5Repository.INSTANCE().optFile(devId,
                    loginSession!!.ip,
                    LoginTokenUtil.getToken(),
                    "delete",
                    jsonArray,
                    SHARE_PATH_TYPE,
                    object : V5Observer<Any>(devId) {
                        override fun isNotV5() {
                        }

                        override fun retry(): Boolean {
                            V5Repository.INSTANCE().optFile(
                                devId,
                                loginSession.ip,
                                LoginTokenUtil.getToken(),
                                dup,
                                jsonArray,
                                SHARE_PATH_TYPE,
                                this
                            )
                            return true
                        }

                        override fun success(result: BaseProtocol<Any>) {
                            liveData.postValue(Resource.success(result.result));
                        }

                        override fun fail(result: BaseProtocol<Any>) {
                            liveData.postValue(
                                Resource.error(
                                    code = result.error?.code
                                        ?: 0, msg = result.error?.msg ?: "", data = null
                                )
                            )
                        }
                    })

            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error(errorMsg ?: "", null));
            }
        })

        return liveData
    }


    //5.27获取重复文件的
    fun getDuplicateFiles(
        devId: String, share_path_type: Int = SHARE_PATH_TYPE,
        type: String = ALL_TYPE, page: Int = PAGE, num: Int = NUM
    ): LiveData<Resource<RdDuplicateFilesResult>> {
        val liveData = MutableLiveData<Resource<RdDuplicateFilesResult>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mLoginSession = loginSession
                val jsonArray = JSONArray()
                if (selectedFolderList.isNullOrEmpty()) {
                    jsonArray.put("/")
                } else {
                    selectedFolderList.forEach {
                        jsonArray.put(it.getPath())
                    }
                }
                V5Repository.INSTANCE().getDuplicateFiles(devId,
                    loginSession.ip,
                    LoginTokenUtil.getToken(),
                    jsonArray,
                    share_path_type,
                    type,
                    page,
                    num,
                    object : V5Observer<Any>(devId) {
                        override fun isNotV5() {
                        }

                        override fun retry(): Boolean {
                            V5Repository.INSTANCE().getDuplicateFiles(
                                devId,
                                loginSession.ip,
                                LoginTokenUtil.getToken(),
                                jsonArray,
                                share_path_type,
                                type,
                                page,
                                num,
                                this
                            )
                            return true
                        }

                        override fun success(result: BaseProtocol<Any>) {
                            val gson = Gson()
                            result.data?.let {
                                try {
                                    val dataStr = gson.toJson(result.data)
                                    val objectType =
                                        object : TypeToken<RdDuplicateFilesResult>() {}.type
                                    val data =
                                        gson.fromJson<RdDuplicateFilesResult>(dataStr, objectType)
                                    liveData.postValue(Resource.success(data))
                                } catch (e: Exception) {
                                    liveData.postValue(
                                        Resource.error(
                                            code = result.error?.code
                                                ?: 0, msg = result.error?.msg ?: "", data = null
                                        )
                                    );
                                }

                            }
                        }

                        override fun fail(result: BaseProtocol<Any>) {
                            liveData.postValue(
                                Resource.error(
                                    code = result.error?.code
                                        ?: 0, msg = result.error?.msg ?: "", data = null
                                )
                            );
                        }
                    })

            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error(errorMsg ?: "", null, errorNo));
            }
        })
        return liveData
    }

    fun genThumbnailUrl(oneOSPath: String): String? {
        mLoginSession?.let {
            return OneOSAPIs.genThumbnailUrl(it, oneOSPath)
        }
        return null
    }


    //获选用于选择的文件夹路径getFileTreeFromServer是实现
    fun getFolderList(deviceid: String): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val liveData = MediatorLiveData<Resource<BaseResultModel<FileListModel>>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                liveData.addSource(getFileTreeFromServer(loginSession)) { t ->
                    liveData.postValue(t)
                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(
                    Resource.error(
                        code = errorNo
                            ?: 0, msg = errorMsg, data = null
                    )
                );
            }
        })
        return liveData
    }


    //获选用于选择的文件夹路径
    private fun getFileTreeFromServer(loginSession: LoginSession): LiveData<Resource<BaseResultModel<FileListModel>>> {
        return nasRepository.loadChoiceFilesFormServer(
            devId = loginSession.id!!,
            session = loginSession.session!!,
            sharePathType = SharePathType.USER.type,
            path = "/",
            filterList = OneOSFilterType.DIR.getFilterList()
        )
    }


}
