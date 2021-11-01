package io.weline.repo.api

import io.weline.repo.files.data.SharePathType

/** 

Created by admin on 2020/10/26,18:59

 */
object DefSysInfo {
    // 快传跟踪服务器
    const val dir_fast_transfer = "/fast_transfer/"
    const val key_fast_transfer_ts = "fast_transfer_ts"
    const val level_fast_transfer_ts = 3

    //简介
    const val dir_introduction = "/introduction/"
    val pathType:Int = SharePathType.GLOBAL.type

    //设备简介
    const val dir_introduction_dev = "${dir_introduction}dev/"
    const val key_introduction_dev = "introduction_dev"
    const val key_introduction_dev_icon_name = "icon.jpg"
    const val key_introduction_dev_bg_name = "bg.jpg"
    const val level_introduction_dev = 3
    //圈子简介
    const val dir_introduction_circle = "${dir_introduction}circle/"
    const val key_introduction_circle = "introduction_circle"
    const val key_introduction_circle_icon_name = "icon.jpg"
    const val level_introduction_circle = 3

}