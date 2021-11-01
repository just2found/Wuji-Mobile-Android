package net.linkmate.app.ui.activity.nasApp.deviceDetial

import androidx.lifecycle.ViewModel
import net.linkmate.app.base.MyApplication

/**拓展函数
 * @author Raleigh.Luo
 * describe：
 */

/********ViewModel************************************************************************/

fun ViewModel.getString(id: Int): String {
    return MyApplication.getInstance().resources.getString(id)
}