package net.linkmate.app.poster.repository

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.*
import net.linkmate.app.R
import net.linkmate.app.poster.database.AppDatabase
import net.linkmate.app.poster.model.*
import net.linkmate.app.poster.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.File
import java.io.StringReader
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val MOVIE_POSTER = "movie-poster"      //海报墙根目录，无此目录则无海报墙
const val MOVIE_POSTER_MOVIE = "movie"       //电影文件夹
const val MOVIE_POSTER_TV_SHOW = "tvshow"    //剧集文件夹
const val MOVIE_POSTER_MENU = "menu"         //menu根目录 无此目录则无分类无海报墙
const val MOVIE_POSTER_FOLDER = "folder"     //menu文件夹 普通分类规则，必须要有lists，否则无影片
const val MOVIE_POSTER_NFO = "nfo"           //menu文件夹 nfo分类规则，如有lists，lists影片列表优先
const val MOVIE_POSTER_ALL = "all"           //menu文件夹 all分类规则，同级文件夹下lists所列影片
const val MOVIE_POSTER_LISTS = "lists"       //影片列表 放在menu/folder/nfo/all文件下，表示所在位置的一级或二级分类的所有影片列表
const val MOVIE_POSTER_DIVIDED = "-"         //拼接符
const val MOVIE_POSTER_WALL = "movie-poster-wall"   //海报墙背景
const val MOVIE_POSTER_LOGO = "movie-poster-logo"   //海报墙/设备LOGO
const val MOVIE_POSTER_BG = "movie-poster-bg"       //设备背景图
const val MOVIE_POSTER_COVER = "movie-poster-cover" //设备缩略图
const val MOVIE_POSTER_IN = "movie-poster-in"       //设备描述文字
const val TRAILER = "-trailer"    //影片预告 trailer1 trailer2 ……
const val SAMPLE = "-sample"      //影片片段 sample1 sample2 ……
const val POSTER = "-poster"      //影片海报 <电影文件名>-poster.jpg
const val FANART = "-fanart"      //影片背景 <电影文件名>-fanart.png
const val TAR_META_VERSION_PATH = "/movie-poster/.meta.json"
const val TAR_META_FILES = ".meta.files"

class PosterRepository : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val tag = "PosterRepository"
    private lateinit var api: Api
    private var apiCrm: Api? = null
    private lateinit var context: Context
    private lateinit var session: String
    private lateinit var deviceId: String
    private lateinit var ip: String
    private val tarFileName = "tar.gz"
    private lateinit var tarUrlStr: String
    private lateinit var tarDirPath: String
    private lateinit var tarFilePath: String
    //private val tarMetaFilesPath = "${tarDirPath}${TAR_META_FILES_PATH}"

    private lateinit var listener: OnPullTarListener
    private val dispatcher by lazy {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
                .asCoroutineDispatcher()
    }

//    private val tarFilesPath by lazy { Vector<String>() }
//    private val nfos by lazy { Vector<String>() }
    private val medias by lazy { HashMap<String, String>() }
//    private val bdmvs by lazy { Vector<String>() }
    private val posters by lazy { HashMap<String, ArrayList<String>>() }
    private val fanarts by lazy { HashMap<String, ArrayList<String>>() }
    private val samples by lazy { HashMap<String, ArrayList<String>>() }
    private val trailers by lazy { HashMap<String, ArrayList<String>>() }

    private val topTabs by lazy { Vector<TopTabModel>() }
    private val leftTabs by lazy { Vector<LeftTabModel>() }

    private val infos by lazy { Vector<MediaInfoModel>() }
    private lateinit var deviceInfo: DeviceInfoModel

//    private var nfoCount = 0
//    private var parsingNfoCount = 0
    private val retryCount = 3
    private var progressStep = 1 // 1下载 -> 2解压 -> 3解析file -> 4解析movie
    private var mProgress = 0

    private var tarVersion: String? = null
    private var scope: CoroutineScope? = null


    interface OnPullTarListener {
        fun onStart()
        fun onProgress(progress: Int)
        fun onSuccess(data: List<TopWithLeftTabModel>, deviceInfoModel: DeviceInfoModel?, isScan: Boolean)
        fun onToFileFragment()
        fun update()
        fun onNoTar()
    }

    private suspend fun runOnUI(block: () -> Unit) = withContext(Dispatchers.Main) {
        block()
    }

    fun test(){
        val mao = HashMap<String, String>()
    }

    /**
     * 获取海报墙数据
     *
     * 检查db是否有数据
     * Y -> [listener].onQuerySuccess()返回数据 并检查是否有跟新
     * N -> 获取并解析网络数据 -> 存db -> 查询db接口返回数据
     */
    fun getPosterData(_listener: OnPullTarListener, _session: String, _id: String, _ip: String, _context: Context) = launch {
        Timber.i("$tag  getPosterData")
        scope = this
        api = RetrofitRep().initRetrofit(_ip)
        listener = _listener
        context = _context
        session = _session
        deviceId = _id
        ip = _ip

        initData()

        if (checkDB(this, false)) {
//            if (!isActive) return@launch
//            checkTarVersion(deviceId, session, TAR_META_VERSION_PATH, context)
        }
        else{

            getTarAndParse()

        }

    }

    private fun initData(){
        deviceInfo = AppDatabase.getInstance(context).getDeviceInfoDao().getDeviceInfo(deviceId) ?: DeviceInfoModel(deviceId)
        tarUrlStr = "http://${ip}:9898/file/download?session=${session}&path=/movie-poster/.meta.tar.gz"
        tarDirPath = "${context.filesDir?.path}/tar/${deviceId}"
        tarFilePath = "${tarDirPath}/${tarFileName}"
    }

    private fun clearData(){
//        "initData".log(tag)
        medias.clear()
        posters.clear()
        fanarts.clear()
        trailers.clear()
        samples.clear()
        topTabs.clear()
        leftTabs.clear()
        infos.clear()
    }

    /**
     *  ==================================== 获取数据并解析 ======================================
     * 1 下载tar包
     *   1.1   没有tar包，[checkIsPosterType]检查是否海报墙文件规则
     *   1.1.1 是 -> 调用[updateTarHttp]打包，成功后继续下载
     *   1.1.2 不是 -> 则跳文件列表页面
     * 2 解析tar
     * 3 存db
     * 4 检查db
     */
    fun getTarAndParse() = launch {
        Timber.i("$tag  getTarAndParse")
        scope = this
        if(!checkIsPosterType()){
            if (isActive) {
                runOnUI { listener.onToFileFragment() }
                destroy()
                return@launch
            }
        }
        if (isActive) {
            runOnUI { listener.onStart() }
            startProgress(this)
        }
        /** 1 下载tar包 */
        var dir = downAndUnTar()
        if(dir == null){
            runOnUI { listener.onNoTar() }
            /*if(checkIsPosterType()){
                if (!isActive) return@launch
                runOnUI { listener.onNoTar() }
            }
            else {
                if (isActive) {
                    runOnUI { listener.onToFileFragment() }
                }
            }*/
            destroy()
            FileUtils.deleteFolderFile(tarDirPath)
            return@launch
        }
        launch {
            this.launch {
                //如果是检查过版本则不必再重复查询
                if(tarVersion == null){
                    tarVersion = getTarVersionStr(session, TAR_META_VERSION_PATH, context)
                }
                deviceInfo.updateTime = tarVersion as String
            }
            /** 2 解析tar */
            clearData()
            parseTar(this, dir)
        }.join()
        FileUtils.deleteFolderFile(tarDirPath)

        /** 3 存db */
        insertDB()
        clearData()

        mProgress = 100
        runOnUI { listener.onProgress(mProgress) }

        /** 4 检查db */
        checkDB(this, true)

//        destroy()
    }

    private suspend fun startProgress(scope: CoroutineScope){
        mProgress = 0
        progressStep = 1
        scope.launch {
            while (isActive && mProgress < 98){
                if(mProgress < progressStep*25){
                    mProgress++
                    runOnUI { listener.onProgress(mProgress) }
                }
                delay(500)
            }
        }
    }

    /**
     * 下载和解压tar包
     * @return File 返回解压后的文件跟目录
     */
    private suspend fun downAndUnTar() : File?{
        Timber.i("$tag  downTar  $tarUrlStr")
        val result = HttpDownloaderUtil().downFile(tarUrlStr, tarDirPath, tarFileName)
        if(result == 0){
            mProgress = 25
            runOnUI { listener.onProgress(mProgress) }
            progressStep = 2
            val file = File(tarFilePath)
            if(!file.exists() || !file.isFile || file.length()/1024 == 0L){
                return null
            }
            Timber.i("$tag  downTar  ${file.length() / 1024} kb")
            FileUtils.untarGZip(file, tarDirPath)
            if (!isActive) return null
            mProgress = 50
            runOnUI { listener.onProgress(mProgress) }
            progressStep = 3
            return File(tarDirPath)
        }
        return null
    }

    private suspend fun checkIsPosterType(): Boolean {
        Timber.i("$tag  checkIsPosterType")
        var len = 0
        var file = getFileList("/movie-poster") ?: return false
        if(file.result){
            file.data?.files?.apply {
                filter { it.name == "menu" || it.name == "movie"}.onEach{
                    len++
                }
            }
        }
        return len == 2
    }

    private suspend fun getFileList(path: String): BaseResult<FileListResult>? {
        Timber.i("$tag  getFileList")
        try {
            if(apiTime == -1L){
                apiTime = Date().time/1000
            }
            val params = HashMap<String, Any>()
            params["path"] = path
            params["share_path_type"] = 2
            params["show_hidden"] = 0
            params["ftype"] = ""
            params["order"] = ""
            params["page"] = 0
            params["num"] = 10000
            val body = HashMap<String, Any>()
            body["method"] = "list"
            body["session"] = session
            body["params"] = params

            val res = api.fileList(body)
            if (!isActive) return null
            apiTime = -1L
            return res

        } catch (e: Exception) {
            val time = Date().time/1000
            if (e is SocketTimeoutException && time - apiTime < 10){
                return getFileList(path)
            }
            else{
                apiTime = -1L
                return null
            }
        }
        return null
    }
    private var apiTime = -1L

    private suspend fun parseTar(scope: CoroutineScope, tarDir: File) {
        Timber.i("$tag  parseTar")
        if (tarDir.exists() && tarDir.isDirectory){
            tarDir.listFiles().filter { MOVIE_POSTER == it.name }.onEach { file ->
                file.listFiles().apply {
                    filter {
                        it.isFile && it.name?.startsWith(TAR_META_FILES, true) == true
                    }.onEach {
                        parseMetaFile(it)
                        mProgress = 75
                        runOnUI { listener.onProgress(mProgress) }
                        progressStep = 4
                    }

                    filter {
                        it.isFile && it.name?.startsWith(MOVIE_POSTER_IN, true) == true
                    }.onEach {
                        scope.launch {
                            paraInTxt(it, deviceInfo)
                        }
                    }

                    filter {
                        it.isDirectory && it.name?.equals(MOVIE_POSTER_MENU, true) == true
                    }.onEach {
                        scope.launch {
                            paraOneMenuFile(it)
                        }
                    }

                    filter {
                        it.isDirectory && it.name?.equals(MOVIE_POSTER_MOVIE, true) == true
                    }.onEach {
                        scope.launch {
                            parseMovie(scope, it)
                        }
                    }
                }
            }
        }
        else{
            if (isActive) {
                scope.launch { listener.onToFileFragment() }
                cancel()
            }
        }
    }

    private fun parseMetaFile(file: File) {
        Timber.i("$tag  parseMetaFile  ${file.name}:${file.length()}")

        val pathStr = FileUtils.loadFromFile(file)
        if (!isActive) return
        if(pathStr != null){
            pathStr.split("\n").forEach {
                if (!isActive) return
                if(it.contains("/") && !it.endsWith(".thumb")){
                    if(it.contains(".") ){
                        if( it.contains("/${MOVIE_POSTER}/${MOVIE_POSTER_BG}", true)) {
                            deviceInfo.movie_poster_bg = it
//                            "parseMetaFile $it".log(tag)
                        }
                        else if (it.contains("/${MOVIE_POSTER}/${MOVIE_POSTER_COVER}", true)) {
                            deviceInfo.movie_poster_cover = it
//                            "parseMetaFile $it".log(tag)
                        }
                        else if (it.contains("/${MOVIE_POSTER}/${MOVIE_POSTER_LOGO}", true)) {
                            deviceInfo.movie_poster_logo = it
//                            "parseMetaFile $it".log(tag)
                        }
                        else if (it.contains("/${MOVIE_POSTER}/${MOVIE_POSTER_WALL}", true)) {
                            deviceInfo.movie_poster_wall = it
//                            "parseMetaFile $it".log(tag)
                        }
                        else if (it.startsWith("/movie-poster/movie") && !it.contains("bdmv", true)) {
//                            "parseMetaFile $it".log(tag)
                            parseMetaFileMovie(it)
                        }
                        /*else if(it.startsWith("/movie-poster/menu")){

                        }*/
                    }
                    else if(it.startsWith("/movie-poster/movie") && it.endsWith("bdmv", true)){
//                        "parseMetaFile $it".log(tag)
                        val dirPath = it.substring(0, it.lastIndexOf("/"))
                        medias[dirPath] = it
                    }
                }
            }
        }
        else{
            if (isActive) {
                launch { listener.onToFileFragment() }
                cancel()
            }
        }
        Timber.i("$tag  parseMetaFile  结束")
    }

    private fun paraInTxt(file: File, deviceInfo: DeviceInfoModel) {
        val txtStr = FileUtils.loadFromFile(file)
        if (txtStr != null) {
            val list = txtStr.split("\r\n")
            if (list.size >= 4) {
                deviceInfo.name = list[0]
                deviceInfo.plot = list[1]
                //deviceInfo.updateTime = list[2]
                deviceInfo.type = list[3]
                //currentGetDataDevice?.newNumber = list[4]
                Timber.i("$tag  paraInTxt----成功  ${file.name}:${file.length()}")
            }
        }
        else{
            Timber.i("$tag  paraInTxt----失败")
        }
    }

    private fun parseMetaFileMovie(filePath: String){
        val dirPath = filePath.substring(0, filePath.lastIndexOf("/"))
        if(isVideo(filePath) && filePath.contains("trailer", true)) {
            if (trailers[dirPath] == null){
                trailers[dirPath] = arrayListOf(filePath)
            }
            else {
                trailers[dirPath]?.add(filePath)
            }
        }
        else if(isVideo(filePath) && filePath.contains("sample", true)) {
            if (samples[dirPath] == null){
                samples[dirPath] = arrayListOf(filePath)
            }
            else {
                samples[dirPath]?.add(filePath)
            }
        }
        else if(isImage(filePath) && filePath.contains("fanart")) {
            if (fanarts[dirPath] == null){
                fanarts[dirPath] = arrayListOf(filePath)
            }
            else {
                fanarts[dirPath]?.add(filePath)
            }
        }
        else if(isImage(filePath) && filePath.substring(0, filePath.lastIndexOf("."))
                        .endsWith("poster", true)) {
            if (posters[dirPath] == null){
                posters[dirPath] = arrayListOf(filePath)
            }
            else {
                posters[dirPath]?.add(filePath)
            }
        }
        else if(isVideo(filePath)) {
            medias[dirPath] = filePath
        }
    }

    /**
     * menu  解析
     */
    private fun paraOneMenuFile(file: File) {
        Timber.i("$tag  paraOneMenu")
        if (file.exists() && file.isDirectory){
            file.listFiles().forEach {
                try {
                    val name = it.name.toLowerCase(Locale.ROOT)
                    when {
                        name.contains("nfo") -> {
                            // 如果包含nfo，解析文件夹名字规则
                            val info = name.split("-")
                            val condition = info[4].split("&")
                            val list = ArrayList<String>()
                            list.addAll(condition)
                            val topTabModel = TopTabModel(TYPE_NFO, info[0].toInt(), info[1], info[3], list)
                            topTabModel.topId = "${deviceId}--${topTabModel.index}"
                            topTabModel.deviceId = deviceId
                            topTabs.add(topTabModel)
                            paraSecondMenu(
                                    it,
                                    topTabModel.topId,
                                    "${topTabModel.deviceId}#${topTabModel.name}#${topTabModel.index}"
                            )
                        }
                        name.contains("all") -> {
                            // 如果包含all，说明是全部分类
                            val info = name.split("-")
                            val topTabModel = TopTabModel(TYPE_ALL, info[0].toInt(), info[1])
                            topTabModel.topId = "${deviceId}--${topTabModel.index}"
                            topTabModel.deviceId = deviceId
                            topTabs.add(topTabModel)
                            paraSecondMenu(
                                    it,
                                    topTabModel.topId,
                                    "${topTabModel.deviceId}#${topTabModel.name}#${topTabModel.index}"
                            )
                        }
                        name.contains("folder") -> {
                            // 如果包含folder，即获取下级目录的lists.txt文件内容
                            paraListsTxt(it)
                        }
                        else -> Unit
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        else{
            if (isActive) {
                launch { listener.onToFileFragment() }
                cancel()
            }
        }
    }

    private fun paraListsTxt(file: File) {
        val fileList = file.listFiles()
        fileList.filter { it.name.equals("lists.txt", true) }.onEach { it ->
            Timber.i("$tag  paraListsTxt ${it.name}:${it.length()}")
            val listsStr = FileUtils.loadFromFile(it)
            Timber.i("$tag  paraListsTxt res $this")
            listsStr?.apply {
                val split = this.split("\n")
                val names = ArrayList<String>()
                split.forEach { n ->
                    names.add(n.trimStart().trimEnd())
                }
//                names.addAll(split)
                val info = file.name.split("-")
                val topTabModel = TopTabModel(
                        TYPE_FOLDER, info[0].toInt(), info[1], folderMovieList = names
                )
                topTabModel.topId = "${deviceId}--${topTabModel.index}"
                topTabModel.deviceId = deviceId
                topTabs.add(topTabModel)
                paraSecondMenu(
                        file,
                        topTabModel.topId,
                        "${topTabModel.deviceId}#${topTabModel.name}#${topTabModel.index}"
                )
            }
        }
    }

    private fun paraSecondMenu(file: File, topId: String, index: String) {
        Timber.i("$tag  paraSecondMenu")
        try {
            val fileList = file.listFiles()
            fileList?.forEach {
                val name = it.name.toLowerCase(Locale.ROOT)
                when {
                    name.contains("nfo") -> {
                        // 如果包含nfo，解析文件夹名字规则
                        val info = name.split("-")
                        val condition = info[4].split("&")
                        val list = ArrayList<String>()
                        list.addAll(condition)
                        val leftTabModel =
                                LeftTabModel(TYPE_NFO, info[0].toInt(), info[1], info[3], list)
                        leftTabModel.leftTopId = topId
                        leftTabModel.topNameIndexAndLeftIndex = "$index#${leftTabModel.index}"
                        leftTabModel.deviceId = deviceId
                        leftTabs.add(leftTabModel)
                    }
                    name.contains("all") -> {
                        // 如果包含all，说明是全部分类
                        val info = name.split("-")
                        val leftTabModel = LeftTabModel(TYPE_ALL, info[0].toInt(), info[1])
                        leftTabModel.leftTopId = topId
                        leftTabModel.topNameIndexAndLeftIndex = "$index#${leftTabModel.index}"
                        leftTabModel.deviceId = deviceId
                        leftTabs.add(leftTabModel)
                    }
                    name.contains("folder") -> {
                        // 如果包含folder，即获取下级目录的lists.txt文件内容
                        paraSecondListsTxt(it, topId, index)
                    }
                    else -> Unit
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun paraSecondListsTxt(file: File, topId: String, index: String) {
        val fileList = file.listFiles()
        fileList.filter { it.name.equals("lists.txt", true) }.onEach {
            Timber.i("$tag  paraSecondListsTxt  ${it.name}:${it.length()}")
            val listsStr = FileUtils.loadFromFile(it)
            listsStr?.apply {
                val split = this.split("\n")
                val names = ArrayList<String>()
//                names.addAll(split)
                split.forEach { n ->
                    names.add(n.trimStart().trimEnd())
                }
                val info = file.name.split("-")
                val leftTabModel = LeftTabModel(
                        TYPE_FOLDER, info[0].toInt(), info[1], folderMovieList = names
                )
                leftTabModel.leftTopId = topId
                leftTabModel.topNameIndexAndLeftIndex = "$index#${leftTabModel.index}"
                leftTabModel.deviceId = deviceId
                leftTabs.add(leftTabModel)
            }
        }
    }

    /**
     * movie  解析
     */
    private fun parseMovie(scope: CoroutineScope, file: File) {
//        Timber.i("$tag  parseMovie")
        if (!isActive) return
        if (file.exists() && file.isDirectory){
            file.listFiles().apply {
                filter { isDir(it) }.onEach {
                    scope.launch(dispatcher) {
                        parseMovie(this, it)
                    }
                }
                filter { !it.isDirectory && it.name.endsWith(".nfo", true) }
                        .onEach { parseNfo(it) }
            }
        }
        else{
            if (isActive) {
                launch { listener.onToFileFragment() }
                cancel()
            }
        }
    }

    private fun parseNfo(file: File) {
//        Timber.i("$tag  parseNfo ${file.name}:${file.length()}")
        val nfoStr = FileUtils.loadFromFile(file)
        nfoStr?.apply {
            // 拼装数据
            val info = MediaInfoModel()
            info.session = session
            //info.localSession = sessionLocal
            info.ip = ip
            info.deviceId = deviceId
            var nameNoExtension = file.path.substring(0, file.path.lastIndexOf("/"))
                    .replace(tarDirPath, "")
            info.path = medias[nameNoExtension] ?: "" //filterMedia(file)
//            nameNoExtension = file.path.substring(0, file.path.lastIndexOf("/"))
//                    .replace(tarDirPath,"")
            info.fanartList = fanarts[nameNoExtension] ?: arrayListOf()//filterFilesPath(file.path, FANART)
            info.posterList = posters[nameNoExtension] ?: arrayListOf()//filterFilesPath(file.path, POSTER)
            info.sampleList = samples[nameNoExtension] ?: arrayListOf()//filterFilesPath(file.path, SAMPLE)
            info.trailerList = trailers[nameNoExtension] ?: arrayListOf()//filterFilesPath(file.path, TRAILER)
//            Timber.i("$tag  parseNfo path:${info.path}")
//            Timber.i("$tag  parseNfo fanartList:${!info.fanartList.isNullOrEmpty()}")
//            Timber.i("$tag  parseNfo posterList:${!info.posterList.isNullOrEmpty()}")
            if(info.path.isNotEmpty()
                    && !info.fanartList.isNullOrEmpty()
                    && !info.posterList.isNullOrEmpty()){
                infos.add(info)
                readXmlByPull(this, info)
            }
        }
    }

    private fun isBdmv(path: String): Boolean {
        if (path.contains(".")) return false
//        Timber.i("$tag  isBdmv")
        return path.endsWith("bdmv", true)
                || path.endsWith("bdmv/", true)
                || path.endsWith("video_ts", true)
                || path.endsWith("video_ts/", true)
                || path.endsWith("bdav", true)
                || path.endsWith("bdav/", true)
                || path.endsWith("certificate", true)
                || path.endsWith("certificate/", true)
    }

    private fun isDir(file: File): Boolean {
//        Timber.i("$tag  isDir")
        val name = file.name.toLowerCase(Locale.ROOT)
        return file.isDirectory && !(name == "bdmv" || name == "video_ts" || name == "bdav" || name == "certificate")
    }

    private fun readXmlByPull(d: String, info: MediaInfoModel) {
//        Timber.i("$tag  readXmlByPull")
        var xmlData = d.replace("\ufeff", "")
        //有些标签</>没有内容，直接过滤掉不解析，try catch异常会少一点
        val listA = xmlData.split("<")
        for (errFlag in listA) {
            if (errFlag.contains("/>")) {
                xmlData =
                        if (errFlag.endsWith("\r\n")) xmlData?.replace("<$errFlag\r\n", "")
                        else xmlData?.replace("<$errFlag", "")
            }
        }
        try {

            var parser = Xml.newPullParser()
            parser.setInput(StringReader(xmlData))
            var eventType = parser.eventType
            var name = ""
            var height = ""
            var rating = ""
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    //START_TAG取值
                    XmlPullParser.START_TAG -> {
                        val parserName = parser.name
                        //有二级的标签nextText会异常，catch后跳过取值
                        try {
                            //有二级的标签过滤一下就可以避免nextText异常，无需取值的可忽略
                            //有取值的一定要加上，否则第一个取不到值，如actor不加，需要取值的name刚好在第一位会跳过取不到值
                            if (parserName != "movie"
                                    && parserName != "tvshow"
                                    && parserName != "episodedetails"
                                    && parserName != "set"
                                    && parserName != "actor"
                                    && parserName != "video"
                                    && parserName != "rating"
                            ) {
                                var value = parser.nextText()
                                when (parserName) {
                                    "title" -> info.title = value
                                    "showtitle" -> info.showTitle = value
                                    "premiered" -> info.premiered = value
                                    "year" -> info.year = value
                                    "runtime" -> info.runtime = value
                                    "country" -> {
                                        info.country?.apply {
                                            add(value)
                                        } ?: run {
                                            val country = ArrayList<String>()
                                            country.add(value)
                                            info.country = country
                                        }
                                    }
                                    "genre" -> {
                                        if (value != null) {
                                            info.genre?.apply {
                                                add(GenreUtil().getGenre(value))
                                            } ?: run {
                                                val genre = ArrayList<String>()
                                                genre.add(GenreUtil().getGenre(value))
                                                info.genre = genre
                                            }
                                        }
                                    }
                                    "director" -> info.director = value
                                    "plot" -> info.plot = value
                                    "original_filename" -> info.originalFileName = value
                                    "name" -> name = value
                                    "height" -> height = value
                                    "value" -> rating = value
                                    "id" -> if (value.isNotEmpty()) info.movieId = value
                                }
                            }
                        } catch (e: java.lang.Exception) {
//                            e.printStackTrace()
                        }
                    }
                    //值在二级标签下时START_TAG记录取值，END_TAG处理
                    XmlPullParser.END_TAG -> {
                        //catch避免某个取值异常时不继续解析xml
                        try {
                            when (parser.name) {
                                "actor" -> {
                                    if (name.isNotEmpty()) {
                                        info.actor?.apply {
                                            //暂时控制最多显示3个主演
                                            if (size < 3) {
                                                add(name)
                                            }
                                        } ?: run {
                                            val actor = ArrayList<String>()
                                            actor.add(name)
                                            info.actor = actor
                                        }
                                    }
                                }
                                "set" -> {
                                    if (name.isNotEmpty()) {
                                        info.set = name
                                    }
                                }
                                "video" -> {
                                    if (height.isNotEmpty()) {
                                        info.nfoVideo = height
                                    }
                                }
                                "rating" -> {
                                    if (rating.isNotEmpty()) {
                                        if (rating.toFloat() > info.rating) {
                                            info.rating = rating.toFloat()
                                        }
                                    }
                                }
                            }
                        } catch (e: java.lang.Exception) {
//                            e.printStackTrace()
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
//            e.printStackTrace()
        }
        //有些是set里没有二级标签<name>
        val set = FastBlur.subRangeString(xmlData, "<set>", "</set>").replace("\r\n", "")
        if (!set.contains("<")) {
            info.set = set
        }
    }

    private fun insertDB() {
        Timber.i("$tag  insertDB  topTabs:${topTabs.size}  media:${infos.size} version:${deviceInfo.updateTime}")
        //如需要更新图片，海报墙图片只需要删除即可，设备的不能直接删除，需要下载存储实现更新
        //FileUtils.deleteFolderFile("${context.filesDir?.path}/images/poster/$deviceId")
        AppDatabase.getInstance(context).getTopTabDao().delete(deviceId)
        AppDatabase.getInstance(context).getLeftTabDao().delete(deviceId)
        AppDatabase.getInstance(context).getMovieListDao().delete(deviceId)

        AppDatabase.getInstance(context).getTopTabDao().insertList(topTabs)
        AppDatabase.getInstance(context).getLeftTabDao().insertList(leftTabs)
        AppDatabase.getInstance(context).getDeviceInfoDao().insert(deviceInfo)
        AppDatabase.getInstance(context).getMovieListDao().insertList(infos)
    }

    /**
     * ============================== 查询数据库，有数据则接口回调 ====================================
     */
    private fun checkDB(scope: CoroutineScope, isScan: Boolean): Boolean {
        Timber.i("$tag  checkDB")
        val tabs =
                AppDatabase.getInstance(context).getTopWithLeftTabDao()
                        .getTopWithLeftTabsWithDeviceId(deviceId)

        val mediaInfo =
                AppDatabase.getInstance(context).getMovieListDao()
                        .getMediasWithDeviceId(deviceId)

        val deviceInfoModel =
                AppDatabase.getInstance(context).getDeviceInfoDao()
                        .getDeviceInfo(deviceId)

        if (tabs.isNotEmpty() && mediaInfo.isNotEmpty()) {
            getTabDatas(scope, tabs,deviceInfoModel,isScan)
            /*if (isActive) {
//                tabs.forEach {
//                    Timber.i("TabFragment rep ${it.topTabModel.name} ${it.topTabModel.posterData?.size ?: 0}")
//                }
                runOnUI { listener.onSuccess(tabs, deviceInfoModel, isScan) }
            }*/
            return true
        } else {
            /*if (isActive) {
                runOnUI { listener.onSuccess(tabs, deviceInfoModel, isScan) }
            }*/
            return false
        }
    }

    private fun getTabDatas(scope: CoroutineScope, tabs: List<TopWithLeftTabModel>,
                            deviceInfoModel: DeviceInfoModel?,isScan: Boolean) {
//        Timber.i("$tag  getTabDatas")
        scope.launch {
            tabs.forEachIndexed() { index,it ->
                Timber.i("$tag  getTabDatas  leftTabs:${it.leftTabs.size}")
                when (it.topTabModel.folderType) {
                    TYPE_FOLDER -> {
                        if (it.topTabModel.folderMovieList != null) {
                            val datas = ArrayList<MediaInfoModel>()
                            val temp = AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithTitle(deviceId, it.topTabModel.folderMovieList)
                            datas.addAll(temp)
                            it.topTabModel.posterData = datas
                            it.leftTabs.forEach { leftTabs ->
                                getLeftTabsDatas(leftTabs, datas)
                            }
                        }
                    }
                    TYPE_ALL -> {
                        val datas = ArrayList<MediaInfoModel>()
                        val temp = AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithDeviceId(deviceId)
                        datas.addAll(temp)
                        it.topTabModel.posterData = datas
                        it.leftTabs.forEach { leftTabs ->
                            getLeftTabsDatas(leftTabs, datas)
                        }
                    }
                    TYPE_NFO -> {
                        if (it.topTabModel.movieTypeFilter != null && it.topTabModel.movieConditionFilter != null) {
                            val datas = getNfoTypeData(
                                    it.topTabModel.movieTypeFilter,
                                    it.topTabModel.movieConditionFilter
                            )
                            it.topTabModel.posterData = datas
                            it.leftTabs.forEach { leftTabs ->
                                getLeftTabsDatas(leftTabs, datas)
                            }
                        }
                    }
                }
                /*if (index == 0){
                    if (isActive) {
                        runOnUI { listener.onSuccess(tabs, deviceInfoModel, isScan) }
                    }
                }*/
            }
            if (isActive) {
                runOnUI { listener.onSuccess(tabs,deviceInfoModel,isScan) }
            }
            if(!isScan){
                checkTarVersion(scope,deviceId, session, TAR_META_VERSION_PATH, context)
            }
        }
    }

    private fun getLeftTabsDatas(leftTab: LeftTabModel, topDatas: ArrayList<MediaInfoModel>) {
//        Timber.i("$tag  getLeftTabsDatas")
        // 不能改变初始值，否则top的poster list会变
        val tempTopDatas = ArrayList<MediaInfoModel>()
        tempTopDatas.addAll(topDatas)

        when (leftTab.folderType) {
            TYPE_FOLDER -> {
                if (leftTab.folderMovieList != null) {
                    val leftDatas = ArrayList<MediaInfoModel>()
                    val leftTemp =
                            AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithTitle(deviceId, leftTab.folderMovieList)
                    // 求交集
                    tempTopDatas.retainAll(leftTemp)
                    leftDatas.addAll(tempTopDatas)
                    leftTab.posterData = leftDatas
                }
            }
            TYPE_NFO -> {
                if (leftTab.movieTypeFilter != null && leftTab.movieConditionFilter != null) {
                    val leftDatas = ArrayList<MediaInfoModel>()
                    val leftTemp = getNfoTypeData(
                            leftTab.movieTypeFilter,
                            leftTab.movieConditionFilter
                    )
                    // 求交集
                    tempTopDatas.retainAll(leftTemp)
                    leftDatas.addAll(tempTopDatas)
                    leftTab.posterData = leftDatas
                }
            }
            TYPE_ALL -> {
                val leftDatas = ArrayList<MediaInfoModel>()
                leftDatas.addAll(tempTopDatas)
                leftTab.posterData = leftDatas
            }
        }
    }

    private fun getNfoTypeData(
            movieTypeFilter: String,
            movieConditionFilter: ArrayList<String>
    ): ArrayList<MediaInfoModel> {
//        Timber.i("$tag  getNfoTypeData")
        val result = ArrayList<MediaInfoModel>()
        when (movieTypeFilter) {
            "title" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithTitle(deviceId, movieConditionFilter)
                )
            }
            "showtitle" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithShowTitle(deviceId, movieConditionFilter)
                )
            }
            "premiered" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithPremiered(deviceId, movieConditionFilter)
                )
            }
            "year" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithYear(deviceId, movieConditionFilter)
                )
            }
            "director" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithDirector(deviceId, movieConditionFilter)
                )
            }
            "video" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithVideo(deviceId, movieConditionFilter)
                )
            }
            "plot" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithPlot(deviceId, movieConditionFilter)
                )
            }
            "original_filename" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithOriginalFilename(deviceId, movieConditionFilter)
                )
            }
            "ratings" -> {
                movieConditionFilter.forEach {
                    result.addAll(
                            AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithRating(deviceId, it.toFloat())
                    )
                }
            }
            "rating" -> {
                movieConditionFilter.forEach {
                    result.addAll(
                            AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithRating(deviceId, it.toFloat())
                    )
                }
            }
            "id" -> {
                result.addAll(
                        AppDatabase.getInstance(context).getMovieListDao()
                                .getMediasWithMovieId(deviceId, movieConditionFilter)
                )
            }
            "genre" -> {
                movieConditionFilter.forEach {
                    result.addAll(
                            AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithGenre(deviceId, it)
                    )
                }
            }
            "country" -> {
                movieConditionFilter.forEach {
                    result.addAll(
                            AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithCountry(deviceId, it)
                    )
                }
            }
            "actor" -> {
                movieConditionFilter.forEach {
                    result.addAll(
                            AppDatabase.getInstance(context).getMovieListDao()
                                    .getMediasWithActor(deviceId, it)
                    )
                }
            }
        }
        //去重
        val hashSet: LinkedHashSet<MediaInfoModel> = LinkedHashSet(result)
        return ArrayList(hashSet)
    }

    /**
     * =================================== 检查是否有最新版本 ====================================
     * 有新版本[listener].update()通知UI
     * 如需跟新调用[getTarAndParse]
     */
    private fun checkTarVersion(scope: CoroutineScope,deviceId: String, session: String, path: String, context: Context) {
        Timber.i("$tag  checkTarVersion")
        scope.launch {
            try {
                val res = getTarVersion(session, path, context)
                if (!isActive) return@launch
                res?.let { result ->
                    if (result.result){
                        if(result.data != null){
                            val deviceInfoModel = AppDatabase.getInstance(context).getDeviceInfoDao().getDeviceInfo(deviceId)
                            if(deviceInfoModel?.updateTime!!.isEmpty()
                                    || deviceInfoModel.updateTime != result.data.context){

                                tarVersion = result.data.context
                                //弹框提示跟新
                                runOnUI { listener.update() }
                            }
                        }
                    }
                    else{
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun getTarVersionStr(session: String, path: String, context: Context): String {
        Timber.i("$tag  getTarVersionStr")
        try {
            val res = getTarVersion(session, path, context)
            res?.data?.context?.let { return it }
        } catch (e: Exception) {
        }
        return "0"
    }

    private suspend fun getTarVersion(session: String, path: String, context: Context): BaseResult<TxtResult>? {
        Timber.i("$tag  getTarVersion")
        try {
            val params = HashMap<String, Any>()
            params["path"] = path
            params["cmd"] = "readtxt"
            val body = HashMap<String, Any>()
            body["method"] = "manage"
            body["session"] = session
            body["params"] = params

            return api.readTxt(body)
        } catch (e: Exception) {
        }
        return null
    }
    /**=========================================================================================*/


    fun loginCrm(_ip: String, username: String, pwd: String, pwdNew: String, listener: OnLoginCrmListener){
        launch {
            scope = this
            Timber.i("loginCrm  _ip=${_ip} username=${username} pwd=${pwd} pwdNew=${pwdNew}")
            if(apiCrm == null){
                apiCrm = RetrofitRep().initRetrofitCrm(_ip)
            }
            try {
                var resultLogin = apiCrm?.login(username, pwdNew)
                if (resultLogin != null) {
                    if(resultLogin.code == 1){
                        resultLogin.data?.token?.let {
                            Timber.i("$tag  登录成功 token:$it")
                            //
                            runOnUI { listener.onLogin(it) }
                        }
                        return@launch
                    }
                    else if(resultLogin.code == 0){
                        Timber.i("$tag 登录失败")
                        if(pwdNew != pwd){
                            //用旧密码登录，如果成功修改成新密码
                            resultLogin = apiCrm?.login(username, pwd)
                            if(resultLogin != null && resultLogin.code == 1){
                                resultLogin.data?.token?.let {
                                    val resultPwdReset = apiCrm?.passwordReset(it, pwdNew)
                                    if(resultPwdReset != null && resultPwdReset.code == 1){
                                        Timber.i("$tag  修改密码成功  pwd:${pwd}  pwdNew:${pwdNew}")
                                        updateUser(username, pwdNew)
                                    }
                                    else{
                                        Timber.i("$tag 修改密码失败")
                                    }
                                    Timber.i("$tag  token:$it")
                                    //
                                    runOnUI { listener.onLogin(it) }
                                }
                                return@launch
                            }
                        }
                        //注册
                        val resultRegister = apiCrm?.register(username, pwdNew)
                        if (resultRegister != null && resultRegister.code == 1) {
                            Timber.i("$tag 注册成功")
                            resultLogin = apiCrm?.login(username, pwdNew)
                            if(resultLogin != null && resultLogin.code == 1){
                                resultLogin.data?.token?.let {
                                    Timber.i("$tag 登录成功 token:$it")
                                    //
                                    runOnUI { listener.onLogin(it) }
                                }
                                return@launch
                            }
                        }
                        else{
                            Timber.i("$tag 注册失败")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.i("$tag  loginCrmErr  $e")
            }
            runOnUI { listener.onError() }
        }
    }
    interface OnLoginCrmListener {
        fun onLogin(token: String)
        fun onError()
    }

    private fun updateUser(account: String, password: String) {
        val dao = AppDatabase.getInstance(context).getUserDao()
        var userModel = dao.getUser(account)
        if (userModel == null) {
            userModel = UserModel(account, password, password)
        }
        if (password != userModel.pwdNew) {
            userModel.pwdNew = password
            dao.insert(userModel)
        }
    }

    fun productLists(_ip: String, token: String, listener: OnProductListsListener){
        launch {
            try {
                scope = this
                if(apiCrm == null){
                    apiCrm = RetrofitRep().initRetrofitCrm(_ip)
                }
                val result = apiCrm?.productLists(token)
                if (result != null && result.code == 1) {
                    result.data?.let { runOnUI { listener.onResponse(it) } }
                }
                else{
                    runOnUI { listener.onError() }
                }
            } catch (e: Exception) {
                Timber.i("$tag  productLists  $e")
            }
        }
    }
    interface OnProductListsListener {
        fun onResponse(list: ArrayList<ProductResult>)
        fun onError()
    }

    fun qrcode(_ip: String, token: String, listener: OnCodeListener){
        launch {
            try {
                scope = this
                if(apiCrm == null){
                    apiCrm = RetrofitRep().initRetrofitCrm(_ip)
                }
                val result = apiCrm?.qrcode(token)
                if (result != null && result.code == 1) {
                    result.data?.let { runOnUI { listener.onResponse(it) } }
                }
                else{
                    runOnUI { listener.onError() }
                }
            } catch (e: Exception) {
                Timber.i("$tag  qrcode  $e")
            }
        }
    }
    interface OnCodeListener {
        fun onResponse(data: ProductPaymentCode)
        fun onError()
    }

    fun uploadScreenshot(_ip: String, token: String, path: String, listener: OnUploadScreenshotListener){
        launch {
            try {
                scope = this
                if(apiCrm == null){
                    apiCrm = RetrofitRep().initRetrofitCrm(_ip)
                }
                val file = File(path)
                val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val result = apiCrm?.uploadScreenshot(token,body)
                if (result != null && result.code == 1) {
                    result.data?.let { runOnUI { listener.onResponse(it.filePath) } }
                }
                else{
                    runOnUI { listener.onError() }
                }
            } catch (e: Exception) {
                Timber.i("$tag  uploadScreenshot  $e")
            }
        }
    }
    interface OnUploadScreenshotListener {
        fun onResponse(path: String?)
        fun onError()
    }

    fun place(_ip: String, token: String, productId: String, payType: String, screenshot: String, listener: OnPlaceListener){
        launch {
            try {
                scope = this
                if(apiCrm == null){
                    apiCrm = RetrofitRep().initRetrofitCrm(_ip)
                }
                val result = apiCrm?.place(token,productId,payType,screenshot)
                if (result != null && result.code == 1) {
                    runOnUI { listener.onResponse() }
                }
                else{
                    runOnUI { listener.onError() }
                }
            } catch (e: Exception) {
                Timber.i("$tag  place  $e")
            }
        }
    }
    interface OnPlaceListener {
        fun onResponse()
        fun onError()
    }

    fun orders(_ip: String, token: String, listener: OnOrdersListener){
        launch {
            try {
                scope = this
                if(apiCrm == null){
                    apiCrm = RetrofitRep().initRetrofitCrm(_ip)
                }
                val result = apiCrm?.orders(token,"2")
                if (result != null && result.code == 1) {
                    Timber.i("$tag  orders  ${result.data}")
                    runOnUI { listener.onResponse(result.data) }
                }
                else{
                    runOnUI { listener.onError() }
                }
            } catch (e: Exception) {
                Timber.i("$tag  orders  $e")
            }
        }
    }
    interface OnOrdersListener {
        fun onResponse(data: ArrayList<OrderModel>?)
        fun onError()
    }

    /**
     * 网络请求跟新tar包版本
     */
    fun updateTar(session: String, listener: OnUpdateListener, context: Context){
        Timber.i("$tag  updateTar")
        launch {
            scope = this
            val res = updateTarHttp(session)
            if (!isActive) return@launch
            if(res != null){
                if (res.result){
                    runOnUI {
                        listener.onUpdate(
                                res.result,
                                1,
                                "刷新完成")
                    }
                }
                else{
                    runOnUI {
                        listener.onUpdate(
                                res.result,
                                res.error?.code ?: -1,
                                res.error?.msg ?: context.getString(R.string.fail))
                    }
                }
            }
            else {
                runOnUI {
                    listener.onUpdate(
                            false,
                            -2,
                            context.getString(R.string.fail))
                }
            }
        }
    }
    interface OnUpdateListener {
        fun onUpdate(isSuccess: Boolean, code: Int, msg: String)
    }

    private suspend fun updateTarHttp(session: String): BaseResult<FileListResult>? {
        Timber.i("$tag  updateTarHttp")
        try {
            val params = HashMap<String, Any>()
            params["cmd"] = "package"
            params["path"] = "/movie-poster"
            params["todir"] = "/movie-poster"
            params["share_path_type"] = 2
            params["des_path_type"] = 2
            params["patterns"] = arrayOf(".nfo", ".txt")
            val body = HashMap<String, Any>()
            body["method"] = "manage"
            body["session"] = session
            body["params"] = params

            return api.fileList(body)

        } catch (e: Exception) {
            Timber.i("$tag  updateTarHttp  $e")
        }
        return null
    }

    fun getFileManage(path: String, session: String, listener: OnFileManageListener) {
//        "getFileManage".log(tag)
        launch {
            scope = this
            try {
                val params = HashMap<String, Any>()
                params["path"] = path
                params["share_path_type"] = 2
                params["show_hidden"] = 0
                params["cmd"] = "attributes"
                val body = HashMap<String, Any>()
                body["method"] = "manage"
                body["session"] = session
                body["params"] = params

                val res = api.fileInfo(body)
                if (isActive) {
                    runOnUI { listener.onFileManage(res.data) }
                }

            } catch (e: Exception) {
//                e.toString().log(tag)
            }
        }
    }
    interface OnFileManageListener {
        fun onFileManage(data: FileInfoResult?)
    }

    fun destroy() {
        scope?.cancel()
    }
}