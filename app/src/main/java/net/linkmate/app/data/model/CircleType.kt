package net.linkmate.app.data.model

import androidx.annotation.Keep
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable

/**
 * @author Raleigh.Luo
 * date：20/8/19 16
 * describe：
 */
@Keep
data class CircleType(var data:Types?=null) : GsonBaseProtocol(){
    @Keep
    data class Types(var list:List<Type>?)
    @Keep
    data class Type(
           var modelid:String?=null,//类型ID
           var modelname:String?=null,//类型名称(后台国际化支持)
           var modelprops:TypeProperty?=null //类型属性
    ): Serializable
    @Keep
    data class TypeProperty(
            var network_scale:List<TypePropertyValue>?=null, //网络规模
            var network_fee:List<TypePropertyValue>?=null //网络费用
    ):Serializable
    @Keep
    data class TypePropertyValue(
            var key:String?=null,
            var value:String?=null,
            var title:String?=null,
            var owner_custom:Boolean?=null//网络属主是否可修改此属性
    ):Serializable
}