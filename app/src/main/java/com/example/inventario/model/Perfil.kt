package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    @SerialName("id")
    val id: String,
    @SerialName("nombre")
    val nombre: String? = null,
    @SerialName("rol")
    val rol: String? = "USER" // ADMIN, USER, o VIEWER
)
