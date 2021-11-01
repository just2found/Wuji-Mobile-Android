package io.weline.repo.files

import io.weline.repo.files.data.OneOSFileType
import io.weline.repo.files.data.SharePathType

object NasCompat {
    @JvmStatic
    fun getSharePathType(type: OneOSFileType): SharePathType {
        return when (type) {
            OneOSFileType.PUBLIC -> SharePathType.PUBLIC
            OneOSFileType.PRIVATE -> SharePathType.USER
            OneOSFileType.RECYCLE -> SharePathType.USER
            OneOSFileType.AUDIO -> SharePathType.USER
            OneOSFileType.VIDEO -> SharePathType.USER
            OneOSFileType.DOC -> SharePathType.USER
            OneOSFileType.PICTURE -> SharePathType.USER
            else -> {
                SharePathType.VIRTUAL
            }
        }
    }
}