package io.weline.repo.data.remote

import io.reactivex.Observable
import io.weline.repo.api.ApiService
import io.weline.repo.api.DefCopyOrMoveAction
import io.weline.repo.api.RecycleMode
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.OptTagFileError
import io.weline.repo.files.data.FileTag
import io.weline.repo.files.data.SharePathType
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author Raleigh.Luo
 * date：20/9/18 19
 * describe：
 */
class FileRemoteSource(val apiService: ApiService) {
    /**
     * 清空回收站
     */
    fun cleanRecycle(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "cleanrecycle")
        val params = JSONObject()
        params.put("share_path_type", SharePathType.USER.type)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 清空回收站
     */
    fun cleanRecycle(deviceId: String, ip: String, token: String,groupId: Long?=null): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "cleanrecycle")
        val params = JSONObject()
        if(groupId!=null &&groupId>0)
        {
            params.put("share_path_type", SharePathType.GROUP.type)
            params.put("groupId", groupId)
        }else
        {
            params.put("share_path_type", SharePathType.USER.type)
        }
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 恢复回收站
     */
    fun restoreRecycleFile(deviceId: String, ip: String, token: String, sharePathType: Int, path: List<String>,recycleMode: String= RecycleMode.keepall.name): Observable<BaseProtocol<Any>> {
        val body = hashMapOf<String,Any>()
        body.put("method", "recycle")
        val params = hashMapOf<String,Any>()
        params.put("share_path_type", sharePathType)
        params.put("path", path)
        params.put("recycle_mode",recycleMode)
        body.put("params", params)
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 恢复回收站
     */
    fun restoreRecycleFile(
        deviceId: String,
        ip: String,
        token: String,
        sharePathType: Int,
        path: List<String>,
        recycleMode: String= RecycleMode.keepall.name,
        groupId: Long?=null
    ): Observable<BaseProtocol<Any>> {
        val body = hashMapOf<String, Any>()
        body.put("method", "recycle")
        val params = hashMapOf<String, Any>()
        params.put("share_path_type", sharePathType)
        params.put("path", path)
        if(groupId!=null &&groupId>0)
        {
            params.put("groupid", groupId)
        }
        body.put("params", params)
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 获取文件列表
     */
    fun getFileList(deviceId: String, ip: String, token: String, action: String, params1: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", action)
        val params = JSONObject(params1)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getFileList(deviceId, ip, token, body)
    }

    /**
     * 操作文件 删除／获取属性
     */
    fun optFileSync(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        groupId: Long?
    ): Observable<BaseProtocol<Any>> {
        return optFile(deviceId, ip, token, action, path, share_path_type, false,groupId)
    }

    /**
     * 操作文件 删除／获取属性
     */
    fun optFileAsync(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        groupId: Long?=null
    ): Observable<BaseProtocol<Any>> {
        return optFile(deviceId, ip, token, action, path, share_path_type, true,groupId)
    }

    /**
     * 操作文件 删除／获取属性
     */
    fun optFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        groupId: Long
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", action)
        params.put("path", path)
        params.put("async", 1)
        params.put("share_path_type", share_path_type)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 文件去重功能
     */
    fun getDuplicateFiles(deviceId: String, ip: String, token: String, path: JSONArray, share_path_type: Int, type: String, page: Int, num: Int
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", "dup")

        params.put("path", path)
        params.put("share_path_type", share_path_type)

        params.put("type", type)
        params.put("page", page)
        params.put("num", num)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }


    /**
     * 操作文件 删除／获取属性
     */
    fun optFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        async: Boolean = true,
        groupId: Long?=null
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", action)
        params.put("path", path)
        params.put("async", if (async) 1 else 0)
        params.put("share_path_type", share_path_type)
        if(groupId!=null&&  groupId>0)
        {
            params.put("groupid", groupId)
        }
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 操作文件 复制／移动文件
     */
    fun copyOrMoveFile(
        deviceId: String, ip: String, token: String, cmd: String, path: JSONArray,
        share_path_type: Int, toDir: String, des_path_type: Int,
        action: Int = DefCopyOrMoveAction.ACTION_DEFAULT, async: Boolean? = null,
        groupid: Long?=null, to_groupid: Long?=null
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", cmd)
        params.put(
            "path", if (path.length() == 1) {
                path[0]
            } else {
                path
            }
        )
        //覆盖选项，0 自定重命名（默认） ，1 覆盖， -1 不覆盖
        //产品需求：默认为不覆盖，有同名的，需告知用户原因且操作失败
        params.put("action", action)
        toDir?.let {
            params.put("toDir", toDir)
        }
        share_path_type?.let {
            params.put("share_path_type", share_path_type)
        }
        des_path_type?.let {
            params.put("des_path_type", des_path_type)
        }
        async?.let {
            params.put("async", if (async) 1 else 0)
        }
        if(groupid!=null&& groupid>0 && share_path_type==SharePathType.GROUP.type )
        {
            params.put("groupid", groupid)
        }
        if(to_groupid!=null&&  to_groupid>0&& des_path_type==SharePathType.GROUP.type)
        {
            params.put("to_groupid", to_groupid)
        }
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 重命名文件
     */
    fun renameFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        newname: String,
        share_path_type: Int,
        groupId: Long?=null
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", action)
        params.put("path", path)
        params.put("share_path_type", share_path_type)
        params.put("newname", newname)
        params.put("des_path_type", share_path_type)
        if(groupId!=null &&groupId>0)
        {
            params.put("groupId", groupId)
        }
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 搜索
     */
    fun searchFile(
        deviceId: String,
        ip: String,
        token: String,
        params: JSONObject
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        params.put("cmd", "search")
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 解压文件
     */
    fun extractFile(
        deviceId: String, ip: String, token: String, path: String, share_path_type: Int,
        todir: String, des_path_type: Int
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")

        val params = JSONObject()
        params.put("cmd", "extract")
        params.put("path", path)
        params.put("share_path_type", share_path_type)
        params.put("todir", todir)
        params.put("des_path_type", des_path_type)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }

    /**
     * 压缩文件
     */
    fun archiverFile(
        deviceId: String, ip: String, token: String, path: List<String>, share_path_type: Int,
        todir: String, des_path_type: Int
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", "archiver")
        params.put("path", JSONArray(path))
        params.put("share_path_type", share_path_type)
        params.put("todir", todir)
        params.put("des_path_type", des_path_type)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)
    }


    fun queryTaskList(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("service", "ofldown")
        json.put("router", "/show")
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDownloadOffline(deviceId, ip, token, body)
    }


    fun optTaskStatus(
        deviceId: String, ip: String, token: String,
        id: String, btsubfile: List<String>?, cmd: Int
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("service", "ofldown")
        json.put("router", "/ctrl")
        json.put("gid", id)
//        btsubfile?.let {
//            val jsonArray = JSONArray()
//            btsubfile.forEach {
//                jsonArray.put(it)
//            }
//            json.put("btsubfiles", jsonArray)
//
//        }
        json.put("cmd", cmd)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDownloadOffline(deviceId, ip, token, body)
    }


    /**
     * 操作离线文件
     */
    fun addDownloadOfflineTask(
        deviceId: String, ip: String, token: String,
        session: String, service: String,
        router: String, url: String? = null,
        savePath: String, share_path_type: Int, //这二个是下载存放位置
        btfile: String? = null, share_path_type_btfile: Int? = null//这二个种子文件的
    ): Observable<BaseProtocol<Any>> {
        val map = mutableMapOf<String, RequestBody>()
        map["session"] = toRequestBody(session)
        map["service"] = toRequestBody(service)
        map["router"] = toRequestBody(router)
        url?.let { map["url"] = toRequestBody(it) }
        map["savePath"] = toRequestBody(savePath)
        map["share_path_type"] = toRequestBody(share_path_type.toString())
        btfile?.let { map["btfile"] = toRequestBody(it) }
        share_path_type_btfile?.let { map["share_path_type_btfile"] = toRequestBody(it.toString()) }

        return apiService.addDownloadOfflineTask(deviceId, ip, token, map)
    }

    private fun toRequestBody(value: String): RequestBody {
        val requestBody = RequestBody.create(MediaType.parse("text/plain"), value);
        return requestBody;
    }

    fun tags(deviceId: String, ip: String, token: String): Observable<BaseProtocol<List<FileTag>>> {
        val map = hashMapOf<String, Any>()
        map.put("method", "tags")
        val params = hashMapOf<String, Any>()
        params.put("cmd", "list")
        map.put("params", params)
        return apiService.tags(deviceId, ip, token, map)
    }

    fun tagFiles(
        deviceId: String, ip: String, token: String, tagId: Int, sharePathType: Int,
        page: Int, num: Int, ftype: List<String>?, order: String?, pattern: String?
    )
            : Observable<BaseProtocol<Any>> {
        val map = hashMapOf<String, Any>()
        map.put("method", "tag")
        val params = hashMapOf<String, Any>()
        params.put("cmd", "files")
        params.put("id", tagId)
        params.put("share_path_type", sharePathType)
        params.put("page", page)
        params.put("num", num)
        if (ftype != null)
            params.put("ftype", ftype)
        if (order != null)
            params.put("order", order)
        if (pattern != null)
            params.put("pattern", pattern)

        map.put("params", params)
        return apiService.tagFiles(deviceId, ip, token, map)
    }

    fun fileOptTag(
        deviceId: String,
        ip: String,
        token: String,
        cmd: String,
        tagId: Int,
        sharePathType: Int,
        path: List<String>
    )
            : Observable<BaseProtocol<OptTagFileError>> {
        val map = hashMapOf<String, Any>()
        map.put("method", "tag")
        val params = hashMapOf<String, Any>()
        params.put("cmd", cmd)
        params.put("share_path_type", sharePathType)
        params.put("id", tagId)
        params.put("path", path)
        map.put("params", params)
        return apiService.fileOptTag(deviceId, ip, token, map)
    }

    fun copyOrMoveFileV1(
        deviceId: String, ip: String, token: String, cmd: String, path: JSONArray,
        share_path_type: Int, groupId: Long?, toDir: String, des_path_type: Int, toGroupId: Long?,
        action: Int = DefCopyOrMoveAction.ACTION_DEFAULT, async: Boolean? = null
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "manage")
        val params = JSONObject()
        params.put("cmd", cmd)
        params.put(
            "path", if (path.length() == 1) {
                path[0]
            } else {
                path
            }
        )
        //覆盖选项，0 自定重命名（默认） ，1 覆盖， -1 不覆盖
        //产品需求：默认为不覆盖，有同名的，需告知用户原因且操作失败
        params.put("action", action)
        toDir?.let {
            params.put("toDir", toDir)
        }
        share_path_type?.let {
            params.put("share_path_type", share_path_type)
        }
        des_path_type?.let {
            params.put("des_path_type", des_path_type)
        }
        groupId?.let {
            if (it > 0) {
                params.put("groupid", it)
            }
        }

        toGroupId?.let {
            if (it > 0) {
                params.put("to_groupid", it)
            }
        }

        async?.let {
            params.put("async", if (async) 1 else 0)
        }
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optFile(deviceId, ip, token, body)

    }


}