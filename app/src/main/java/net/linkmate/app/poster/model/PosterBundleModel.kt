package net.linkmate.app.poster.model

import java.io.Serializable

data class PosterBundleModel(
    val session: String,
    val sessionLocal: String,
    val ip: String,
    val deviceId: String,
    val updateTime: String
) : Serializable