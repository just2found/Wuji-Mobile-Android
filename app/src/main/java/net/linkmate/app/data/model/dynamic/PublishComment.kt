package net.linkmate.app.data.model.dynamic

import androidx.annotation.Keep
import net.sdvn.cmapi.CMAPI

/**
 * @author Raleigh.Luo
 * date：20/12/25 14
 * describe：
 */
@Keep
data class PublishComment(val momentID: Long,//动态id
                          val id: Long? = null,//评论id
                          val targetUID: String? = null,//回复的对象userid  null 代表只是评论，不为空则为回复
                          val targetUserName: String? = null,//回复的对象昵称
                          val content: String
)