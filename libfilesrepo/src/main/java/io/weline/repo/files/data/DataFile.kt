package io.weline.repo.files.data

interface DataFile {
    fun getPath(): String
    fun getName(): String
    fun getSize(): Long
    fun isDirectory(): Boolean
    fun isPublicFile(): Boolean
    fun getTime(): Long
}