package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Equipo(
    @SerialName("id")
    val id: String? = null,
    @SerialName("no_inventario")
    val noInventario: String? = null,
    @SerialName("nombre")
    val nombre: String? = null,
    @SerialName("descripcion")
    val descripcion: String? = null,
    @SerialName("categoria")
    val categoria: String? = null,
    @SerialName("marca")
    val marca: String? = null,
    @SerialName("modelo")
    val modelo: String? = null,
    @SerialName("estado")
    val estado: String? = null,
    @SerialName("numero_serie")
    val numeroSerie: String? = null,
    @SerialName("numerotag")
    val numerotag: String? = null,
    @SerialName("fecha_registro")
    val fechaRegistro: String? = null,
    @SerialName("fecha_modificacion")
    val fechaModificacion: String? = null,
    @SerialName("imagen_url")
    val imagenUrl: String? = null,
    @SerialName("creado_por_modelo")
    val creadoPorModelo: String? = null,
    @SerialName("creado_por_nombre")
    val creadoPorNombre: String? = null,
    @SerialName("modificado_por_modelo")
    val modificadoPorModelo: String? = null,
    @SerialName("modificado_por_nombre")
    val modificadoPorNombre: String? = null
)
