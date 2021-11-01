package net.linkmate.app.ui.nas.helper

import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.comp.*
import java.util.*

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/5/13
 */
object FileSortHelper {

    fun sortWith(mFileType1: OneOSFileType, mOrderType: FileOrderTypeV2, oneOSFiles: List<OneOSFile>) {
        when {
            mFileType1 == OneOSFileType.PICTURE -> Collections.sort(oneOSFiles, OneOSFileCTTimeComparator())
            mOrderType == FileOrderTypeV2.name_asc -> Collections.sort(oneOSFiles, OneOSFileNameComparatorV2(true))
            mOrderType == FileOrderTypeV2.name_desc -> Collections.sort(oneOSFiles, OneOSFileNameComparatorV2(false))
            mOrderType == FileOrderTypeV2.size_asc -> Collections.sort(oneOSFiles, OneOSFileSizeComparatorV2(true))
            mOrderType == FileOrderTypeV2.size_desc -> Collections.sort(oneOSFiles, OneOSFileSizeComparatorV2(false))
            mOrderType == FileOrderTypeV2.time_asc -> Collections.sort(oneOSFiles, OneOSFileTimeComparatorV2(true))
            mOrderType == FileOrderTypeV2.time_desc -> Collections.sort(oneOSFiles, OneOSFileTimeComparatorV2(false))
            else -> Collections.sort(oneOSFiles, OneOSFileTimeComparator())
        }
    }
}