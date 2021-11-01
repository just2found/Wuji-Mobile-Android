package net.linkmate.app.ui.function_ui.choicefile.base


/**
create by: 86136
create time: 2021/4/23 10:38
Function description: 用于过滤筛选类型
 */

enum class OneOSFilterType(val numberCode: Int) {
    ALL(0),
    DIR(1),
    BT(2),
    PICTURE(3),
    VIDEO(4),
    AUDIO(5);



    /**
    all	所有类型文件（默认）
    dir	目录文件
    pic	图片文件
    video	视频文件
    audio	音频文件
    txt	文本文件
    doc	文档文件
    pdf	PDF文件
    xls	Excel文件
    ppt	PPT文件
    zip	压缩包文件
    bt	种子文件
     */
    fun getFilterList(): ArrayList<String> {
        val list = ArrayList<String>()
        when (this) {
            BT -> {
                list.add("dir")
                list.add("bt")
            }
            PICTURE -> {
                list.add("dir")
                list.add("pic")
            }
            VIDEO -> {
                list.add("dir")
                list.add("video")
            }
            AUDIO -> {
                list.add("dir")
                list.add("audio")
            }
            DIR -> {
                list.add("dir")
            }
            ALL -> {
                list.add("all")
            }
        }
        return list
    }
}