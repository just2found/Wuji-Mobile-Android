package net.linkmate.app.ui.nas.images

/**
 *
 * @Description: 选择器接口
 * @Author: todo2088
 * @CreateDate: 2021/3/4 20:52
 */
interface ISelection {
    fun selectRange(start: Int, end: Int, selected: Boolean)
    val selection: MutableSet<Int>
}