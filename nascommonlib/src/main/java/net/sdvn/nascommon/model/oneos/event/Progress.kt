package net.sdvn.nascommon.model.oneos.event

import androidx.annotation.Keep

@Keep
class Progress<T> : Event() {

    /**
     * content : {"path":"public/TRDOWNLOAD/已完成/冒险类/[加勒比海盗5：死无对证/[加勒比海盗5：死无对证].Pirates.of.the.Caribbean.Dead.Men.Tell.No.Tales.2017.UHD.BluRay.2160p.x265-10bit.AC3.3Audios-CMCT.mkv","progress":0,"topath":"/[加勒比海盗5：死无对证].Pirates.of.the.Caribbean.Dead.Men.Tell.No.Tales.2017.UHD.BluRay.2160p.x265-10bit.AC3.3Audios-CMCT.mkv"}
     */
    var content: T? = null

}
