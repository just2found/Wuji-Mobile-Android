package net.linkmate.app.ui.nas.images

import net.sdvn.nascommon.model.oneos.OneOSFile

data class OneFileModel(val viewType: Int, val id: Long,
                        var ext1: String, var ext: String) : OneOSFile() {

}

class OneFilePagesModel<T> {
    var mSectionLetters = ArrayList<String>()
    var index = 0
    var files: MutableList<T> = mutableListOf()
    var total = 0
    var page = 0
        set(value) {
            if (value == 0) {
                index = 0
                files.clear()
                mSectionLetters.clear()
            }
            field = value
        }
    var pages = 0
    fun hasMorePage(): Boolean {
        return pages - 1 > page
    }

    fun nextPage(): Int {
        return page + 1
    }
}
