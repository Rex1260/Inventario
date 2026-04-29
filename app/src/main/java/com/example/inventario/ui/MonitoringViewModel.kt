package com.example.inventario.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.supabase
import com.example.inventario.model.SupabaseUsageStats
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class MonitoringViewModel : ViewModel() {
    var stats by mutableStateOf<SupabaseUsageStats?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchStats() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Intentar obtener la última captura de la tabla
                val result = supabase.from("supabase_stats")
                    .select() {
                        order("captured_at", Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<SupabaseUsageStats>()
                
                stats = result
            } catch (e: Exception) {
                Log.e("Monitoring", "Error fetching stats: ${e.message}")
                errorMessage = "No se pudieron cargar las métricas: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Llamar a la Edge Function para forzar una actualización de métricas
                supabase.functions.invoke("fetch-supabase-usage")
                // Recargar los datos de la tabla
                fetchStats()
            } catch (e: Exception) {
                Log.e("Monitoring", "Error refreshing: ${e.message}")
                errorMessage = "Error al actualizar métricas: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
