package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ProductPaymentCode(
    @SerializedName("qrcode_wechat")
    val qrWechat: String?,
    @SerializedName("qrcode_alipay")
    val qrAlipay: String?,
    @SerializedName("integral_exchange")
    val integralExchange: String?,
    @SerializedName("screenshot")
    val screenshot: String?
) : Serializable