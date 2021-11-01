package net.linkmate.app.ui.simplestyle.device.disk.data

import com.google.gson.annotations.SerializedName

//"op":"create","mode":"raid0"
//BaseProtocol(result=true, error=null, data=[{op=create, mode=lvm, deivces=null}, {op=create, mode=raid0, deivces=null}, {op=create, mode=raid1, deivces=null}, {op=create, mode=raid5, deivces=null}, {op=extend, mode=lvm, deivces=[/dev/sdb, /dev/sdc]}])
//[{"op":"create","mode":"lvm"},{"op":"create","mode":"raid0"},{"op":"create","mode":"raid1"},{"op":"create","mode":"raid5"},{"op":"extend","mode":"lvm","deivces":["/dev/sdb","/dev/sdc"]}]
data class ActionItem(@field:SerializedName("op") val op: String,
                      @field:SerializedName("mode") val mode: String,
                      @field:SerializedName("deivces") val deivces : List<String>)
