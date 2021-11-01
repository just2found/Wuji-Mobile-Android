package net.linkmate.app.ui.simplestyle.device.self_check.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Hdinfo(@field: SerializedName("Power_On_Hours") val powerOnHours: Int,
                  @field: SerializedName("Temperature_Celsius") val temperatureCelsius: String,
                  @field: SerializedName("name") val name: String,
                  @field: SerializedName("slot") val slot: Int)
