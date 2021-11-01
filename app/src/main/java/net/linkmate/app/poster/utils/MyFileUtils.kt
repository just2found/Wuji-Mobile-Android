package net.linkmate.app.poster.utils

import java.io.File
import java.util.*



// 视频后缀扩展
private val VIDEO_EXTENSIONS = arrayOf(
  "mp4",
  "mkv",
  "mov",
  "avi",
  "rm",
  "rmvb",
  "3gp",
  "flv",
  "ts",
  "m4v",
  "wmv",
  "asf",
  "m2ts",
  "vob",
  "3g2",
  "3gp2",
  "3gpp",
  "amv",
  "divx",
  "drc",
  "dv",
  "f4v",
  "gvi",
  "gxf",
  "m1v",
  "m2v",
  "m2t",
  "m3u8",
  "mp2",
  "mp2v",
  "mp4v",
  "mpe",
  "mpeg",
  "mpeg1",
  "mpeg2",
  "mpeg4",
  "mpg",
  "mpv2",
  "mts",
  "mtv",
  "mxf",
  "mxg",
  "nsv",
  "nuv",
  "ogm",
  "ogv",
  "ogx",
  "ps",
  "rec",
  "tod",
  "tts",
  "vro",
  "webm",
  "wm",
  "wtv",
  "xesc",
  "evo",
  "vidon",
  "tp",
  "dat",
  "mk3d",
  "iso"
)

private val AUDIO_EXTENSIONS = arrayOf(
  "3ga",
  "a52",
  "aac",
  "ac3",
  "adt",
  "adts",
  "aif",
  "aifc",
  "aiff",
  "aob",
  "ape",
  "awb",
  "caf",
  "dts",
  "flac",
  "it",
  "m4a",
  "m4p",
  "mid",
  "mka",
  "mlp",
  "mod",
  "mpa",
  "mp1",
  "mp2",
  "mp3",
  "mpc",
  "mpga",
  "oga",
  "ogg",
  "oma",
  "opus",
  "ra",
  "ram",
  "rmi",
  "s3m",
  "spx",
  "tta",
  "voc",
  "vqf",
  "w64",
  "wav",
  "wma",
  "wv",
  "xa",
  "xm",
  "dsd",
  "dsf",
  "dff",
  "cue",
  "m3u"
)
// 字幕后缀扩展
val IOS_EXTENSIONS = arrayOf("iso", "bdmv", "ifo", "bdav", "dvdvr", "bdm", "vso")
private val PIC_EXTENSIONS = arrayOf("png", "bmp", "jpg", "tiff", "tif", "gif", "tga")

/**
 * 判断是不是视频文件
 */
fun isVideo(fileName: String): Boolean {
  try {
    val name = getFileExtension(fileName)
    return VIDEO_EXTENSIONS.contains(name)
  } catch (e: Exception) {
  }
  return false
}

fun isImage(fileName: String): Boolean {
  try {
    getFileExtension(fileName)?.apply {
      return PIC_EXTENSIONS.contains(this)
    }
  } catch (e: Exception) {
  }
  return false
}

fun isAudio(fileName: String): Boolean {
  try {
    getFileExtension(fileName)?.apply {
      return AUDIO_EXTENSIONS.contains(this)
    }
  } catch (e: Exception) {
  }
  return false
}

/**
 * Folder视频文件判断
 */
fun isFolder(path: String): String? {
  val folderBdmv = File(path, "/BDMV/index.bdmv")
  if (folderBdmv.exists()) {
    return folderBdmv.absolutePath
  }
  val folderIfo = File(path, "/VIDEO_TS/VIDEO_TS.IFO")
  if (folderIfo.exists()) {
    return folderIfo.absolutePath
  }
  val folderBdav = File(path, "/BDAV/info.bdav")
  if (folderBdav.exists()) {
    return folderBdav.absolutePath
  }
  val folderBdm = File(path, "/BDMV/index.bdm")
  if (folderBdm.exists()) {
    return folderBdm.absolutePath
  }
  return null
}

fun isMkuVideo(fileName: String): Boolean {
  try {
    val name = getFileExtension(fileName)
    return "vidon" == name
  } catch (e: Exception) {
  }
  return false
}


/**
 * 判断是不是字幕文件
 */
fun isISOVideo(fileName: String): Boolean {
  try {
    val name = getFileExtension(fileName)
    return IOS_EXTENSIONS.contains(name)
  } catch (e: Exception) {
  }

  return false
}

/**
 * 获取文件的后缀扩展名
 */
fun getFileExtension(fileName: String): String? {
  try {
    val i = fileName.lastIndexOf(".")
    if (i > 0 && i < fileName.length - 1) {
      return fileName.substring(i + 1).toLowerCase(Locale.getDefault())
    }
  } catch (e: Exception) {
  }
  return null
}