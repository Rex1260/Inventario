package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Plantilla(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("nombre_plantilla")
    val nombrePlantilla: String,
    @SerialName("equipo_nombre")
    val equipoNombre: String? = null,
    @SerialName("categoria")
    val categoria: String? = null,
    @SerialName("marca")
    val marca: String? = null,
    @SerialName("modelo")
    val modelo: String? = null,
    @SerialName("descripcion_default")
    val descripcionDefault: String? = null,
    @SerialName("specs_json")
    val specsJson: String = "{}",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null
)
