package net.sdvn.nascommon.model.oneos

interface DataFile {
    fun getPath(): String
    fun getName(): String
    fun getSize(): Long
    fun isDirectory(): Boolean
    fun isPublicFile(): Boolean
    fun getTime(): Long
    fun getTag(): String {
        return getPath() + getTime()
    }

    fun getPathType(): Int {
        return -1
    }

    fun fileIsOnlyRead(): Boolean {
        return false
    }

    fun getMD5(): String? = null
}