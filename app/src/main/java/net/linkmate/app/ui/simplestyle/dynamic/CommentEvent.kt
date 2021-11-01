package net.linkmate.app.ui.simplestyle.dynamic

import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import java.io.Serializable

/** 评论事件触发
 * @author Raleigh.Luo
 * date：20/12/18 15
 * describe：
 */
class CommentEvent:Serializable {
    var type: Int//类型 COMMENT_TYPE评论，REPLY_TYPE回复
    var position: Int//动态项position
    var hint: String
    var dynamicId: Long
    var dynamicAutoIncreaseId: Long
    var commentId: Long? = null
    var targetUID: String? = null//null 代表只是评论，不为空则为回复
    var targetUserName: String? = null
    var screenLocationY: Int = 0

    constructor(position: Int,dynamicId: Long,dynamicAutoIncreaseId: Long) {//评论 构造
        this.type = COMMENT_TYPE
        this.position = position
        this.hint = MyApplication.getContext().getString(R.string.comment)
        this.dynamicId = dynamicId
        this.dynamicAutoIncreaseId = dynamicAutoIncreaseId
    }

    constructor(position: Int,
                dynamicId: Long,
                dynamicAutoIncreaseId: Long,
                commentId: Long,
                targetUID: String,
                targetUserName: String,
                screenLocationY: Int
               ) {//回复
        this.type = REPLY_TYPE
        this.position = position
        this.hint = String.format("%s（%s）：", MyApplication.getContext().getString(R.string.reply), targetUserName)
        this.screenLocationY = screenLocationY
        this.dynamicId = dynamicId
        this.commentId = commentId
        this.targetUserName = targetUserName
        this.targetUID = targetUID
        this.dynamicAutoIncreaseId = dynamicAutoIncreaseId
    }

    companion object {
        const val COMMENT_TYPE = 0//评论
        const val REPLY_TYPE = 1//回复
    }
}