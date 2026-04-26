package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dano(
    @SerialName("id")
    val id: String? = null,
    @SerialName("id_equipo")
    val idEquipo: String? = null,
    @SerialName("descripcion")
    val descripcion: String? = null,
    @SerialName("gravedad")
    val gravedad: String? = null, // 'leve', 'medio', 'grave'
    @SerialName("fecha_reporte")
    val fechaReporte: String? = null,
    @SerialName("estado")
    val estado: String? = "pendiente",
    @SerialName("imagen_url")
    val imagenUrl: String? = null,
    @SerialName("folio_prestamo")
    val folioPrestamo: String? = null,
    @SerialName("comentario_comodatario")
    val comentarioComodatario: String? = null
)
