package com.example.inventario.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import com.example.inventario.model.Perfil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import com.example.inventario.util.ContratoGenerator
import com.example.inventario.model.AppConfig
import com.example.inventario.BuildConfig
import android.content.Intent
import androidx.core.content.FileProvider
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventarioViewModel(application: Application) : AndroidViewModel(application) {
    var sessionInitialized by mutableStateOf(false)
    var currentUserPerfil by mutableStateOf<Perfil?>(null)
    
    // Estado para actualización de la app
    var appUpdateConfig by mutableStateOf<AppConfig?>(null)
    var showUpdateDialog by mutableStateOf(false)
    
    fun isAdmin() = currentUserPerfil?.rol == "ADMIN"
    fun isViewer() = currentUserPerfil?.rol == "VIEWER"
    fun isLogged() = currentUserPerfil != null

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

    // Lista de daños para el equipo seleccionado
    val danosDelEquipo = mutableStateListOf<Dano>()

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
            _equipos
        } else {
            _equipos.filter { equipo ->
                equipo.noInventario?.contains(searchQuery, ignoreCase = true) == true ||
                equipo.marca?.contains(searchQuery, ignoreCase = true) == true ||
                equipo.numeroSerie?.contains(searchQuery, ignoreCase = true) == true ||
                equipo.nombre?.contains(searchQuery, ignoreCase = true) == true
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

    // Estadísticas para el Dashboard
    val statsTotalEquipos by derivedStateOf { _equipos.count { it.deletedAt == null } }
    val statsPrestados by derivedStateOf { _equipos.count { it.estado == "PRESTADO" && it.deletedAt == null } }
    val statsMantenimiento by derivedStateOf { _equipos.count { it.estado == "EN MANTENIMIENTO" && it.deletedAt == null } }
    val statsDisponibles by derivedStateOf { _equipos.count { it.estado == "FUNCIONAL" && it.deletedAt == null } }
    
    val statsVencidos by derivedStateOf {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
        prestamosActivos.count { 
            !it.fechaVencimiento.isNullOrEmpty() && it.fechaVencimiento!! < now 
        }
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
                        order("no_inventario", Order.ASCENDING)
                    }
                    .decodeList<Equipo>()
                _equipos.clear()
                _equipos.addAll(results)
                
                // Actualizar sugerencias solo con equipos activos para que no salgan cosas borradas
                if (!isTrashMode) {
                    categoriasExistentes.clear()
                    categoriasExistentes.addAll(results.mapNotNull { it.categoria }.distinct().filter { it.isNotBlank() }.sorted())
                    marcasExistentes.clear()
                    marcasExistentes.addAll(results.mapNotNull { it.marca }.distinct().filter { it.isNotBlank() }.sorted())
                    modelosExistentes.clear()
                    modelosExistentes.addAll(results.mapNotNull { it.modelo }.distinct().filter { it.isNotBlank() }.sorted())
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

    fun fetchDanosEquipo(idEquipo: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val results = supabase.from("danos")
                    .select {
                        filter { eq("id_equipo", idEquipo) }
                        order("fecha_reporte", Order.DESCENDING)
                    }.decodeList<Dano>()
                danosDelEquipo.clear()
                danosDelEquipo.addAll(results)
            } catch (e: Exception) {
                Log.e("Danos", "Error al cargar daños: ${e.message}")
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
        fechaVencimiento: String? = null,
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
                        fechaVencimiento = fechaVencimiento,
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

                // 4. Generar PDF para el correo
                val pdfFile = withContext(Dispatchers.IO) {
                    val bitmapFirma = BitmapFactory.decodeByteArray(firmaBytes, 0, firmaBytes.size)
                    ContratoGenerator.generarContratoPDF(getApplication(), 
                        Prestamo(folio = folio, nombreComodatario = nombre), 
                        equiposSeleccionados.toList(), 
                        bitmapFirma)
                }
                
                val pdfBase64 = pdfFile?.readBytes()?.let { Base64.encodeToString(it, Base64.NO_WRAP) }

                // 5. Llamar a la Edge Function para enviar el correo con el PDF adjunto
                try {
                    val payload = mapOf(
                        "folio" to folio,
                        "nombre" to nombre.uppercase(),
                        "firma_url" to firmaUrl,
                        "pdf_base64" to pdfBase64
                    )
                    supabase.functions.invoke("send-loan-contract", payload)
                    Log.d("Prestamo", "Edge Function llamada con éxito (PDF incluido)")
                } catch (e: Exception) {
                    Log.e("Prestamo", "Error al llamar Edge Function: ${e.message}")
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
                        "modificado_por_nombre" to (currentUserPerfil?.nombre ?: "Admin")
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

    private suspend fun subirImagen(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos)
                val bytes = bos.toByteArray()
                
                val fileName = "equipo_${System.currentTimeMillis()}.jpg"
                supabase.storage.from("fotos_equipos").upload(fileName, bytes)
                supabase.storage.from("fotos_equipos").publicUrl(fileName)
            } catch (e: Exception) {
                Log.e("Upload", "Error: ${e.message}")
                null
            }
        }
    }

    fun guardarEquipo(
        equipo: Equipo,
        nuevaImagenUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                var finalImagenUrl = equipo.imagenUrl

                if (nuevaImagenUri != null) {
                    finalImagenUrl = subirImagen(nuevaImagenUri)
                }
                
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())
                val equipoConFechas = equipo.copy(
                    fechaRegistro = now,
                    fechaModificacion = now,
                    imagenUrl = finalImagenUrl
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
                var finalImagenUrl = equipo.imagenUrl

                if (nuevaImagenUri != null) {
                    // Subir la nueva
                    finalImagenUrl = subirImagen(nuevaImagenUri)
                    
                    // Borrar la vieja si existía
                    if (!oldImagenUrl.isNullOrEmpty()) {
                        try {
                            val fileName = oldImagenUrl.substringAfterLast("/")
                            supabase.storage.from("fotos_equipos").delete(fileName)
                        } catch (e: Exception) {
                            Log.e("Storage", "No se pudo borrar imagen vieja: ${e.message}")
                        }
                    }
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())
                
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
                    "imagen_url" to finalImagenUrl,
                    "fecha_modificacion" to now,
                    "modificado_por_modelo" to android.os.Build.MODEL,
                    "modificado_por_nombre" to (currentUserPerfil?.nombre ?: "Admin")
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
                // Actualización lógica en DB (mantener la imagen por si se restaura, o borrarla si decides)
                // Para Die Cut, solemos borrar la imagen para ahorrar espacio en borrado lógico
                if (!equipo.imagenUrl.isNullOrEmpty()) {
                    try {
                        val fileName = equipo.imagenUrl.substringAfterLast("/")
                        supabase.storage.from("fotos_equipos").delete(fileName)
                    } catch (e: Exception) {}
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val now = sdf.format(Date())
                
                supabase.from("equipos").update(
                    mapOf(
                        "deleted_at" to now,
                        "imagen_url" to null,
                        "modificado_por_modelo" to android.os.Build.MODEL,
                        "modificado_por_nombre" to (currentUserPerfil?.nombre ?: "Admin")
                    )
                ) {
                    filter { eq("id", equipo.id!!) }
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
                        "modificado_por_nombre" to (currentUserPerfil?.nombre ?: "Admin")
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

                // 4. Verificar cierre de folio
                if (folioFinalizado.isNotEmpty()) {
                    val activos = supabase.from("prestamos").select {
                        filter {
                            eq("folio", folioFinalizado)
                            eq("estado", "activo")
                        }
                    }.decodeList<Prestamo>()

                    if (activos.isEmpty()) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, 15)
                        val expiraAt = sdf.format(calendar.time)
                        
                        supabase.from("prestamos").update(mapOf("firma_expira_at" to expiraAt)) {
                            filter { eq("folio", folioFinalizado) }
                        }
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "¡FOLIO $folioFinalizado CERRADO!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Recibido. Faltan ${activos.size} equipos en este folio.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                fetchEquipos()
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
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
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

    // --- AUTENTICACIÓN ---

    // Gestión de Usuarios (Admin)
    val todosLosPerfiles = mutableStateListOf<Perfil>()

    fun fetchTodosLosPerfiles() {
        viewModelScope.launch {
            isLoading = true
            try {
                val results = supabase.from("perfiles").select().decodeList<Perfil>()
                todosLosPerfiles.clear()
                todosLosPerfiles.addAll(results.sortedBy { it.nombre })
            } catch (e: Exception) {
                Log.e("Admin", "Error cargando perfiles: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun actualizarRolUsuario(idUsuario: String, nuevoRol: String) {
        viewModelScope.launch {
            try {
                supabase.from("perfiles").update(mapOf("rol" to nuevoRol)) {
                    filter { eq("id", idUsuario) }
                }
                // Si el usuario actualizado es el actual, refrescamos su estado local
                if (idUsuario == currentUserPerfil?.id) {
                    currentUserPerfil = currentUserPerfil?.copy(rol = nuevoRol)
                }
                fetchTodosLosPerfiles()
            } catch (e: Exception) {
                Log.e("Admin", "Error actualizando rol: ${e.message}")
            }
        }
    }

    fun eliminarUsuario(idUsuario: String) {
        viewModelScope.launch {
            try {
                // Esto borra el perfil, pero el usuario seguiría existiendo en Auth. 
                // Por ahora borramos el perfil para quitarle acceso.
                supabase.from("perfiles").delete {
                    filter { eq("id", idUsuario) }
                }
                fetchTodosLosPerfiles()
            } catch (e: Exception) {
                Log.e("Admin", "Error eliminando perfil: ${e.message}")
            }
        }
    }

    fun registrarse(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            Log.d("Auth", "Intentando registrar: $email")
            try {
                val response = supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
                Log.d("Auth", "Registro exitoso: $response")
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al registrar: ${e.message}"
                Log.e("Auth", "Error fatal en registro: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun descargarContratoYCompartir(context: android.content.Context, prestamo: Prestamo) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Obtener equipos del folio
                val equipos = supabase.from("equipos").select {
                    filter { eq("id", prestamo.idEquipo!!) }
                }.decodeList<Equipo>()

                // 2. Descargar firma
                val firmaUrl = prestamo.firmaUrl
                var firmaBitmap: Bitmap? = null
                if (!firmaUrl.isNullOrEmpty()) {
                    val bytes = withContext(Dispatchers.IO) {
                        val url = java.net.URL(firmaUrl)
                        val connection = url.openConnection()
                        connection.doInput = true
                        connection.connect()
                        val input = connection.getInputStream()
                        BitmapFactory.decodeStream(input)
                    }
                    firmaBitmap = bytes
                }

                // 3. Generar PDF
                val file = ContratoGenerator.generarContratoPDF(context, prestamo, equipos, firmaBitmap)
                
                if (file != null) {
                    val uri = FileProvider.getUriForFile(context, "com.example.inventario.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir Contrato"))
                }
            } catch (e: Exception) {
                Log.e("PDF", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun iniciarSesion(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val cleanEmail = email.trim().lowercase()
            val cleanPass = pass.trim()
            try {
                supabase.auth.signInWith(Email) {
                    this.email = cleanEmail
                    this.password = cleanPass
                }
                
                // Esperar un momento para que la sesión se asiente
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    val perfil = supabase.from("perfiles").select {
                        filter { eq("id", user.id) }
                    }.decodeSingleOrNull<Perfil>()
                    
                    currentUserPerfil = perfil ?: Perfil(id = user.id, nombre = email.substringBefore("@"), rol = "USER")
                    onSuccess()
                }
            } catch (e: Exception) {
                errorMessage = "Error de acceso: ${e.message}"
                Log.e("Auth", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                currentUserPerfil = null
                onSuccess()
            } catch (e: Exception) {
                Log.e("Auth", "Error al salir: ${e.message}")
            }
        }
    }

    fun verificarSesion() {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    val perfil = supabase.from("perfiles").select {
                        filter { eq("id", user.id) }
                    }.decodeSingleOrNull<Perfil>()
                    
                    currentUserPerfil = perfil ?: Perfil(id = user.id, nombre = user.email?.substringBefore("@"), rol = "USER")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Sesión no encontrada")
            } finally {
                sessionInitialized = true
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                val config = supabase.from("app_config")
                    .select() {
                        order("latest_version_code", Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<AppConfig>()
                
                if (config != null && config.latest_version_code > BuildConfig.VERSION_CODE) {
                    appUpdateConfig = config
                    showUpdateDialog = true
                }
            } catch (e: Exception) {
                Log.e("Update", "Error al verificar versión: ${e.message}")
            }
        }
    }
}
