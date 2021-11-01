package net.linkmate.app.ui.simplestyle.device.self_check.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SmartInfo(
    @field: SerializedName("ATA Version is") val ATA_Version: String,
    @field: SerializedName("Device Model") val deviceModel: String,
    @field: SerializedName("Device is") val deviceIs: String,
    @field: SerializedName("Firmware Version") val FirmwareVersion: String,
    @field: SerializedName("LU WWN Device Id") val lUWWNDeviceId: String,
    @field: SerializedName("Local Time is") val localTime: String,
    @field: SerializedName("Rotation Rate") val RotationRate: String,
    @field: SerializedName("SATA Version is") val SATAVersion: String,
    @field: SerializedName("SMART support is") val sMARTSupport: String,
    @field: SerializedName("Sector Sizes") val sectorSizes: String,
    @field: SerializedName("Serial Number") val serialNumber: String,
    @field: SerializedName("User Capacity") val userCapacity: String

)
