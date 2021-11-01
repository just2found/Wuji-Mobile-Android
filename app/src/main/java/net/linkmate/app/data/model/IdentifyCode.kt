package net.linkmate.app.data.model

import net.sdvn.common.internet.core.GsonBaseProtocol

/**
 * @author Raleigh.Luo
 * date：21/3/22 11
 * describe：
 */
data class IdentifyCode(var data: Code? = null) : GsonBaseProtocol()
data class Code(var indentifycode: String? = null)