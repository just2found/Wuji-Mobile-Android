package net.linkmate.app.ui.nas.cloud

import androidx.fragment.app.viewModels
import net.sdvn.nascommon.viewmodel.FilesViewModel


class VP3QuickCloudNavFragment :VP2QuickCloudNavFragment(){
    override val mFilesViewModel by viewModels<FilesViewModel>()
}