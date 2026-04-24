package com.example.inventario

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.inventario.model.Prestamo
import io.github.jan.supabase.postgrest.from
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.inventario.data.supabase
import com.example.inventario.model.Equipo
import com.example.inventario.ui.InventarioViewModel
import com.example.inventario.ui.theme.InventarioTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.snapshots.SnapshotStateList

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventarioTheme {
                val viewModel: InventarioViewModel = viewModel()
                var currentScreen by rememberSaveable { mutableStateOf("home") }

                when (currentScreen) {
                    "home" -> HomeScreen(
                        onNavigateToInventario = { currentScreen = "inventario" },
                        onNavigateToPrestamos = { currentScreen = "prestamos" }
                    )
                    "inventario" -> InventarioScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "prestamos" -> PrestamosScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToInventario: () -> Unit, onNavigateToPrestamos: () -> Unit) {
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Menú Principal", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar Aplicación")
                    }
                }
            )
        }
    ) { innerPadding ->
        // ... (contenido del menú igual)
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToInventario,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("GESTIÓN DE INVENTARIO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateToPrestamos,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("CONTROL DE PRÉSTAMOS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar Aplicación") },
            text = { Text("¿Estás seguro de que deseas salir del sistema?") },
            confirmButton = {
                Button(onClick = { (context as? Activity)?.finish() }) {
                    Text("SÍ, SALIR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(viewModel: InventarioViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var showForm by rememberSaveable { mutableStateOf(false) }
    var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.fetchEquipos()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (viewModel.isTrashMode) "Papelera de Reciclaje" else "Inventario de Equipos", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (viewModel.isTrashMode) Color(0xFF424242) else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (viewModel.isTrashMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleTrashMode() }) {
                        Icon(
                            imageVector = if (viewModel.isTrashMode) Icons.Default.Inventory else Icons.Default.Delete,
                            contentDescription = "Cambiar modo",
                            tint = if (viewModel.isTrashMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!viewModel.isTrashMode) {
                FloatingActionButton(onClick = { 
                    equipoAEditar = null
                    showForm = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Equipo")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Cuadro de búsqueda
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(if (viewModel.isTrashMode) "Buscar en Papelera..." else "Buscar por No. Inventario...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (viewModel.errorMessage != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error al cargar datos", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(viewModel.errorMessage!!, modifier = Modifier.padding(top = 8.dp))
                        Button(onClick = { viewModel.fetchEquipos() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Reintentar")
                        }
                    }
                } else {
                    if (viewModel.filteredEquipos.isEmpty()) {
                        Text(
                            text = if (viewModel.isTrashMode) "La papelera está vacía" else "No hay equipos registrados",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.filteredEquipos) { equipo ->
                                EquipoCard(
                                    equipo = equipo,
                                    onEdit = {
                                        equipoAEditar = equipo
                                        showForm = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showForm) {
            FormularioEquipo(
                viewModel = viewModel,
                equipoExistente = equipoAEditar,
                onDismiss = { 
                    showForm = false
                    equipoAEditar = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamosScreen(viewModel: InventarioViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        viewModel.fetchEquipos()
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Control de Préstamos", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("PRESTAR") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("RECIBIR") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("PENDIENTES") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> SalidaEquiposView(viewModel)
                1 -> RetornoEquiposView(viewModel)
                2 -> PendientesView(viewModel)
            }
        }
    }
}

@Composable
fun PendientesView(viewModel: InventarioViewModel) {
    // Agrupamos los préstamos por nombre del comodatario
    val prestamosPorPersona = remember(viewModel.prestamosActivos.size) {
        viewModel.prestamosActivos.groupBy { it.nombreComodatario ?: "SIN NOMBRE" }
    }

    if (prestamosPorPersona.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay préstamos activos en este momento", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            prestamosPorPersona.forEach { (nombre, prestamos) ->
                item {
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prestamos.forEach { prestamo ->
                            // Buscamos los datos del equipo en la lista de equipos del viewModel
                            val equipo = viewModel.equiposPrestados.find { it.id == prestamo.idEquipo }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Inv: ${equipo?.noInventario ?: "N/A"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${equipo?.nombre ?: "Equipo desconocido"} - Folio: ${prestamo.folio}",
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "En resguardo desde: ${prestamo.fechaPrestamo?.substringBefore("T")}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalidaEquiposView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showLoanForm by remember { mutableStateOf(false) }
    
    val filteredDisponibles by remember(searchQuery, viewModel.equiposDisponiblesParaPrestamo) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                viewModel.equiposDisponiblesParaPrestamo
            } else {
                viewModel.equiposDisponiblesParaPrestamo.filter { 
                    it.noInventario?.contains(searchQuery, ignoreCase = true) == true 
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar No. Inventario para préstamo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (filteredDisponibles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isEmpty()) "No hay equipos funcionales disponibles" 
                        else "No se encontró el equipo",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDisponibles) { equipo ->
                        val isSelected = viewModel.equiposSeleccionados.contains(equipo)
                        EquipoSeleccionCard(
                            equipo = equipo,
                            isSelected = isSelected,
                            onSelect = { viewModel.toggleSeleccion(equipo) }
                        )
                    }
                }
            }
        }

        if (viewModel.equiposSeleccionados.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { showLoanForm = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                icon = { Icon(Icons.Default.Check, null) },
                text = { Text("CONTINUAR (${viewModel.equiposSeleccionados.size})") }
            )
        }
    }

    if (showLoanForm) {
        FormularioResguardo(
            viewModel = viewModel,
            onDismiss = { showLoanForm = false },
            onSuccess = {
                showLoanForm = false
            }
        )
    }
}

@Composable
fun RetornoEquiposView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showOCR by remember { mutableStateOf(false) }
    var equipoSeleccionado by remember { mutableStateOf<Equipo?>(null) }
    var showReturnConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Lista de coincidencias en tiempo real
    val coincidencias by remember(searchQuery, viewModel.equiposPrestados) {
        derivedStateOf {
            if (searchQuery.length >= 1) {
                viewModel.equiposPrestados.filter { 
                    it.noInventario?.contains(searchQuery, ignoreCase = true) == true 
                }
            } else emptyList()
        }
    }

    // Información del préstamo vinculado al equipo seleccionado
    var prestamoInfo by remember { mutableStateOf<Prestamo?>(null) }
    
    LaunchedEffect(equipoSeleccionado) {
        equipoSeleccionado?.let { equipo ->
            try {
                val result = supabase.from("prestamos")
                    .select() {
                        filter {
                            eq("id_equipo", equipo.id!!)
                            eq("estado", "activo")
                        }
                        order("fecha_prestamo", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<Prestamo>()
                prestamoInfo = result
            } catch (e: Exception) {
                prestamoInfo = null
            }
        } ?: run {
            prestamoInfo = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it.uppercase()
                // Si borra el texto, deseleccionamos el equipo
                if (it.isEmpty()) equipoSeleccionado = null
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Escanear o escribir No. Inventario...") },
            leadingIcon = { 
                IconButton(onClick = { showOCR = true }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "OCR")
                }
            },
            trailingIcon = { 
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        equipoSeleccionado = null
                    }) { 
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar") 
                    } 
                } 
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            if (equipoSeleccionado != null) {
                // Muestra la tarjeta del equipo seleccionado para devolución
                Card(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("EQUIPO SELECCIONADO", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text("No. Inventario: ${equipoSeleccionado!!.noInventario}", fontWeight = FontWeight.Bold)
                        Text("Nombre: ${equipoSeleccionado!!.nombre}")
                        Text("Marca/Modelo: ${equipoSeleccionado!!.marca} ${equipoSeleccionado!!.modelo}")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("DETALLES DEL RESGUARDO", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        
                        if (prestamoInfo != null) {
                            Text("En posesión de: ${prestamoInfo!!.nombreComodatario}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Folio: ${prestamoInfo!!.folio}")
                            Text("Desde: ${prestamoInfo!!.fechaPrestamo?.substringBefore("T") ?: "N/A"}")
                        } else {
                            Text("Cargando detalles del préstamo...", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showReturnConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
                            else Text("RECIBIR EQUIPO Y LIBERAR", fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(
                            onClick = { equipoSeleccionado = null },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("CANCELAR SELECCIÓN")
                        }
                    }
                }
            } else if (coincidencias.isNotEmpty()) {
                // Muestra la lista de coincidencias mientras escribe
                Column {
                    Text("Coincidencias encontradas:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(coincidencias) { equipo ->
                            OutlinedCard(
                                onClick = { 
                                    equipoSeleccionado = equipo
                                    searchQuery = equipo.noInventario ?: ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Inventory, null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(equipo.noInventario ?: "", fontWeight = FontWeight.Bold)
                                        Text("${equipo.nombre} - ${equipo.marca}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text(
                        if (searchQuery.isEmpty()) "Ingresa el No. de Inventario para devolver" 
                        else "No se encontraron coincidencias en equipos prestados", 
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showReturnConfirmDialog && equipoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showReturnConfirmDialog = false },
            title = { Text("¿Confirmar recepción?") },
            text = { 
                Text("¿Estás seguro de que deseas recibir el equipo ${equipoSeleccionado!!.noInventario}? El equipo volverá a estar disponible en el inventario.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReturnConfirmDialog = false
                        coroutineScope.launch {
                            viewModel.registrarDevolucion(equipoSeleccionado!!, prestamoInfo, context) {
                                searchQuery = ""
                                equipoSeleccionado = null
                            }
                        }
                    }
                ) {
                    Text("SÍ, RECIBIR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReturnConfirmDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    if (showOCR) {
        CameraOCRDialog(
            onResult = { 
                searchQuery = it.uppercase()
                showOCR = false
            },
            onDismiss = { showOCR = false }
        )
    }
}



@Composable
fun EquipoSeleccionCard(equipo: Equipo, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(equipo.noInventario ?: "S/N", fontWeight = FontWeight.Bold)
                Text(equipo.nombre ?: "Sin nombre", style = MaterialTheme.typography.bodyMedium)
                Text("${equipo.marca} ${equipo.modelo}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun FormularioResguardo(
    viewModel: InventarioViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var nombreComodatario by rememberSaveable { mutableStateOf("") }
    var points = remember { mutableStateListOf<Offset>() }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Detalle del Resguardo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Equipos seleccionados:", fontWeight = FontWeight.Bold)
                viewModel.equiposSeleccionados.forEach {
                    Text("• ${it.noInventario} - ${it.nombre}", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownSugerencias(
                    value = nombreComodatario,
                    label = "Nombre del Comodatario",
                    options = viewModel.nombresConPrestamosActivos,
                    onValueChange = { nombreComodatario = it.uppercase() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Firma de Conformidad:", fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    SignatureCanvas(
                        points = points,
                        onPointsChange = { points.add(it) }
                    )
                    
                    TextButton(
                        onClick = { points.clear() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("LIMPIAR", color = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (nombreComodatario.isNotBlank() && points.isNotEmpty()) {
                            coroutineScope.launch {
                                // Generar firma con marcas de agua
                                val bitmap = generateSignatureBitmap(
                                    points = points,
                                    nombre = nombreComodatario,
                                    equipos = viewModel.equiposSeleccionados
                                )
                                val bos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos)
                                val firmaBytes = bos.toByteArray()

                                viewModel.realizarPrestamo(
                                    nombre = nombreComodatario,
                                    firmaBytes = firmaBytes,
                                    dispositivoModelo = android.os.Build.MODEL,
                                    dispositivoNombre = android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido",
                                    onSuccess = {
                                        Toast.makeText(context, "Préstamo registrado y correo enviado", Toast.LENGTH_LONG).show()
                                        onSuccess()
                                    }
                                )
                            }
                        } else {
                            val motivo = if (nombreComodatario.isBlank()) "nombre" else "firma"
                            Toast.makeText(context, "Falta $motivo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
                    else Text("FINALIZAR PRÉSTAMO Y ENVIAR CORREO")
                }
            }
        }
    }
}

@Composable
fun SignatureCanvas(points: SnapshotStateList<Offset>, onPointsChange: (Offset) -> Unit) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onPointsChange(offset)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset: Offset -> onPointsChange(offset) },
                    onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, _: Offset ->
                        onPointsChange(change.position)
                    },
                    onDragEnd = { onPointsChange(Offset.Unspecified) }
                )
            }
    ) {
        val path = Path()
        var isFirst = true
        points.forEach { point ->
            if (point == Offset.Unspecified) {
                isFirst = true
            } else {
                if (isFirst) {
                    path.moveTo(point.x, point.y)
                    isFirst = false
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
        }
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

fun generateSignatureBitmap(
    points: List<Offset>,
    nombre: String,
    equipos: List<Equipo>
): Bitmap {
    val width = 800
    val height = 600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    // Dibujar trazo
    val path = android.graphics.Path()
    var isFirst = true
    points.forEach { point ->
        if (point == Offset.Unspecified) {
            isFirst = true
        } else {
            if (isFirst) {
                path.moveTo(point.x * (width / 1000f), point.y * (height / 1000f)) // Escalar si es necesario
                // Ajuste: para simplificar, asumimos que los puntos ya vienen en escala o el lienzo es fijo
                path.moveTo(point.x, point.y)
                isFirst = false
            } else {
                path.lineTo(point.x, point.y)
            }
        }
    }
    canvas.drawPath(path, paint)

    // Dibujar Marcas de Agua (Texto)
    val textPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 24f
        isAntiAlias = true
    }
    
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date())
    
    canvas.drawText("Comodatario: $nombre", 20f, height - 80f, textPaint)
    canvas.drawText("Fecha: $dateStr", 20f, height - 50f, textPaint)
    
    val equiposStr = "Equipos: " + equipos.joinToString(", ") { it.noInventario ?: "" }
    canvas.drawText(equiposStr, 20f, height - 20f, textPaint)

    return bitmap
}




@Composable
fun FormularioEquipo(
    viewModel: InventarioViewModel, 
    equipoExistente: Equipo? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isEditMode = equipoExistente != null

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var nombre by rememberSaveable { mutableStateOf(equipoExistente?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(equipoExistente?.descripcion ?: "") }
    var categoria by rememberSaveable { mutableStateOf(equipoExistente?.categoria ?: "") }
    var marca by rememberSaveable { mutableStateOf(equipoExistente?.marca ?: "") }
    var modelo by rememberSaveable { mutableStateOf(equipoExistente?.modelo ?: "") }
    var estado by rememberSaveable { mutableStateOf(equipoExistente?.estado ?: "FUNCIONAL") }
    var numeroSerie by rememberSaveable { mutableStateOf(equipoExistente?.numeroSerie ?: "") }
    var noInventario by rememberSaveable { mutableStateOf(equipoExistente?.noInventario ?: "") }
    var numerotag by rememberSaveable { mutableStateOf(equipoExistente?.numerotag ?: "") }
    var imageUriString by rememberSaveable { mutableStateOf<String?>(equipoExistente?.imagenUrl) }
    val imageUri = if (imageUriString?.startsWith("http") == true) null else imageUriString?.let { Uri.parse(it) }
    
    var showCameraOCR by rememberSaveable { mutableStateOf(false) }
    var ocrTargetField by rememberSaveable { mutableStateOf("") } // "serie" o "tag"
    var showCameraPhoto by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Estados para los menús desplegables
    var expandedEstado by remember { mutableStateOf(false) }
    val estados = listOf("FUNCIONAL", "OBSOLETO", "DESCOMPUESTO", "PRESTADO")

    val hasChanges = remember(
        nombre, descripcion, categoria, marca, modelo, estado, numeroSerie, noInventario, numerotag, imageUriString
    ) {
        nombre != (equipoExistente?.nombre ?: "") ||
        descripcion != (equipoExistente?.descripcion ?: "") ||
        categoria != (equipoExistente?.categoria ?: "") ||
        marca != (equipoExistente?.marca ?: "") ||
        modelo != (equipoExistente?.modelo ?: "") ||
        estado != (equipoExistente?.estado ?: "FUNCIONAL") ||
        numeroSerie != (equipoExistente?.numeroSerie ?: "") ||
        noInventario != (equipoExistente?.noInventario ?: "") ||
        numerotag != (equipoExistente?.numerotag ?: "") ||
        imageUriString != (equipoExistente?.imagenUrl)
    }

    Dialog(
        onDismissRequest = { if (hasChanges) showDiscardDialog = true else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (hasChanges) showDiscardDialog = true else onDismiss() }) { 
                        Icon(Icons.Default.ArrowBack, null) 
                    }
                    Text(
                        if (isEditMode) "Editar Equipo" else "Nuevo Equipo", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Foto del equipo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showCameraPhoto = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!imageUriString.isNullOrEmpty()) {
                        AsyncImage(
                            model = imageUriString,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(48.dp))
                            Text("Tomar foto del equipo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // No. Inventario (Validación 8 caracteres: 3 letras + 5 números)
                val isDuplicated = viewModel.isNoInventarioDuplicado(noInventario, equipoExistente?.id)
                OutlinedTextField(
                    value = noInventario,
                    onValueChange = { 
                        val input = it.uppercase().take(8)
                        noInventario = input
                        if (input.length == 3 && !isEditMode) {
                            noInventario = viewModel.sugerirSiguienteNumero(input)
                        }
                    },
                    label = { Text("No. Inventario (Ej: ABC00001)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = (noInventario.length != 8 && noInventario.isNotEmpty()) || isDuplicated,
                    supportingText = {
                        if (isDuplicated) Text("¡Este número de inventario ya existe!", color = Color.Red)
                        else if (noInventario.length > 0 && noInventario.length != 8) Text("Debe tener 8 caracteres")
                    }
                )

                CampoTextoMayusculas(nombre, "Nombre del Equipo") { nombre = it }
                CampoTextoMayusculas(descripcion, "Descripción") { descripcion = it }
                
                // Categoría (Sugerencias)
                ExposedDropdownSugerencias(
                    value = categoria,
                    label = "Categoría",
                    options = viewModel.categoriasExistentes,
                    onValueChange = { categoria = it.uppercase() }
                )

                ExposedDropdownSugerencias(
                    value = marca,
                    label = "Marca",
                    options = viewModel.marcasExistentes,
                    onValueChange = { marca = it.uppercase() }
                )

                ExposedDropdownSugerencias(
                    value = modelo,
                    label = "Modelo",
                    options = viewModel.modelosExistentes,
                    onValueChange = { modelo = it.uppercase() }
                )

                // Estado (Combo fijo)
                Box {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedEstado = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                        estados.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = { estado = opt; expandedEstado = false }
                            )
                        }
                    }
                }

                // Número Tag con OCR
                CampoConOCR(
                    value = numerotag,
                    label = "Número Tag",
                    onValueChange = { numerotag = it.uppercase() },
                    onOCRClick = { ocrTargetField = "tag"; showCameraOCR = true }
                )

                // Número de Serie con OCR
                CampoConOCR(
                    value = numeroSerie,
                    label = "Número de Serie",
                    onValueChange = { numeroSerie = it.uppercase() },
                    onOCRClick = { ocrTargetField = "serie"; showCameraOCR = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (noInventario.length == 8 && !isDuplicated) {
                            coroutineScope.launch {
                                // Si hay una nueva imagen (uri local), la subimos
                                val finalUrl = if (imageUri != null) {
                                    subirImagen(context, imageUri)
                                } else {
                                    imageUriString // Mantenemos la URL actual si es de internet
                                }

                                val equipoData = Equipo(
                                    id = equipoExistente?.id,
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    categoria = categoria,
                                    marca = marca,
                                    modelo = modelo,
                                    estado = estado,
                                    numeroSerie = numeroSerie,
                                    numerotag = numerotag,
                                    noInventario = noInventario,
                                    imagenUrl = finalUrl,
                                    creadoPorModelo = equipoExistente?.creadoPorModelo ?: android.os.Build.MODEL,
                                    creadoPorNombre = equipoExistente?.creadoPorNombre ?: (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido"),
                                    modificadoPorModelo = android.os.Build.MODEL,
                                    modificadoPorNombre = android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido"
                                )

                                if (isEditMode) {
                                    viewModel.actualizarEquipo(equipoData, equipoExistente?.imagenUrl, imageUri) {
                                        onDismiss()
                                        Toast.makeText(context, "Actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    viewModel.guardarEquipo(equipoData, null) {
                                        onDismiss()
                                        Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            val msg = if (isDuplicated) "Número de inventario ya existe" else "Revisa los campos obligatorios"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
                    else Text(if (isEditMode) "ACTUALIZAR DATOS" else "GUARDAR EQUIPO")
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("¿Descartar cambios?") },
            text = { Text("Tienes cambios sin guardar. ¿Deseas salir de todas formas?") },
            confirmButton = {
                TextButton(onClick = { 
                    showDiscardDialog = false
                    onDismiss() 
                }) { Text("SÍ, DESCARTAR", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("CONTINUAR EDITANDO") }
            }
        )
    }

    if (showCameraOCR) {
        CameraOCRDialog(
            onResult = { result ->
                if (ocrTargetField == "serie") numeroSerie = result.uppercase()
                else if (ocrTargetField == "tag") numerotag = result.uppercase()
                showCameraOCR = false
            },
            onDismiss = { showCameraOCR = false }
        )
    }

    if (showCameraPhoto) {
        CameraPhotoDialog(
            onCaptured = { uri ->
                imageUriString = uri.toString()
                showCameraPhoto = false
            },
            onDismiss = { showCameraPhoto = false }
        )
    }
}

@Composable
fun CampoTextoMayusculas(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.uppercase()) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun CampoConOCR(value: String, label: String, onValueChange: (String) -> Unit, onOCRClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.uppercase()) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        trailingIcon = {
            IconButton(onClick = onOCRClick) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "OCR")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownSugerencias(value: String, label: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }
        )
        if (expanded && options.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                options.filter { it.contains(value, ignoreCase = true) }.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onValueChange(opt); expanded = false }
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraOCRDialog(onResult: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    var detectedOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSelecting by remember { mutableStateOf(false) }
    var currentLiveText by remember { mutableStateOf("") }
    
    DisposableEffect(Unit) {
        onDispose {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("OCR", "Error unbinding camera", e)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(if (isSelecting) MaterialTheme.colorScheme.background else Color.Black)) {
            if (!isSelecting) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            
                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                        @androidx.camera.core.ExperimentalGetImage
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                            recognizer.process(image)
                                                .addOnSuccessListener { visionText ->
                                                    // Filtrar bloques en el ROI central
                                                    val roiBlocks = visionText.textBlocks.filter { block ->
                                                        val box = block.boundingBox ?: return@filter false
                                                        val centerX = box.centerX()
                                                        val centerY = box.centerY()
                                                        
                                                        centerX > image.width * 0.20 && centerX < image.width * 0.80 &&
                                                        centerY > image.height * 0.30 && centerY < image.height * 0.70
                                                    }
                                                    
                                                    // Guardamos todas las líneas de texto encontradas en el centro
                                                    val allStrings = roiBlocks.flatMap { block -> 
                                                        block.lines.map { it.text.trim().uppercase() } 
                                                    }.filter { it.length > 2 }.distinct()
                                                    
                                                    detectedOptions = allStrings
                                                    currentLiveText = allStrings.firstOrNull() ?: ""
                                                }
                                                .addOnCompleteListener { imageProxy.close() }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                            } catch (e: Exception) {
                                Log.e("OCR", "Binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )
                
                // Guía visual
                Box(
                    modifier = Modifier
                        .size(width = 320.dp, height = 140.dp)
                        .align(Alignment.Center)
                        .border(2.dp, if (detectedOptions.isNotEmpty()) Color.Green else Color.White, RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentLiveText.isNotEmpty()) {
                        Text(
                            "Detectado: $currentLiveText",
                            color = Color.White,
                            modifier = Modifier.background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp)).padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Button(
                        onClick = { if (detectedOptions.isNotEmpty()) isSelecting = true },
                        enabled = detectedOptions.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Text("ESCANEAR ÁREA", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Pantalla de selección de resultados
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Resultados Encontrados", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Toca el texto correcto para capturarlo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(detectedOptions) { text ->
                            OutlinedButton(
                                onClick = { onResult(text) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = { isSelecting = false }) {
                        Text("VOLVER A INTENTAR")
                    }
                }
            }
            
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = if (isSelecting) Color.Black else Color.White)
            }
        }
    }
}

@Composable
fun CameraPhotoDialog(onCaptured: (Uri) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = {
                    val file = java.io.File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    cameraController.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onCaptured(Uri.fromFile(file))
                            }
                            override fun onError(exc: ImageCaptureException) {
                                Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
                            }
                        }
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).size(80.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Icon(Icons.Default.Camera, null, modifier = Modifier.size(40.dp))
            }

            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

suspend fun subirImagen(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            // Optimización 1: Obtener dimensiones sin cargar el bitmap completo en memoria
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options)
            }
            
            // Optimización 2: Calcular el factor de escala (inSampleSize)
            // Limitamos la imagen a un máximo de 1200px para balancear calidad y memoria
            options.inSampleSize = calculateInSampleSize(options, 1200, 1200)
            options.inJustDecodeBounds = false
            
            // Optimización 3: Decodificar el bitmap con el tamaño reducido
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@withContext null

            val bos = ByteArrayOutputStream()
            // Compresión eficiente al 50%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
            val bytes = bos.toByteArray()
            
            // Liberar memoria del bitmap inmediatamente
            bitmap.recycle()
            
            val fileName = "equipo_${System.currentTimeMillis()}.jpg"
            supabase.storage.from("fotos_equipos").upload(fileName, bytes)
            
            val duration = System.currentTimeMillis() - startTime
            Log.d("Performance", "Imagen procesada y subida en ${duration}ms. Tamaño final: ${bytes.size / 1024}KB")
            
            // Obtener URL pública
            supabase.storage.from("fotos_equipos").publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("Performance", "Error en subirImagen: ${e.message}")
            null
        }
    }
}

/**
 * Calcula el factor de escala para reducir el uso de memoria al decodificar Bitmaps.
 */
fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EquipoCard(equipo: Equipo, onEdit: () -> Unit) {
    val viewModel: InventarioViewModel = viewModel()
    val context = LocalContext.current
    var showZoom by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showPermanentDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Click normal */ },
                onLongClick = { 
                    if (viewModel.isTrashMode) {
                        showRestoreDialog = true
                    } else {
                        showDeleteDialog = true 
                    }
                }
            )
            .border(
                width = if (viewModel.isTrashMode) 1.dp else 0.dp,
                color = if (viewModel.isTrashMode) Color.Red.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isTrashMode) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewModel.isTrashMode) "EQUIPO ELIMINADO" else "INFORMACIÓN DEL EQUIPO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (viewModel.isTrashMode) Color.Red else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                
                if (viewModel.isTrashMode) {
                    Row {
                        IconButton(onClick = { showRestoreDialog = true }) {
                            Icon(Icons.Default.RestoreFromTrash, "Restaurar", tint = Color.DarkGray)
                        }
                        IconButton(onClick = { showPermanentDeleteDialog = true }) {
                            Icon(Icons.Default.DeleteForever, "Borrar Permanente", tint = Color.Red)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Mantén presionado para borrar",
                            tint = Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            // ... (Imagen y campos igual)
            if (!equipo.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = equipo.imagenUrl,
                    contentDescription = "Imagen del equipo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { if (!viewModel.isTrashMode) showZoom = true },
                    contentScale = ContentScale.Crop
                )
            }
            // ... (campos de datos)

            CampoDato("No. Inventario:", equipo.noInventario)
            CampoDato("Nombre:", equipo.nombre)
            CampoDato("Descripción:", equipo.descripcion)
            CampoDato("Categoría:", equipo.categoria)
            CampoDato("Marca:", equipo.marca)
            CampoDato("Modelo:", equipo.modelo)
            CampoDato("Estado:", equipo.estado)
            CampoDato("Numero Tag:", equipo.numerotag)
            CampoDato("Número de Serie:", equipo.numeroSerie)
            CampoDato("Fecha Registro:", equipo.fechaRegistro)
            CampoDato("Fecha Modificación:", equipo.fechaModificacion)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Registro de Auditoría", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            CampoDato("Creado por (Modelo):", equipo.creadoPorModelo)
            CampoDato("Creado por (Nombre):", equipo.creadoPorNombre)
            CampoDato("Modif. por (Modelo):", equipo.modificadoPorModelo)
            CampoDato("Modif. por (Nombre):", equipo.modificadoPorNombre)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar equipo?") },
            text = { Text("Esta acción ocultará el equipo y liberará el espacio de su imagen. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarEquipoLogico(equipo, context) {
                            showDeleteDialog = false
                            Toast.makeText(context, "Equipo eliminado", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("¿Restaurar equipo?") },
            text = { Text("El equipo volverá a aparecer en la lista principal.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restaurarEquipo(equipo, context) {
                        showRestoreDialog = false
                        Toast.makeText(context, "Equipo restaurado", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("RESTAURAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    if (showPermanentDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = false },
            title = { Text("¿ELIMINAR DEFINITIVAMENTE?") },
            text = { Text("Esta acción es IRREVERSIBLE. El registro desaparecerá de la base de datos para siempre.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarFisico(equipo) {
                        showPermanentDeleteDialog = false
                        Toast.makeText(context, "Eliminado permanentemente", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("ELIMINAR PARA SIEMPRE", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPermanentDeleteDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    if (showZoom && !equipo.imagenUrl.isNullOrEmpty()) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale *= zoomChange
            offset += offsetChange
        }

        Dialog(
            onDismissRequest = { showZoom = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showZoom = false })
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = equipo.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = maxOf(1f, scale),
                            scaleY = maxOf(1f, scale),
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                        .pointerInput(Unit) {
                            // Este pointerInput vacío evita que el tap del fondo
                            // se active cuando tocas la imagen directamente
                            detectTapGestures(onTap = { /* No hacer nada al tocar la imagen */ })
                        },
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showZoom = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun CampoDato(titulo: String, valor: String?) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(130.dp),
                fontSize = 14.sp
            )
            Text(
                text = valor ?: "Sin datos",
                color = if (valor == null) Color.Gray else Color.Unspecified,
                fontSize = 14.sp
            )
        }
    }
}
