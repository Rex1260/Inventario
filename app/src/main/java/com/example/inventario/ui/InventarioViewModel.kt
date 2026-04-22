package com.example.inventario.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.model.Equipo
import com.example.inventario.data.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class InventarioViewModel : ViewModel() {
    private val _equipos = mutableStateListOf<Equipo>()
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")

    val filteredEquipos by derivedStateOf {
        if (searchQuery.isBlank()) {
            // Si no hay búsqueda, mostramos solo el último registro (el primero de la lista si ordenamos DESC)
            if (_equipos.isNotEmpty()) listOf(_equipos.first()) else emptyList()
        } else {
            // Si hay búsqueda, mostramos los que coincidan
            _equipos.filter { equipo ->
                equipo.noInventario?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    fun fetchEquipos() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Ordenamos por id o fecha de forma descendente para tener el último arriba
                val results = supabase.from("equipos")
                    .select() {
                        order("fecha_registro", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<Equipo>()
                _equipos.clear()
                _equipos.addAll(results)
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
