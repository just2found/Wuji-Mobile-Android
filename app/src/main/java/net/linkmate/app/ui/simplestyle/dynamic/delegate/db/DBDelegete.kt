package net.linkmate.app.ui.simplestyle.dynamic.delegate.db

import androidx.arch.core.util.Function
import io.objectbox.BoxStore
import net.linkmate.app.base.MyApplication
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.vo.DynamicComment
import net.sdvn.common.vo.DynamicLike
import net.sdvn.common.vo.DynamicRelated

/**
 * @author Raleigh.Luo
 * date：21/1/19 13
 * describe：
 */
abstract class DBDelegete<T> {
    companion object {
        val dynamicDelegete: DynamicDelegeteImpl = DynamicDelegeteImpl()
        val likeDelegete: DBDelegete<DynamicLike> = LikeDelegeteImpl()
        val commentDelegete: DBDelegete<DynamicComment> = CommentDelegeteImpl()
        val relatedDelegate: DBDelegete<DynamicRelated> = RelatedDelegateImpl()
    }

    /**
     * 保存服务器数据
     */
    open fun save(networkId: String, deviceId: String, data: List<T>?) {}

    /**
     * 更新服务器数据
     */
    open fun update(autoIncreaseId: Long, data: T?) {}

    /**
     * 本地添加
     */
    open fun updateLocal(data: T, dynamicAutoIncreaseId: Long = -1, callback: Function<Long, Void>) {}

    /**
     * 逻辑本地删除
     */
    open fun deleteLocal(autoIncreaseId: Long, callback: Function<Long, Void>) {}

    /**
     * 物理删除 记录
     */
    open fun delete(autoIncreaseId: Long) {}

    /**
     * 物理删除 记录
     * @param id 服务器返回id,非本地数据id
     */
    open fun deleteById(networkId: String, deviceId: String, id: Long) {}


    /**
     * 检查任务，是否需要继续或丢弃任务
     * @param operateType 操作类型
     */
    open fun check(autoIncreaseId: Long, operateType:String, callback: Function<T, Void>) {}

    fun getBoxStore(): BoxStore? {
        return DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()
    }
}