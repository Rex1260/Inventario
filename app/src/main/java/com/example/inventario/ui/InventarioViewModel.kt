package com.example.inventario.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.supabase
import com.example.inventario.model.Dano
import com.example.inventario.model.Equipo
import com.example.inventario.model.Prestamo
import com.example.inventario.model.Usuario
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
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
    
    // Todos los préstamos activos para la vista de Pendientes
    val prestamosActivos = mutableStateListOf<Prestamo>()
    
    // Historial de préstamos devueltos
    val historialPrestamos = mutableStateListOf<Prestamo>()

    // Usuarios oficiales de la tabla usuarios
    val usuariosEncontrados = mutableStateListOf<Usuario>()
    var usuarioSeleccionado by mutableStateOf<Usuario?>(null)

    // Nombres de personas con préstamos activos para sugerencias
    val nombresConPrestamosActivos = mutableStateListOf<String>()

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

    // Equipos actualmente prestados
    val equiposPrestados by derivedStateOf {
        _equipos.filter { it.estado == "PRESTADO" && it.deletedAt == null }
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
                
                // Actualizar sugerencias solo con equipos activos para que no salgan cosas borradas
                if (!isTrashMode) {
                    categoriasExistentes.clear()
                    categoriasExistentes.addAll(results.mapNotNull { it.categoria }.distinct())
                    marcasExistentes.clear()
                    marcasExistentes.addAll(results.mapNotNull { it.marca }.distinct())
                    modelosExistentes.clear()
                    modelosExistentes.addAll(results.mapNotNull { it.modelo }.distinct())
                }

                // Cargar nombres con préstamos activos
                val pActivos = supabase.from("prestamos")
                    .select() {
                        filter { eq("estado", "activo") }
                    }.decodeList<Prestamo>()
                
                prestamosActivos.clear()
                prestamosActivos.addAll(pActivos)
                
                nombresConPrestamosActivos.clear()
                nombresConPrestamosActivos.addAll(pActivos.mapNotNull { it.nombreComodatario }.distinct())
                
                // Cargar Historial (últimos 100 préstamos devueltos)
                val pHistorial = supabase.from("prestamos")
                    .select() {
                        filter { eq("estado", "devuelto") }
                        order("fecha_devolucion", Order.DESCENDING)
                        limit(100)
                    }.decodeList<Prestamo>()
                historialPrestamos.clear()
                historialPrestamos.addAll(pHistorial)
                
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun buscarUsuarios(query: String) {
        if (query.length < 2) {
            usuariosEncontrados.clear()
            return
        }
        viewModelScope.launch {
            try {
                val results = supabase.from("usuarios")
                    .select {
                        filter {
                            or {
                                ilike("nombre", "%$query%")
                                ilike("email", "%$query%")
                            }
                        }
                        limit(5)
                    }.decodeList<Usuario>()
                usuariosEncontrados.clear()
                usuariosEncontrados.addAll(results)
            } catch (e: Exception) {
                Log.e("Usuarios", "Error buscando: ${e.message}")
            }
        }
    }

    suspend fun obtenerFolioParaUsuario(nombre: String, idUsuario: String? = null): String {
        return try {
            val prestamoExistente = supabase.from("prestamos")
                .select() {
                    filter {
                        if (idUsuario != null) {
                            eq("id_usuario", idUsuario)
                        } else {
                            eq("nombre_comodatario", nombre.uppercase())
                        }
                        eq("estado", "activo")
                    }
                    limit(1)
                }.decodeSingleOrNull<Prestamo>()
            
            if (prestamoExistente != null) {
                prestamoExistente.folio ?: "FOLIO-ERROR"
            } else {
                generarNuevoFolio()
            }
        } catch (e: Exception) {
            generarNuevoFolio()
        }
    }

    private suspend fun generarNuevoFolio(): String {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val count = try {
            val results = supabase.from("prestamos")
                .select() {
                    filter {
                        ilike("folio", "PR-$year-%")
                    }
                }.decodeList<Prestamo>()
            
            val maxNum = results.mapNotNull { 
                it.folio?.substringAfterLast("-")?.toIntOrNull() 
            }.maxOrNull() ?: 0
            maxNum + 1
        } catch (e: Exception) {
            1
        }
        return "PR-$year-${count.toString().padStart(5, '0')}"
    }

    fun realizarPrestamo(
        nombre: String,
        idUsuario: String?,
        firmaBytes: ByteArray,
        dispositivoModelo: String,
        dispositivoNombre: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.d("Prestamo", "Iniciando proceso para: $nombre")
                
                // 1. Obtener Folio
                val folio = obtenerFolioParaUsuario(nombre, idUsuario)
                Log.d("Prestamo", "Folio asignado: $folio")
                
                // 2. Subir Firma
                val fileName = "firma_${folio}_${System.currentTimeMillis()}.jpg"
                supabase.storage.from("firmas_prestamos").upload(fileName, firmaBytes)
                val firmaUrl = supabase.storage.from("firmas_prestamos").publicUrl(fileName)
                Log.d("Prestamo", "Firma subida: $firmaUrl")

                // 3. Crear registros de préstamo y actualizar equipos
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())

                equiposSeleccionados.forEach { equipo ->
                    Log.d("Prestamo", "Procesando equipo: ${equipo.noInventario}")
                    
                    val prestamo = Prestamo(
                        idEquipo = equipo.id,
                        idUsuario = idUsuario,
                        folio = folio,
                        nombreComodatario = nombre.uppercase(),
                        fechaPrestamo = now,
                        estado = "activo",
                        firmaUrl = firmaUrl,
                        modeloDispositivo = dispositivoModelo,
                        nombreDispositivo = dispositivoNombre
                    )
                    supabase.from("prestamos").insert(prestamo)

                    supabase.from("equipos").update(mapOf("estado" to "PRESTADO")) {
                        filter { eq("id", equipo.id!!) }
                    }
                }

                // 4. Llamar a la Edge Function para enviar el correo
                try {
                    val payload = mapOf(
                        "folio" to folio,
                        "nombre" to nombre.uppercase(),
                        "firma_url" to firmaUrl
                    )
                    supabase.functions.invoke("send-loan-contract", payload)
                    Log.d("Prestamo", "Edge Function llamada con éxito")
                } catch (e: Exception) {
                    Log.e("Prestamo", "Error al llamar Edge Function: ${e.message}")
                    // No bloqueamos el éxito del préstamo si solo falla el correo
                }

                equiposSeleccionados.clear()
                usuarioSeleccionado = null
                fetchEquipos()
                onSuccess()
            } catch (e: Exception) {
                Log.e("Prestamo", "Error: ${e.message}", e)
                errorMessage = "Error al procesar préstamo: ${e.message}"
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

    fun registrarDevolucion(
        equipo: Equipo,
        prestamo: Prestamo?,
        context: android.content.Context,
        dano: Dano? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())

                // 1. Actualizar estado del equipo
                val nuevoEstado = if (dano != null) "EN MANTENIMIENTO" else "FUNCIONAL"
                supabase.from("equipos").update(
                    mapOf(
                        "estado" to nuevoEstado,
                        "fecha_modificacion" to now,
                        "modificado_por_modelo" to android.os.Build.MODEL,
                        "modificado_por_nombre" to (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido")
                    )
                ) {
                    filter { eq("id", equipo.id!!) }
                }

                // 2. Marcar el préstamo como devuelto
                var folioFinalizado = ""
                prestamo?.let { p ->
                    supabase.from("prestamos").update(
                        mapOf(
                            "estado" to "devuelto",
                            "fecha_devolucion" to now
                        )
                    ) {
                        filter { eq("id", p.id!!) }
                    }
                    folioFinalizado = p.folio ?: ""
                }

                // 3. Registrar daño si existe
                dano?.let { d ->
                    val danoConFecha = d.copy(
                        idEquipo = equipo.id,
                        folioPrestamo = folioFinalizado,
                        fechaReporte = now
                    )
                    supabase.from("danos").insert(danoConFecha)
                    
                    // Si el daño es grave, enviar correo de alerta
                    if (d.gravedad == "grave") {
                        try {
                            val payload = mapOf(
                                "folio" to folioFinalizado,
                                "equipo" to "${equipo.noInventario} - ${equipo.nombre}",
                                "comodatario" to (prestamo?.nombreComodatario ?: "Desconocido"),
                                "descripcion" to d.descripcion,
                                "imagen_url" to d.imagenUrl
                            )
                            supabase.functions.invoke("notify-grave-damage", payload)
                        } catch (e: Exception) {
                            Log.e("Danos", "Error enviando alerta grave: ${e.message}")
                        }
                    }
                }

                // 4. Verificar si quedan equipos pendientes en ese mismo FOLIO
                val pendientes = if (folioFinalizado.isNotEmpty()) {
                    supabase.from("prestamos").select {
                        filter {
                            eq("folio", folioFinalizado)
                            eq("estado", "activo")
                        }
                    }.decodeList<Prestamo>()
                } else emptyList()

                fetchEquipos()
                
                // Mensaje personalizado según si se cerró el folio o no
                val mensaje = if (pendientes.isEmpty()) {
                    // Si el folio se cerró, calculamos la fecha de borrado de firma (hoy + 15 días)
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 15)
                    val expiraAt = sdf.format(calendar.time)
                    
                    if (folioFinalizado.isNotEmpty()) {
                        supabase.from("prestamos").update(mapOf("firma_expira_at" to expiraAt)) {
                            filter { eq("folio", folioFinalizado) }
                        }
                    }
                    
                    "¡FOLIO $folioFinalizado CERRADO! Equipos devueltos."
                } else {
                    val cant = pendientes.size
                    "Equipo recibido. Quedan $cant artículos en el folio $folioFinalizado"
                }
                
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }

                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al registrar devolución: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun subirImagenDano(context: android.content.Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
                val bytes = bos.toByteArray()
                
                val fileName = "dano_${System.currentTimeMillis()}.jpg"
                supabase.storage.from("fotos_danos").upload(fileName, bytes)
                supabase.storage.from("fotos_danos").publicUrl(fileName)
            } catch (e: Exception) {
                Log.e("Upload", "Error: ${e.message}")
                null
            }
        }
    }
}
