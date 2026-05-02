package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SystemDna(
    val id: Int? = null,
    val category: String, // 'TABLE', 'FIELD', 'ACTION', 'SEARCH', 'SECURITY'
    @SerialName("parent_entity") val parentEntity: String? = null,
    @SerialName("component_key") val componentKey: String,
    @SerialName("display_name") val displayName: String? = null,
    val description: String? = null,
    @SerialName("technical_specs") val technicalSpecs: JsonObject? = null,
    @SerialName("action_flow") val actionFlow: String? = null,
    val status: String = "ACTIVO", // 'ACTIVO', 'POSPUESTO', 'DEPRECATED'
    val dependencies: List<String>? = null,
    @SerialName("notes_history") val notesHistory: String? = null
)
