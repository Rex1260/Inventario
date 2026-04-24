package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Prestamo(
    @SerialName("id")
    val id: String? = null,
    @SerialName("id_equipo")
    val idEquipo: String? = null,
    @SerialName("folio")
    val folio: String? = null,
    @SerialName("nombre_comodatario")
    val nombreComodatario: String? = null,
    @SerialName("fecha_prestamo")
    val fechaPrestamo: String? = null,
    @SerialName("fecha_devolucion")
    val fechaDevolucion: String? = null,
    @SerialName("estado")
    val estado: String? = "activo",
    @SerialName("firma_url")
    val firmaUrl: String? = null,
    @SerialName("modelo_dispositivo")
    val modeloDispositivo: String? = null,
    @SerialName("nombre_dispositivo")
    val nombreDispositivo: String? = null
)
