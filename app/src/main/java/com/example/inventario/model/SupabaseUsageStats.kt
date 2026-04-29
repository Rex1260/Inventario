package com.example.inventario.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUsageStats(
    val id: Int? = null,
    @SerialName("db_size_bytes") val dbSizeBytes: Long = 0,
    @SerialName("storage_size_bytes") val storageSizeBytes: Long = 0,
    @SerialName("storage_egress_bytes") val storageEgressBytes: Long = 0,
    @SerialName("edge_invocations") val edgeInvocations: Long = 0,
    @SerialName("auth_mau") val authMau: Long = 0,
    @SerialName("realtime_connections") val realtimeConnections: Long? = 0,
    @SerialName("realtime_messages") val realtimeMessages: Long = 0,
    @SerialName("captured_at") val capturedAt: String? = null
) {
    // Límites del Plan Gratuito
    companion object {
        const val LIMIT_DB_SIZE = 500L * 1024 * 1024 // 500 MB
        const val LIMIT_STORAGE_SIZE = 1024L * 1024 * 1024 // 1 GB
        const val LIMIT_STORAGE_EGRESS = 2048L * 1024 * 1024 // 2 GB
        const val LIMIT_EDGE_INVOCATIONS = 500_000L
        const val LIMIT_AUTH_MAU = 50_000L
        const val LIMIT_REALTIME_CONNS = 200L
        const val LIMIT_REALTIME_MSGS = 2_000_000L
    }
}
