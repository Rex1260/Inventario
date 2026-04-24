package com.example.inventario.ui

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.supabase
import com.example.inventario.model.Equipo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventarioViewModel : ViewModel() {
    private val _equipos = mutableStateListOf<Equipo>()
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    var isTrashMode by mutableStateOf(false)

    // Selección para préstamos
    val equiposSeleccionados = mutableStateListOf<Equipo>()

    // Listas para sugerencias
    val categoriasExistentes = mutableStateListOf<String>()
    val marcasExistentes = mutableStateListOf<String>()
    val modelosExistentes = mutableStateListOf<String>()

    val filteredEquipos by derivedStateOf {
        if (searchQuery.isBlank()) {
            if (_equipos.isNotEmpty()) listOf(_equipos.first()) else emptyList()
        } else {
            _equipos.filter { equipo ->
                equipo.noInventario?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // Equipos disponibles para préstamo (FUNCIONAL y no eliminados)
    val equiposDisponiblesParaPrestamo by derivedStateOf {
        _equipos.filter { it.estado == "FUNCIONAL" && it.deletedAt == null }
    }

    fun toggleSeleccion(equipo: Equipo) {
        if (equiposSeleccionados.contains(equipo)) {
            equiposSeleccionados.remove(equipo)
        } else {
            equiposSeleccionados.add(equipo)
        }
    }

    fun fetchEquipos() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Filtramos según el modo (Normal o Papelera)
                val results = supabase.from("equipos")
                    .select() {
                        filter {
                            if (isTrashMode) {
                                // Modo Papelera: los que SÍ están eliminados (deleted_at NO es NULL)
                                filterNot("deleted_at", FilterOperator.IS, null)
                            } else {
                                // Modo Normal: los que NO están eliminados (deleted_at ES NULL)
                                filter("deleted_at", FilterOperator.IS, null)
                            }
                        }
                        order("fecha_registro", Order.DESCENDING)
                    }
                    .decodeList<Equipo>()
                _equipos.clear()
                _equipos.addAll(results)
                
                // Limpiar selección si los equipos ya no están en la lista (por si acaso)
                equiposSeleccionados.removeAll { sel -> !results.any { it.id == sel.id } }
                
                // Actualizar sugerencias solo con equipos activos para que no salgan cosas borradas
                if (!isTrashMode) {
                    categoriasExistentes.clear()
                    categoriasExistentes.addAll(results.mapNotNull { it.categoria }.distinct())
                    marcasExistentes.clear()
                    marcasExistentes.addAll(results.mapNotNull { it.marca }.distinct())
                    modelosExistentes.clear()
                    modelosExistentes.addAll(results.mapNotNull { it.modelo }.distinct())
                }
                
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun isNoInventarioDuplicado(noInventario: String, currentId: String?): Boolean {
        // Buscamos en la lista local (que ya tiene todos los registros cargados)
        // si existe otro equipo con ese número que no sea el mismo que estamos editando
        return _equipos.any { it.noInventario == noInventario && it.id != currentId }
    }

    fun toggleTrashMode() {
        isTrashMode = !isTrashMode
        fetchEquipos()
    }

    fun restaurarEquipo(equipo: Equipo, context: android.content.Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                supabase.from("equipos").update(
                    mapOf(
                        "deleted_at" to null,
                        "modificado_por_modelo" to android.os.Build.MODEL,
                        "modificado_por_nombre" to (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido")
                    )
                ) {
                    filter { eq("id", equipo.id!!) }
                }
                fetchEquipos()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al restaurar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun eliminarFisico(equipo: Equipo, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                supabase.from("equipos").delete {
                    filter { eq("id", equipo.id!!) }
                }
                fetchEquipos()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error en borrado permanente: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun sugerirSiguienteNumero(prefijo: String): String {
        if (prefijo.length != 3) return ""
        val maxNumero = _equipos
            .filter { it.noInventario?.startsWith(prefijo, ignoreCase = true) == true }
            .mapNotNull { 
                it.noInventario?.substring(3)?.toIntOrNull()
            }
            .maxOrNull() ?: 0
        
        return prefijo.uppercase() + (maxNumero + 1).toString().padStart(5, '0')
    }

    fun guardarEquipo(
        equipo: Equipo,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 2. Insertar en DB
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())
                val equipoConFechas = equipo.copy(
                    fechaRegistro = now,
                    fechaModificacion = now
                )
                
                supabase.from("equipos").insert(equipoConFechas)
                fetchEquipos()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al guardar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun actualizarEquipo(
        equipo: Equipo,
        oldImagenUrl: String?,
        nuevaImagenUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Si hay una imagen anterior y se subió una nueva, borramos la vieja del Storage
                if (!oldImagenUrl.isNullOrEmpty() && nuevaImagenUri != null) {
                    try {
                        val fileName = oldImagenUrl.substringAfterLast("/")
                        supabase.storage.from("fotos_equipos").delete(fileName)
                    } catch (e: Exception) {
                        // Continuamos aunque falle el borrado de la imagen vieja
                    }
                }

                // 2. Actualizar en DB
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())
                
                // Preparamos los datos a actualizar (excluimos campos que no cambian como fecha_registro o id)
                val updates = mapOf(
                    "no_inventario" to equipo.noInventario,
                    "nombre" to equipo.nombre,
                    "descripcion" to equipo.descripcion,
                    "categoria" to equipo.categoria,
                    "marca" to equipo.marca,
                    "modelo" to equipo.modelo,
                    "estado" to equipo.estado,
                    "numero_serie" to equipo.numeroSerie,
                    "numerotag" to equipo.numerotag,
                    "imagen_url" to equipo.imagenUrl,
                    "fecha_modificacion" to now,
                    "modificado_por_modelo" to equipo.modificadoPorModelo,
                    "modificado_por_nombre" to equipo.modificadoPorNombre
                )

                supabase.from("equipos").update(updates) {
                    filter { eq("id", equipo.id!!) }
                }

                fetchEquipos()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al actualizar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun eliminarEquipoLogico(equipo: Equipo, context: android.content.Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Si tiene imagen, la borramos físicamente de Storage
                if (!equipo.imagenUrl.isNullOrEmpty()) {
                    try {
                        val fileName = equipo.imagenUrl.substringAfterLast("/")
                        supabase.storage.from("fotos_equipos").delete(fileName)
                    } catch (e: Exception) {
                        // Si falla el borrado de imagen, continuamos con el registro
                    }
                }

                // 2. Actualización lógica en DB
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())
                
                supabase.from("equipos").update(
                    mapOf(
                        "deleted_at" to now,
                        "imagen_url" to null,
                        "modificado_por_modelo" to android.os.Build.MODEL,
                        "modificado_por_nombre" to (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido")
                    )
                ) {
                    filter {
                        eq("id", equipo.id!!)
                    }
                }

                fetchEquipos()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al eliminar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
