package net.linkmate.app.ui.nas.images

import androidx.fragment.app.viewModels
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.sdvn.nascommon.model.oneos.DataFile


/**
 *
 * @Description: 图片预览
 * @Author: todo2088
 * @CreateDate: 2021/2/5 22:00
 */
class ImagePreviewFragment : BaseImagePreviewFragment() {
    private val photosViewModel by viewModels<PhotosViewModel>({ requireParentFragment() }, { NasAndroidViewModel.ViewModeFactory(requireActivity().application, getDevId()) })

    override fun getPhotosViewModel(): IPhotosViewModel<out DataFile> {
        return photosViewModel
    }

    override fun getCurrentPosition(): Int {
        return navArgs.position
    }
}