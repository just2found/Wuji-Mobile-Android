package io.weline.mediaplayer.internal.util

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.weline.mediaplayer.R

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/4/26
 */

object AndroidUtils {
    fun open(context: Context, uri: Uri, type: String) {
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = Intent.ACTION_VIEW
            if (uri.scheme == ContentResolver.SCHEME_CONTENT &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.setDataAndType(uri, type)
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.open))
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

}