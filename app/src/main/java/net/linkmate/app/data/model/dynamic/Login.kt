package net.linkmate.app.data.model.dynamic

import androidx.annotation.Keep
import net.linkmate.app.data.model.Base

/**
 * @author Raleigh.Luo
 * date：20/12/24 15
 * describe：
 */
@Keep
data class Login(var expire:String? = null, var token: String? = null): Base()