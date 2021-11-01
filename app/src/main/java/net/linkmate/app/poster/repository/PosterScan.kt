package net.linkmate.app.poster.repository

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.*
import net.linkmate.app.R
import net.linkmate.app.poster.database.AppDatabase
import net.linkmate.app.poster.model.*
import net.linkmate.app.poster.utils.*
import net.linkmate.app.poster.utils.Utils.getFileNoExtension
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.concurrent.Executors


class PosterScan : CoroutineScope by CoroutineScope(Dispatchers.IO) {

  private val tag = "cybtest"
  private lateinit var device: PosterBundleModel
  private lateinit var listener: OnScanListener
  private lateinit var listenerOnUpdate: OnUpdateListener
  private lateinit var context: Context
  private lateinit var api: Api
  private val dispatcher by lazy {
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
      .asCoroutineDispatcher()
  }

  private val nfos by lazy { Vector<MyFile>() }
  private val medias by lazy { Vector<MyFile>() }
  private val bdmvs by lazy { Vector<MyFile>() }
  private val posters by lazy { Vector<MyFile>() }
  private val fanarts by lazy { Vector<MyFile>() }
  private val samples by lazy { Vector<MyFile>() }
  private val trailers by lazy { Vector<MyFile>() }

  private val topTabs by lazy { Vector<TopTabModel>() }
  private val leftTabs by lazy { Vector<LeftTabModel>() }

  private val infos by lazy { Vector<MediaInfoModel>() }
  private var deviceInfo: DeviceInfoModel? = null

  private var nfoCount = 0
  private var parsingNfoCount = 0
  private val retryCount = 3


  interface OnScanListener {
    fun onQuerySuccess(data: List<TopWithLeftTabModel>,deviceInfoModel: DeviceInfoModel?,isScan: Boolean)
    fun onStartScan()
    fun onFileAdd(progress: Int)
    fun onFileScanSuccess()
    fun onToFileFragment()
    fun onParsingNfoStart(max: Int)
    fun onParsingNfoAdd(progress: Int)
    fun update()
  }

  interface OnUpdateListener {
    fun onUpdate(isSuccess: Boolean, code: Int, msg: String)
  }

  private suspend fun runOnUI(block: () -> Unit) = withContext(Dispatchers.Main) {
    block()
  }

  fun test(){
    val mao = HashMap<String,String>()
  }

  fun updateFile(
          _device: PosterBundleModel,
          session: String,
          ip: String,
          _listener: OnUpdateListener,
          _context: Context
  ){
    launch {
      try {
        listenerOnUpdate = _listener
        context = _context

        api = RetrofitRep().initRetrofit(ip)

        val params = HashMap<String, Any>()
        params["cmd"] = "package"
        params["path"] = "/movie-poster"
        params["share_path_type"] = 2
        params["des_path_type"] = 2
        params["todir"] = "/movie-poster"
        params["patterns"] = arrayOf(".nfo",".txt")
        val body = HashMap<String, Any>()
        body["method"] = "manage"
        body["session"] = session
        body["params"] = params
        val url = "http://$ip:9898/file"

        val res = api?.updateFile(body)

        res.let { result ->
          runOnUI {
            if (result.result){
              listenerOnUpdate.onUpdate(result.result,1, context.getString(R.string.success))
            }
            else{
              listenerOnUpdate.onUpdate(result.result,result.error?.code ?: -1, result.error?.msg ?: context.getString(R.string.fail))
            }
          }
        }

      } catch (e: Exception) {
        runOnUI { listenerOnUpdate.onUpdate(false,-1,e.toString()) }
      }
    }
  }

  fun startScan(_device: PosterBundleModel, _listener: OnScanListener, _context: Context)/* = launch*/ {
    Timber.i("onToFileFragment  startScan")
    device = _device
    listener = _listener
    context = _context
    deviceInfo = DeviceInfoModel(device.deviceId)
    if (isActive) {
      /*runOnUI { */listener.onStartScan() /*}*/
    }

    api = RetrofitRep().initRetrofit(_device.ip)

    launch {
      if (checkDB(this,false)) {
        launch {
          getMoviePoster(this,"/movie-poster")
        }.join()
        val deviceInfoModel = AppDatabase.getInstance(context).getDeviceInfoDao().getDeviceInfo(device.deviceId)
        if((deviceInfoModel != null
                && !deviceInfo?.updateTime.isNullOrEmpty()
                && deviceInfoModel.updateTime != deviceInfo?.updateTime)
                || deviceInfoModel == null){
          //弹框提示跟新
          runOnUI { listener.update() }
        }
        Timber.i("PosterScan  return@launch")
        return@launch
      }
      scan()
    }

  }

  fun scan() = launch {
    // 检查是否有movie-poster文件夹
    Timber.i("onToFileFragment  checkHasMenu()")
    if (!checkHasMenu()) {
      if (isActive) {
        /*runOnUI { */listener.onToFileFragment() /*}*/
      }
      return@launch
    }
    Timber.i("PosterScan  launch")
    launch {
      nfoCount = 0
      parsingNfoCount = 0
      nfos.clear()
      medias.clear()
      posters.clear()
      fanarts.clear()
      trailers.clear()
      samples.clear()
      bdmvs.clear()

      topTabs.clear()
      leftTabs.clear()

      getMoviePoster(this,"/movie-poster")
      getMenu(this, "/movie-poster/menu")
      getAllFile(this, "/movie-poster/movie")

    }.join()
    Timber.i("PosterScan  join")
    AppDatabase.getInstance(context).getTopTabDao().insertList(topTabs)
    AppDatabase.getInstance(context).getLeftTabDao().insertList(leftTabs)
    AppDatabase.getInstance(context).getDeviceInfoDao().insert(deviceInfo!!)
    runOnUI {
      if (isActive) {
        listener.onFileScanSuccess()
        listener.onParsingNfoStart(nfos.size)
      }
    }
    Timber.i("PosterScan  launch")
    launch {
      infos.clear()
      filterAllMovie(this)
    }.join()
    Timber.i("PosterScan  join")
    AppDatabase.getInstance(context).getMovieListDao().insertList(infos)
    Timber.i("PosterScan  checkDB")
    checkDB(this,true)

  }

  private suspend fun checkMoviePoster(): Boolean {
    val file = getFileList("/movie-poster", retryCount) ?: return false
    return file.result
  }

  private suspend fun getMoviePoster(scope: CoroutineScope, path: String) {
    Timber.i("PosterScan  getMoviePoster")
    val res = getFileList(path, retryCount)
    if(res == null){
      Timber.i("PosterScan  没有MoviePoster")
    }
    else {
      res.let { baseResult ->
        baseResult.result.apply {
          if(this){
            baseResult.data?.files?.forEach {
              val name = getFileNoExtension(it.name)
              Timber.i("PosterScan  name:${name}")
              if (name.equals("movie-poster-bg", true)) {
                deviceInfo?.movie_poster_bg = it.path
              } else if (name.equals("movie-poster-cover", true)) {
                deviceInfo?.movie_poster_cover = it.path
              } else if (name.equals("movie-poster-logo", true)) {
                deviceInfo?.movie_poster_logo = it.path
              } else if (name.equals("movie-poster-wall", true)) {
                deviceInfo?.movie_poster_wall = it.path
              } else if (name.equals("movie-poster-in", true)) {
                // 读取movie_poster_in文件
                scope.launch(dispatcher) {
                  paraInTxt(it.path,retryCount)
                }.join()
              }
            }
          }
          else {
            Timber.i("PosterScan  MoviePoster没有获取到文件")
          }
        }
      }
      Timber.i("PosterScan  结束")
    }
  }

  private suspend fun paraInTxt(path: String, count: Int) {
    try {
      Timber.i("PosterScan  paraInTxt")
      val params = HashMap<String, Any>()
      params["path"] = path
      params["cmd"] = "readtxt"
      val body = HashMap<String, Any>()
      body["method"] = "manage"
      body["session"] = device.session
      body["params"] = params

      api?.readTxt(body)?.apply {
        if (result) {
          data?.context?.apply {
            val list = this.split("\r\n")
            if (list.size >= 4) {
              deviceInfo?.name = list[0]
              deviceInfo?.plot = list[1]
              deviceInfo?.updateTime = list[2]
              deviceInfo?.type = list[3]
              //currentGetDataDevice?.newNumber = list[4]
              Timber.i("PosterScan  paraInTxt----成功")
            }
          }
        }
        else{
          Timber.i("PosterScan  paraInTxt----失败")
        }
      }
    } catch (e: Exception) {
      if (e is IOException) {
        var _count = count
        if (_count > 0) {
          _count--
          paraInTxt(path, _count)
        }
      }
    }
  }

  private suspend fun checkHasMenu(): Boolean {
    val file = getFileList("/movie-poster/menu", retryCount) ?: return false
    return file.result
  }

  private suspend fun checkDB(scope: CoroutineScope,isScan: Boolean): Boolean {
    val tabs =
      AppDatabase.getInstance(context).getTopWithLeftTabDao()
        .getTopWithLeftTabsWithDeviceId(device.deviceId)
      val mediaInfos =
      AppDatabase.getInstance(context).getMovieListDao()
        .getMediasWithDeviceId(device.deviceId)
    val deviceInfoModel = AppDatabase.getInstance(context).getDeviceInfoDao().getDeviceInfo(device.deviceId)

    if (tabs.isNotEmpty() && mediaInfos.isNotEmpty()) {
      scope.launch {
        getTabDatas(this, tabs)
      }.join()
      if (isActive) {
        runOnUI { listener.onQuerySuccess(tabs,deviceInfoModel,isScan) }
      }
      return true
    } else {
      return false
    }
  }

  private fun getTabDatas(scope: CoroutineScope, tabs: List<TopWithLeftTabModel>) {
    tabs.forEach {
      scope.launch(dispatcher) {
        when (it.topTabModel.folderType) {
          TYPE_FOLDER -> {
            if (it.topTabModel.folderMovieList != null) {
              val datas = ArrayList<MediaInfoModel>()
              val temp = AppDatabase.getInstance(context).getMovieListDao()
                .getMediasWithTitle(device.deviceId,it.topTabModel.folderMovieList)
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
              .getMediasWithDeviceId(device.deviceId)
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
      }
    }
  }

  private fun getLeftTabsDatas(leftTab: LeftTabModel, topDatas: ArrayList<MediaInfoModel>) {
    // 不能改变初始值，否则top的poster list会变
    val tempTopDatas = ArrayList<MediaInfoModel>()
    tempTopDatas.addAll(topDatas)

    when (leftTab.folderType) {
      TYPE_FOLDER -> {
        if (leftTab.folderMovieList != null) {
          val leftDatas = ArrayList<MediaInfoModel>()
          val leftTemp =
            AppDatabase.getInstance(context).getMovieListDao()
              .getMediasWithTitle(device.deviceId,leftTab.folderMovieList)
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
    val result = ArrayList<MediaInfoModel>()
    when (movieTypeFilter) {
      "title" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithTitle(device.deviceId,movieConditionFilter)
        )
      }
      "showtitle" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithShowTitle(device.deviceId,movieConditionFilter)
        )
      }
      "premiered" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithPremiered(device.deviceId,movieConditionFilter)
        )
      }
      "year" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithYear(device.deviceId,movieConditionFilter)
        )
      }
      "director" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithDirector(device.deviceId,movieConditionFilter)
        )
      }
      "video" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithVideo(device.deviceId,movieConditionFilter)
        )
      }
      "plot" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithPlot(device.deviceId,movieConditionFilter)
        )
      }
      "original_filename" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithOriginalFilename(device.deviceId,movieConditionFilter)
        )
      }
      "ratings" -> {
        movieConditionFilter.forEach {
          result.addAll(
            AppDatabase.getInstance(context).getMovieListDao()
              .getMediasWithRating(device.deviceId,it.toFloat())
          )
        }
      }
      "rating" -> {
        movieConditionFilter.forEach {
          result.addAll(
            AppDatabase.getInstance(context).getMovieListDao()
              .getMediasWithRating(device.deviceId,it.toFloat())
          )
        }
      }
      "id" -> {
        result.addAll(
          AppDatabase.getInstance(context).getMovieListDao()
            .getMediasWithMovieId(device.deviceId,movieConditionFilter)
        )
      }
      "genre" -> {
        movieConditionFilter.forEach {
          result.addAll(
            AppDatabase.getInstance(context).getMovieListDao()
              .getMediasWithGenre(device.deviceId,it)
          )
        }
      }
      "country" -> {
        movieConditionFilter.forEach {
          result.addAll(
            AppDatabase.getInstance(context).getMovieListDao()
              .getMediasWithCountry(device.deviceId,it)
          )
        }
      }
      "actor" -> {
        movieConditionFilter.forEach {
          result.addAll(
            AppDatabase.getInstance(context).getMovieListDao()
              .getMediasWithActor(device.deviceId,it)
          )
        }
      }
    }
    //去重
    val hashSet: LinkedHashSet<MediaInfoModel> = LinkedHashSet(result)
    return ArrayList(hashSet)
  }

  private suspend fun getMenu(scope: CoroutineScope, path: String) {
    Timber.i("PosterScan  getMenu")
    getFileList(path, retryCount)?.let { baseResult ->
      baseResult.result.apply {
        baseResult.data?.files?.forEach {
          // 解析第一层
          scope.launch(dispatcher) {
            paraMenu(it)
          }
        }
      }
    }
  }

  private suspend fun paraMenu(file: MyFile) {
    try {
      Timber.i("PosterScan  paraMenu")
      val name = file.name.toLowerCase(Locale.ROOT)
      when {
        name.contains("nfo") -> {
          // 如果包含nfo，解析文件夹名字规则
          val info = name.split("-")
          val condition = info[4].split("&")
          val list = ArrayList<String>()
          list.addAll(condition)
          val topTabModel = TopTabModel(TYPE_NFO, info[0].toInt(), info[1], info[3], list)
          topTabModel.topId = "${topTabModel.name}--${topTabModel.index}"
          topTabModel.deviceId = device.deviceId
          topTabs.add(topTabModel)
          paraSecondMenu(
            file.path,
            topTabModel.topId,
            "${topTabModel.deviceId}#${topTabModel.name}#${topTabModel.index}"
          )
        }
        name.contains("all") -> {
          // 如果包含all，说明是全部分类
          val info = name.split("-")
          val topTabModel = TopTabModel(TYPE_ALL, info[0].toInt(), info[1])
          topTabModel.topId = "${topTabModel.name}--${topTabModel.index}"
          topTabModel.deviceId = device.deviceId
          topTabs.add(topTabModel)
          paraSecondMenu(
            file.path,
            topTabModel.topId,
            "${topTabModel.deviceId}#${topTabModel.name}#${topTabModel.index}"
          )
        }
        name.contains("folder") -> {
          // 如果包含folder，即获取下级目录的lists.txt文件内容
          paraListsTxt(file, retryCount)
        }
        else -> Unit
      }

    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private suspend fun paraListsTxt(file: MyFile, count: Int) {
    try {
      val params = HashMap<String, Any>()
      params["path"] = "${file.path}/lists.txt"
      params["cmd"] = "readtxt"
      val body = HashMap<String, Any>()
      body["method"] = "manage"
      body["session"] = device.session
      body["params"] = params

      api?.readTxt(body)?.apply {
        if (result) {
          data?.context?.apply {
            val split = this.split("\r\n")
            val names = ArrayList<String>()
            names.addAll(split)
            val info = file.name.split("-")
            val topTabModel = TopTabModel(
              TYPE_FOLDER, info[0].toInt(), info[1], folderMovieList = names
            )
            topTabModel.topId = "${topTabModel.name}--${topTabModel.index}"
            topTabModel.deviceId = device.deviceId
            topTabs.add(topTabModel)
            paraSecondMenu(
              file.path,
              topTabModel.topId,
              "${topTabModel.deviceId}#${topTabModel.name}#${topTabModel.index}"
            )
          }
        }
      }
    } catch (e: Exception) {
      if (e is IOException) {
        var _count = count
        if (_count > 0) {
          _count--
          paraListsTxt(file, _count)
        }
      }
    }
  }

  private suspend fun paraSecondMenu(path: String, topId: String, index: String) {
    try {
      val datas = getFileList(path, retryCount)
      datas?.data?.files?.forEach { file ->
        val name = file.name.toLowerCase(Locale.ROOT)
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
            leftTabs.add(leftTabModel)
          }
          name.contains("all") -> {
            // 如果包含all，说明是全部分类
            val info = name.split("-")
            val leftTabModel = LeftTabModel(TYPE_ALL, info[0].toInt(), info[1])
            leftTabModel.leftTopId = topId
            leftTabModel.topNameIndexAndLeftIndex = "$index#${leftTabModel.index}"
            leftTabs.add(leftTabModel)
          }
          name.contains("folder") -> {
            // 如果包含folder，即获取下级目录的lists.txt文件内容
            paraSecondListsTxt(file, topId, index, retryCount)
          }
          else -> Unit
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private suspend fun paraSecondListsTxt(file: MyFile, topId: String, index: String, count: Int) {
    try {

      val params = HashMap<String, Any>()
      params["path"] = "${file.path}/lists.txt"
      params["cmd"] = "readtxt"
      val body = HashMap<String, Any>()
      body["method"] = "manage"
      body["session"] = device.session
      body["params"] = params

      api?.readTxt(body)?.apply {
        if (result) {
          data?.context?.apply {
            val split = this.split("\n")
            val names = ArrayList<String>()
            names.addAll(split)
            val info = file.name.split("-")
            val leftTabModel = LeftTabModel(
              TYPE_FOLDER, info[0].toInt(), info[1], folderMovieList = names
            )
            leftTabModel.leftTopId = topId
            leftTabModel.topNameIndexAndLeftIndex = "$index#${leftTabModel.index}"
            leftTabs.add(leftTabModel)
          }
        }
      }
    } catch (e: Exception) {
      if (e is IOException) {
        var _count = count
        if (_count > 0) {
          _count--
          paraSecondListsTxt(file, topId, index, _count)
        }
      }
    }
  }

  private suspend fun filterAllMovie(scope: CoroutineScope) {
    Timber.i("PosterScan  filterAllMovie")
    nfos.forEach {
      scope.launch(dispatcher) {
        readNfo(it, retryCount)
        parsingNfoAdd()
      }
    }
  }

  private suspend fun readNfo(file: MyFile, count: Int) {
    try {
      val params = HashMap<String, Any>()
      params["path"] = file.path
      params["cmd"] = "readtxt"
      val body = HashMap<String, Any>()
      body["method"] = "manage"
      body["session"] = device.session
      body["params"] = params

      api.readTxt(body).apply {
        if (result) {
          data?.context?.apply {
            // 拼装数据
            val info = MediaInfoModel()
            info.session = device.session
            info.localSession = device.sessionLocal
            info.ip = device.ip
            info.deviceId = device.deviceId
            info.path = filterMedia(file)
            info.fanartList = filterFanart(file)
            info.posterList = filterPoster(file)
            info.sampleList = filterSample(file)
            info.trailerList = filterTrailer(file)
            infos.add(info)
            readXmlByPull(this, info)
          }
        }
      }
    } catch (e: Exception) {
      if (e is IOException) {
        var _count = count
        if (_count > 0) {
          _count--
          readNfo(file, _count)
        }
      }
    }
  }

  private fun filterMedia(file: MyFile): String {
    return medias.filter {
      it.path.contains(
        file.path.substring(
          0, file.path.lastIndexOf(".")
        )
      )
    }.map {
      when (it.ftype) {
        "dir" -> {
          return@map it.path.substring(0, it.path.lastIndexOf("/"))
        }
        else -> {
          return@map it.path
        }
      }
    }.toList()[0]
  }

  private fun filterFanart(file: MyFile): ArrayList<String> {
    val list = ArrayList<String>()
    val fanart = fanarts.filter {
      it.path.contains(file.path.substring(0, file.path.lastIndexOf(".")))
    }.map {
      return@map it.path
    }.toList()
    list.addAll(fanart)
    return list
  }

  private fun filterPoster(file: MyFile): ArrayList<String> {
    val list = ArrayList<String>()
    val poster = posters.filter {
      it.path.contains(
        file.path.substring(
          0, file.path.lastIndexOf(".")
        )
      )
    }.map {
      return@map it.path
    }.toList()
    list.addAll(poster)
    return list
  }

  private fun filterSample(file: MyFile): ArrayList<String> {
    val list = ArrayList<String>()
    val sample = samples.filter {
      it.path.contains(
        file.path.substring(
          0, file.path.lastIndexOf(".")
        )
      )
    }.map {
      return@map it.path
    }.toList()
    list.addAll(sample)
    return list
  }

  private fun filterTrailer(file: MyFile): ArrayList<String> {
    val list = ArrayList<String>()
    val trailer = trailers.filter {
      it.path.contains(
        file.path.substring(
          0, file.path.lastIndexOf(".")
        )
      )
    }.map {
      return@map it.path
    }.toList()
    list.addAll(trailer)
    return list
  }

  private suspend fun getAllFile(scope: CoroutineScope, path: String) {
    Timber.i("PosterScan  paraMenu")
    getFileList(path, retryCount)?.let { baseResult ->
      baseResult.result.apply {
        baseResult.data?.files?.apply {
          filter { isDir(it) }.onEach {
            scope.launch(dispatcher) {
              getAllFile(this, it.path)
            }
          }
          filter { isBdmv(it) }.apply {
            bdmvs.addAll(this)
          }
          filter { isNfo(it) }.apply {
            nfos.addAll(this)
            nfoAdd(this.size)
          }
          filter { isFanart(it) }.apply {
            fanarts.addAll(this)
          }
          filter { isPoster(it) }.apply {
            posters.addAll(this)
          }
          filter { isAudiod(it) }.apply {
            medias.addAll(this)
          }
          filter { isVideod(it) }.apply {
            medias.addAll(this)
          }
          filter { isSample(it) }.apply {
            samples.addAll(this)
          }
          filter { isTrailer(it) }.apply {
            trailers.addAll(this)
          }
        }
      }
    }
  }

  @Synchronized
  private suspend fun nfoAdd(size: Int) {
    if (isActive) {
      nfoCount += size
      runOnUI { listener.onFileAdd(nfoCount) }
    }
  }

  @Synchronized
  private suspend fun parsingNfoAdd() {
    Timber.i("PosterScan  parsingNfoAdd")
    if (isActive) {
      parsingNfoCount++
      runOnUI { listener.onParsingNfoAdd(parsingNfoCount) }
    }
  }


  private fun isBdmv(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type == "dir" && (name == "bdmv" || name == "video_ts" || name == "bdav" || name == "certificate")
  }

  private fun isDir(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type == "dir" && !(name == "bdmv" || name == "video_ts" || name == "bdav" || name == "certificate")
  }

  private fun isNfo(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type != "dir" && name.endsWith(".nfo")
  }

  private fun isPoster(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type != "dir" && name.contains("poster") && isImage(name)
  }

  private fun isFanart(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type != "dir" && name.contains("fanart") && isImage(name)
  }

  private fun isAudiod(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type != "dir" && isAudio(name)
  }

  private fun isVideod(file: MyFile): Boolean {
    val type = file.ftype.toLowerCase(Locale.ROOT)
    val name = file.name.toLowerCase(Locale.ROOT)
    return type != "dir" && isVideo(name)
  }

  private fun isSample(file: MyFile): Boolean {
    return isVideod(file) && file.name.contains("sample", true)
  }

  private fun isTrailer(file: MyFile): Boolean {
    return isVideod(file) && file.name.contains("trailer", true)
  }

  private suspend fun getFileList(path: String, count: Int): BaseResult<FileListResult>? {
    try {
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
      body["session"] = device.session
      body["params"] = params

      return api?.fileList(body)

    } catch (e: Exception) {
      if (e is IOException) {
        var _count = count
        if (_count > 0) {
          _count--
          return getFileList(path, _count)
        }
      }
    }
    return null
  }

  private fun readXmlByPull(d: String, info: MediaInfoModel) {

    var xmlData = d.replace("\ufeff", "")//if (d.startsWith("\ufeff")) d.substring(1) else d
//        val xmlData = deleteStringBom(d)
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

  fun destroy() {
    cancel()
  }

}