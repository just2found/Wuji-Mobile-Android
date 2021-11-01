package libs.source.common.exts

import android.content.res.Resources
import kotlin.math.roundToInt

fun Resources.dpToPx(dp: Int): Int {
    return ((dp * displayMetrics.density + 0.5).roundToInt())
}