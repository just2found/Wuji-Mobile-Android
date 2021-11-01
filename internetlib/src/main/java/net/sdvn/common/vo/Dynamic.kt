package net.sdvn.common.vo

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.objectbox.BoxStore
import io.objectbox.annotation.*
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne
import net.sdvn.cmapi.CMAPI
import java.io.Serializable
import java.util.*

/**
 * @author Raleigh.Luo
 * date：21/1/5 19
 * describe：
 */
@Keep
@Entity
data class Dynamic(
        @Expose
        @Id
        var autoIncreaseId: Long = 0,//数据库自增id, 忽略json解析字段
        @Expose
        var networkId: String? = null,//网络id  忽略json解析字段, 数据库中，networkId＋ID+deviceId 可以唯一确定一条数据，该值为请求后手动赋值
        @Expose
        var deviceId: String? = null,//主EN设备ID,仅用来过滤
        @SerializedName("id")
        var ID: Long? = null,
        @SerializedName("uid")
        var UID: String? = null,
        @SerializedName("username")
        var Username: String? = null,
        @SerializedName("content")
        var Content: String? = null,
        @SerializedName("location")
        var Location: String? = null,
        @SerializedName("createat")
        var CreateAt: Long? = null,//时间戳，秒数
        @SerializedName("updateat")
        var UpdateAt: Long? = null,//时间戳，秒数
        /**--DTO 数据list ,不存储 仅用于json 数据解析---**/
        @Transient
        @SerializedName("medias")
        var Medias: List<DynamicMedia>? = null,
        @Transient
        @SerializedName("attachments")
        var Attachments: List<DynamicAttachment>? = null,
        @Transient
        @SerializedName("comments")
        var Comments: List<DynamicComment>? = null,//用于网络请求解析时 评论数据，且临时用于过滤CommentsPO非本地删除数据
        @Transient
        @SerializedName("likes")
        var Likes: List<DynamicLike>? = null,//用于网络请求解析时 评论数据，且临时用于过滤CommentsPO非本地删除数据
        @Expose
        var isDeleted: Boolean = false//是否被逻辑删除，为本地进行删除时，临时保存字段

) {
    /**--PO 数据list ,存储 不参与json解析---**/
    @Expose
    @Backlink
    lateinit var MediasPO: ToMany<DynamicMedia>

    @Expose
    @Backlink
    lateinit var AttachmentsPO: ToMany<DynamicAttachment>

    @Expose
    @Backlink
    lateinit var CommentsPO: ToMany<DynamicComment>

    @Expose
    @Backlink
    lateinit var LikesPO: ToMany<DynamicLike>

    fun transfer(networkId: String, deviceId: String, boxStore: BoxStore) {
        this.networkId = networkId
        this.deviceId = deviceId
        boxStore.boxFor(Dynamic::class.java).attach(this)
        //排序 index正序排序，先排序再存
        toSort()
        Medias?.forEach {
            MediasPO.add(it)
        }
        Attachments?.forEach {
            AttachmentsPO.add(it)
        }
        Comments?.forEach {
            CommentsPO.add(it)
        }
        Likes?.forEach {
            LikesPO.add(it)
        }
    }

    /**
     * 网络请求到的数据排序
     */
    private fun toSort() {
        if (Medias != null)
            Collections.sort(Medias, object : Comparator<DynamicMedia> {
                override fun compare(p0: DynamicMedia, p1: DynamicMedia): Int {
                    when {
                        (p0.index ?: 0) > (p1.index ?: 0) -> {
                            return 1
                        }
                        (p0.index ?: 0) < (p1.index ?: 0) -> {
                            return -1
                        }
                        else -> {
                            return 0
                        }
                    }
                }

            })
    }
}

@Entity
@Keep
data class DynamicMedia(
        @Expose
        @Id
        var autoIncreaseId: Long = 0,//数据库自增id, 忽略json解析字段
        var id: Long? = null,//后台ID，若为本地数据(待请求数据)，id=-1
        @SerializedName("momentid")
        var momentID: Long? = null,
        var url: String? = null,//远程下载地址
        @Expose
        var localPath: String? = null,//本地存储字段 本地文件路径时，可用id=-1区分本地或网络
        var index: Int? = null,
        var thumbnail: String? = null,
        var width: Int? = null,//原图宽
        var height: Int? = null,//原图高
        var type: String? = null//类型：image/video

) {
    @Expose
    lateinit var dynamic: ToOne<Dynamic>

    fun getVideoType(): String {
        return "video"
    }

    fun getImageType(): String {
        return "image"
    }

}

@Entity
@Keep
data class DynamicAttachment(
        @Expose
        @Id
        var autoIncreaseId: Long = 0,//数据库自增id, 忽略json解析字段
        var id: Long? = null,//后台ID，若为本地数据(待请求数据)，id=-1
        @SerializedName("momentid")
        var momentID: Long? = null,
        var url: String? = null,//远程下载地址
        @Expose
        var localPath: String? = null,//本地存储字段 本地文件路径时，可用id=-1区分本地或网络
        var size: Long? = null,//大小
        var name: String? = null,//附件名字
        var cost: String? = null//费用
) {
    @Expose
    lateinit var dynamic: ToOne<Dynamic>
}

@Entity
@Keep
data class DynamicComment(
        @Expose
        @Id
        var autoIncreaseId: Long = 0,//数据库自增id, 忽略json解析字段
        var id: Long? = null,//后台评论ID，若为本地数据(待请求数据)，id=-1
        @SerializedName("momentid")
        var momentID: Long? = null,
        var uid: String? = null,
        var username: String? = null,
        @SerializedName("targetuid")
        var targetUID: String? = null,
        @SerializedName("targetusername")
        var targetUserName: String? = null,
        var content: String? = null,
        @SerializedName("createat")
        var createAt: Long? = null,
        @Expose
        var isDeleted: Boolean = false//暂时未用，是否被逻辑删除，为本地进行删除时，临时保存字段

):Serializable {
    @Expose
    lateinit var dynamic: ToOne<Dynamic>
}

@Entity
@Keep
data class DynamicLike(
        @Expose
        @Id
        var autoIncreaseId: Long = 0,//数据库自增id, 忽略json解析字段
        var id: Long? = null,//后台ID，若为本地数据(待请求数据)，id=-1
        @SerializedName("momentid")
        var momentID: Long? = null,
        var uid: String? = null,
        var username: String? = null,
        @SerializedName("updateat")
        var updateAt: Long? = null,
        @Expose
        var isDeleted: Boolean = false//是否被逻辑删除，为本地进行删除时，临时保存字段

) {
    @Expose
    lateinit var dynamic: ToOne<Dynamic>
}

@Entity
@Keep
data class DynamicRelated(//数据库对象
        @Id(assignable = true)
        @Expose
        var relatedId: Long? = null,//相关id 点赞或评论id
        @SerializedName("momentid")
        var momentID: Long? = null,
        @Transient
        var comment: DynamicComment? = null,////网络数据，不存储数据库字段
        @Transient
        var like: DynamicLike? = null,//网络数据，不存储数据库字段
        @Expose
        var type: String? = null,//类型 点赞LIKE或评论COMMENT
        @Expose
        var createAt: Long? = null,//时间
        @Expose
        var uid: String? = null,
        @Expose
        var username: String? = null,
        @Expose
        var targetUID: String? = null,
        @Expose
        var targetUserName: String? = null,
        @Expose
        var content: String? = null,

        @Expose
        var networkId: String? = null,//网络id  忽略json解析字段, 数据库中，networkId＋ID+deviceId 可以唯一确定一条数据，该值为请求后手动赋值
        @Expose
        var deviceId: String? = null//主EN设备ID,仅用来过滤

) {
    fun getLikeType(): String {
        return "LIKE"
    }

    fun getCommentType(): String {
        return "COMMENT"
    }

    /**
     * 由网络请求数据初始化存储到db数据
     */
    fun init(networkId: String, deviceId: String) {
        comment?.let {
            type = getCommentType()
            relatedId = it.id
            createAt = it.createAt
            uid = it.uid
            username = it.username
            targetUID = it.targetUID
            targetUserName = it.targetUserName
            content = it.content
        }

        like?.let {
            type = getLikeType()
            relatedId = it.id
            createAt = it.updateAt
            uid = it.uid
            username = it.username
        }
        this.networkId = networkId
        this.deviceId = deviceId
    }
}