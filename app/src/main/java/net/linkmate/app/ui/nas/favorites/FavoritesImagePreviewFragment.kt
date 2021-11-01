package net.linkmate.app.ui.nas.favorites

import androidx.fragment.app.viewModels
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.images.BaseImagePreviewFragment
import net.linkmate.app.ui.nas.images.IPhotosViewModel
import net.sdvn.nascommon.model.oneos.DataFile


/**
 *
 * @Description: 图片搜索预览
 * @Author: todo2088
 * @CreateDate: 2021/2/5 22:00
 */
class FavoritesImagePreviewFragment : BaseImagePreviewFragment() {
    private val fileSearchViewModel by viewModels<FileFavoritesViewModel>({ requireParentFragment() }, { NasAndroidViewModel.ViewModeFactory(requireActivity().application, getDevId()) })
    override fun getPhotosViewModel(): IPhotosViewModel<out DataFile> {
        return fileSearchViewModel
    }

    override fun getCurrentPosition(): Int {
        return navArgs.position
    }
    override fun getTotal(): Int {
        return  fileSearchViewModel.getPagesPicModel().files.count()
    }
}