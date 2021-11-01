package net.sdvn.nascommon.model.oneos.event

/* {
"action": "copy",
"channel": "file",
"content": {
    "count": 0,
    "cur_length": 0,
    "cur_name": "",
    "cur_size": 0,
    "id": "31905f17a966e0894b5e444350a31e17",
    "length": 0,
    "size": 0,
    "src_path": "/IMG_0062.mp4",
    "state": 0,
    "to_path": "/TimeMachine/",
    "total": 0
},
"user": "18175192545"
}*/
class ContentCopy {

    /**
     * count : 0
     * cur_length : 0
     * cur_name :
     * cur_size : 0
     * id : 31905f17a966e0894b5e444350a31e17
     * length : 0
     * size : 0
     * src_path : /IMG_0062.mp4
     * state : 0
     * to_path : /TimeMachine/
     * total : 0
     */

    var count: Int = 0
    var cur_length: Int = 0
    var cur_name: String? = null
    var cur_size: Int = 0
    var id: String? = null
    var length: Int = 0
    var size: Int = 0
    var path: String? = null
    var state: Int = 0
    var topath: String? = null
    var total: Int = 0
}