package com.example.inventario.model

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val id: Int? = null,
    val latest_version_code: Int,
    val latest_version_name: String,
    val apk_url: String,
    val release_notes: String? = null
)
