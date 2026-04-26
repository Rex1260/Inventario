package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id")
    val id: String? = null,
    @SerialName("nombre")
    val nombre: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("rol")
    val rol: String? = "usuario"
)
