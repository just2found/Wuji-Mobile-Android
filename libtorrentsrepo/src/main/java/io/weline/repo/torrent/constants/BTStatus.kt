package io.weline.repo.torrent.constants

/** 

Created by admin on 2020/7/3,16:20

 */
//0 进行中，1结束，2 取消或者不存在，3 错误
object BTStatus {
    const val INIT = -1
    const val DOWNLOADING = 0
    const val STOPPED = 1
    const val COMPLETE = 2
    const val ERROR = 3
}
