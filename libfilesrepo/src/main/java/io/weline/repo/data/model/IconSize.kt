package io.weline.repo.data.model

/**
 *
 * @Description: IconSize 图片size
 * @Author: todo2088
 * @CreateDate: 2021/2/20 22:10
 */
data class IconSize(var width: Int, var height: Int) {
    fun getSize(): String {
        return "${width}x$height"
    }

    companion object {
        const val Max = "max"
        const val Mid = "mid"
        const val Min = "min"
    }
}