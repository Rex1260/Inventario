package com.example.inventario

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
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
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.example.inventario.model.*
import com.example.inventario.ui.InventarioViewModel
import com.example.inventario.ui.theme.InventarioTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

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
                    "home" -> HomeScreen(onNavigateToInventario = { currentScreen = "inventario" }, onNavigateToPrestamos = { currentScreen = "prestamos" })
                    "inventario" -> InventarioScreen(viewModel = viewModel, onBack = { currentScreen = "home" })
                    "prestamos" -> PrestamosScreen(viewModel = viewModel, onBack = { currentScreen = "home" })
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
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { CenterAlignedTopAppBar(title = { Text("Menú Principal", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { showExitDialog = true }) { Icon(Icons.Default.Close, "Cerrar") } }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onNavigateToInventario, modifier = Modifier.fillMaxWidth(0.8f).height(100.dp).padding(8.dp), shape = RoundedCornerShape(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Inventory, null, modifier = Modifier.size(32.dp)); Spacer(modifier = Modifier.width(16.dp)); Text("GESTIÓN DE INVENTARIO", fontSize = 18.sp, fontWeight = FontWeight.Bold) } }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToPrestamos, modifier = Modifier.fillMaxWidth(0.8f).height(100.dp).padding(8.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Filled.Assignment, null, modifier = Modifier.size(32.dp)); Spacer(modifier = Modifier.width(16.dp)); Text("CONTROL DE PRÉSTAMOS", fontSize = 18.sp, fontWeight = FontWeight.Bold) } }
        }
    }
    if (showExitDialog) { AlertDialog(onDismissRequest = { showExitDialog = false }, title = { Text("Cerrar") }, text = { Text("¿Deseas salir?") }, confirmButton = { Button(onClick = { (context as? Activity)?.finish() }) { Text("SÍ") } }, dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("NO") } }) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(viewModel: InventarioViewModel, onBack: () -> Unit) {
    var showForm by rememberSaveable { mutableStateOf(false) }; var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
    LaunchedEffect(Unit) { viewModel.fetchEquipos() }
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { CenterAlignedTopAppBar(title = { Text(if (viewModel.isTrashMode) "Papelera" else "Inventario", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = if (viewModel.isTrashMode) Color.DarkGray else MaterialTheme.colorScheme.primaryContainer), actions = { IconButton(onClick = { viewModel.toggleTrashMode() }) { Icon(if (viewModel.isTrashMode) Icons.Default.Inventory else Icons.Default.Delete, null) } }) }, floatingActionButton = { if (!viewModel.isTrashMode) { FloatingActionButton(onClick = { equipoAEditar = null; showForm = true }) { Icon(Icons.Default.Add, null) } } }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            OutlinedTextField(value = viewModel.searchQuery, onValueChange = { viewModel.searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Buscar No. Inv...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isLoading) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                else if (viewModel.errorMessage != null) { Text(viewModel.errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red) }
                else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(viewModel.filteredEquipos) { equipo -> EquipoCard(equipo = equipo, onEdit = { equipoAEditar = equipo; showForm = true }) } } }
            }
        }
        if (showForm) { FormularioEquipo(viewModel = viewModel, equipoExistente = equipoAEditar, onDismiss = { showForm = false; equipoAEditar = null }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamosScreen(viewModel: InventarioViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { viewModel.fetchEquipos() }
    Scaffold(topBar = { Column { CenterAlignedTopAppBar(title = { Text("Control de Préstamos", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar") } }); TabRow(selectedTabIndex = selectedTab) { Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("PRESTAR") }); Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("RECIBIR") }); Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("PENDIENTES") }); Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("HISTORIAL") }) } } }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> SalidaEquiposView(viewModel)
                1 -> RetornoEquiposView(viewModel)
                2 -> PendientesView(viewModel)
                3 -> HistorialView(viewModel)
            }
        }
    }
}

@Composable
fun SalidaEquiposView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }; var showLoanForm by remember { mutableStateOf(false) }
    val filteredDisponibles by remember(searchQuery, viewModel.equiposDisponiblesParaPrestamo) { derivedStateOf { if (searchQuery.isBlank()) viewModel.equiposDisponiblesParaPrestamo else viewModel.equiposDisponiblesParaPrestamo.filter { it.noInventario?.contains(searchQuery, ignoreCase = true) == true } } }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Buscar No. Inventario...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
            if (filteredDisponibles.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay equipos disponibles", color = Color.Gray) } }
            else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filteredDisponibles) { equipo -> val isSelected = viewModel.equiposSeleccionados.contains(equipo); EquipoSeleccionCard(equipo = equipo, isSelected = isSelected, onSelect = { viewModel.toggleSeleccion(equipo) }) } } }
        }
        if (viewModel.equiposSeleccionados.isNotEmpty()) { ExtendedFloatingActionButton(onClick = { showLoanForm = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), icon = { Icon(Icons.Default.Check, null) }, text = { Text("CONTINUAR (${viewModel.equiposSeleccionados.size})") }) }
    }
    if (showLoanForm) { FormularioResguardo(viewModel = viewModel, onDismiss = { showLoanForm = false }, onSuccess = { showLoanForm = false }) }
}

@Composable
fun RetornoEquiposView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }; var showOCR by remember { mutableStateOf(false) }; var equipoSeleccionado by remember { mutableStateOf<Equipo?>(null) }; var showReturnChoiceDialog by remember { mutableStateOf(false) }; var showDamageForm by remember { mutableStateOf(false) }
    val context = LocalContext.current; val coroutineScope = rememberCoroutineScope()
    val coincidencias by remember(searchQuery, viewModel.equiposPrestados) { derivedStateOf { if (searchQuery.length >= 1) viewModel.equiposPrestados.filter { it.noInventario?.contains(searchQuery, ignoreCase = true) == true } else emptyList() } }
    var prestamoInfo by remember { mutableStateOf<Prestamo?>(null) }
    LaunchedEffect(equipoSeleccionado) {
        equipoSeleccionado?.let { equipo -> try { val result = supabase.from("prestamos").select() { filter { eq("id_equipo", equipo.id!!); eq("estado", "activo") }; order("fecha_prestamo", io.github.jan.supabase.postgrest.query.Order.DESCENDING); limit(1) }.decodeSingleOrNull<Prestamo>(); prestamoInfo = result } catch (e: Exception) { prestamoInfo = null } } ?: run { prestamoInfo = null }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it.uppercase(); if (it.isEmpty()) equipoSeleccionado = null }, modifier = Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Escanear o escribir No. Inv...") }, leadingIcon = { IconButton(onClick = { showOCR = true }) { Icon(Icons.Default.QrCodeScanner, null) } }, trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = ""; equipoSeleccionado = null }) { Icon(Icons.Default.Clear, null) } }, singleLine = true)
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            if (equipoSeleccionado != null) {
                Card(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("EQUIPO SELECCIONADO", fontWeight = FontWeight.Bold); HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)); Text("No. Inv: ${equipoSeleccionado!!.noInventario}"); Text("Nombre: ${equipoSeleccionado!!.nombre}"); Spacer(modifier = Modifier.height(16.dp)); if (prestamoInfo != null) { Text("Comodatario: ${prestamoInfo!!.nombreComodatario}", fontWeight = FontWeight.Bold); Text("Desde: ${prestamoInfo!!.fechaPrestamo?.substringBefore("T")}") }
                        Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showReturnChoiceDialog = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("RECIBIR EQUIPO") }; TextButton(onClick = { equipoSeleccionado = null }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("CANCELAR") }
                    }
                }
            } else if (coincidencias.isNotEmpty()) { LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(coincidencias) { equipo -> OutlinedCard(onClick = { equipoSeleccionado = equipo; searchQuery = equipo.noInventario ?: "" }, modifier = Modifier.fillMaxWidth()) { Row(modifier = Modifier.padding(12.dp)) { Column { Text(equipo.noInventario ?: "", fontWeight = FontWeight.Bold); Text(equipo.nombre ?: "") } } } } } }
            else { Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = Color.LightGray); Text("Ingresa el No. de Inventario", color = Color.Gray) } }
        }
    }
    if (showReturnChoiceDialog && equipoSeleccionado != null) { AlertDialog(onDismissRequest = { showReturnChoiceDialog = false }, title = { Text("Estado") }, text = { Text("¿En qué condiciones se recibe?") }, confirmButton = { Button(onClick = { showReturnChoiceDialog = false; coroutineScope.launch { viewModel.registrarDevolucion(equipoSeleccionado!!, prestamoInfo, context) { searchQuery = ""; equipoSeleccionado = null } } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("BIEN") } }, dismissButton = { Button(onClick = { showReturnChoiceDialog = false; showDamageForm = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("DAÑADO") } }) }
    if (showDamageForm && equipoSeleccionado != null) { FormularioDano(viewModel = viewModel, equipo = equipoSeleccionado!!, prestamo = prestamoInfo, onDismiss = { showDamageForm = false }, onSuccess = { showDamageForm = false; searchQuery = ""; equipoSeleccionado = null }) }
    if (showOCR) { CameraOCRDialog(onResult = { searchQuery = it.uppercase(); showOCR = false }, onDismiss = { showOCR = false }) }
}

@Composable
fun PendientesView(viewModel: InventarioViewModel) {
    val prestamosPorPersona = remember(viewModel.prestamosActivos.size) { viewModel.prestamosActivos.groupBy { it.nombreComodatario ?: "SIN NOMBRE" } }
    if (prestamosPorPersona.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay préstamos activos", color = Color.Gray) } }
    else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { prestamosPorPersona.forEach { (nombre, prestamos) -> item { Text(text = nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { prestamos.forEach { prestamo -> val equipo = viewModel.equiposPrestados.find { it.id == prestamo.idEquipo }; Card(modifier = Modifier.fillMaxWidth()) { Row(modifier = Modifier.padding(12.dp)) { Column { Text("Inv: ${equipo?.noInventario ?: "N/A"}", fontWeight = FontWeight.Bold); Text("${equipo?.nombre ?: "N/A"} - Folio: ${prestamo.folio}"); Text("Desde: ${prestamo.fechaPrestamo?.substringBefore("T")}", fontSize = 11.sp, color = Color.Gray) } } } } }; HorizontalDivider(modifier = Modifier.padding(top = 16.dp)) } } } }
}

@Composable
fun HistorialView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredHistorial by remember(searchQuery, viewModel.historialPrestamos.size) { derivedStateOf { if (searchQuery.isBlank()) viewModel.historialPrestamos else viewModel.historialPrestamos.filter { p -> val equipo = viewModel.filteredEquipos.find { it.id == p.idEquipo }; p.folio?.contains(searchQuery, ignoreCase = true) == true || p.nombreComodatario?.contains(searchQuery, ignoreCase = true) == true || equipo?.noInventario?.contains(searchQuery, ignoreCase = true) == true } } }
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Buscar en historial...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        if (filteredHistorial.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay registros", color = Color.Gray) } }
        else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filteredHistorial) { prestamo -> val equipo = viewModel.filteredEquipos.find { it.id == prestamo.idEquipo }; Card(modifier = Modifier.fillMaxWidth()) { Column(modifier = Modifier.padding(16.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("FOLIO: ${prestamo.folio}", fontWeight = FontWeight.Bold); Text("DEVUELTO", fontSize = 10.sp, color = Color.Green) }; HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)); Text("Equipo: ${equipo?.noInventario} - ${equipo?.nombre}"); Text("Comodatario: ${prestamo.nombreComodatario}"); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Salida:", fontSize = 11.sp); Text(prestamo.fechaPrestamo?.substringBefore("T") ?: "") }; Column { Text("Regreso:", fontSize = 11.sp); Text(prestamo.fechaDevolucion?.substringBefore("T") ?: "") } }; if (!prestamo.firmaUrl.isNullOrEmpty()) { var showFirma by remember { mutableStateOf(false) }; TextButton(onClick = { showFirma = true }) { Text("VER FIRMA") }; if (showFirma) { Dialog(onDismissRequest = { showFirma = false }) { Card(modifier = Modifier.fillMaxWidth().height(300.dp)) { Box { AsyncImage(model = prestamo.firmaUrl, contentDescription = null, modifier = Modifier.fillMaxSize()); IconButton(onClick = { showFirma = false }, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Close, null) } } } } } } } } } } }
    }
}

@Composable
fun FormularioResguardo(viewModel: InventarioViewModel, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current; var searchQuery by rememberSaveable { mutableStateOf("") }; val points = remember { mutableStateListOf<Offset>() }; val coroutineScope = rememberCoroutineScope()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }; Text("Detalle del Resguardo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(24.dp)); Text("Equipos:", fontWeight = FontWeight.Bold); viewModel.equiposSeleccionados.forEach { Text("• ${it.noInventario} - ${it.nombre}") }
                Spacer(modifier = Modifier.height(24.dp))
                if (viewModel.usuarioSeleccionado == null) {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it; viewModel.buscarUsuarios(it) }, label = { Text("Buscar Comodatario") }, modifier = Modifier.fillMaxWidth())
                    if (viewModel.usuariosEncontrados.isNotEmpty()) { Card(modifier = Modifier.fillMaxWidth()) { Column { viewModel.usuariosEncontrados.forEach { usuario -> ListItem(headlineContent = { Text(usuario.nombre ?: "") }, modifier = Modifier.clickable { viewModel.usuarioSeleccionado = usuario }) } } } }
                } else { Card(modifier = Modifier.fillMaxWidth()) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text(viewModel.usuarioSeleccionado!!.nombre ?: "", fontWeight = FontWeight.Bold); Text(viewModel.usuarioSeleccionado!!.email ?: "") }; IconButton(onClick = { viewModel.usuarioSeleccionado = null }) { Icon(Icons.Default.Edit, null) } } } }
                Spacer(modifier = Modifier.height(24.dp)); Text("Firma:", fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, Color.Gray).background(Color.White)) { SignatureCanvas(points = points, onPointsChange = { points.add(it) }); TextButton(onClick = { points.clear() }, modifier = Modifier.align(Alignment.TopEnd)) { Text("LIMPIAR", color = Color.Red) } }
                Spacer(modifier = Modifier.height(32.dp)); Button(onClick = { val usuario = viewModel.usuarioSeleccionado; if (usuario != null && points.isNotEmpty()) { coroutineScope.launch { val bitmap = generateSignatureBitmap(points, usuario.nombre ?: "", viewModel.equiposSeleccionados); val bos = ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos); viewModel.realizarPrestamo(usuario.nombre ?: "", usuario.id, bos.toByteArray(), android.os.Build.MODEL, "Device", onSuccess = { onSuccess() }) } } }, modifier = Modifier.fillMaxWidth()) { Text("FINALIZAR") }
            }
        }
    }
}

@Composable
fun FormularioDano(viewModel: InventarioViewModel, equipo: Equipo, prestamo: Prestamo?, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current; val coroutineScope = rememberCoroutineScope(); var gravedad by remember { mutableStateOf("leve") }; var descripcion by remember { mutableStateOf("") }; var imageUri by remember { mutableStateOf<Uri?>(null) }; var showCamera by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Reporte de Daño", fontWeight = FontWeight.Bold); Row { SeverityOption("Leve", Color.Green, gravedad == "leve") { gravedad = "leve" }; SeverityOption("Grave", Color.Red, gravedad == "grave") { gravedad = "grave" } }
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { showCamera = true }) { Text(if (imageUri != null) "CAMBIAR FOTO" else "TOMAR FOTO") }
                Button(onClick = { coroutineScope.launch { val url = if (imageUri != null) viewModel.subirImagenDano(context, imageUri!!) else null; viewModel.registrarDevolucion(equipo, prestamo, context, Dano(gravedad, descripcion, "", url)) { onSuccess() } } }, modifier = Modifier.fillMaxWidth()) { Text("ENVIAR") }
            }
        }
    }
    if (showCamera) { CameraPhotoDialog(onCaptured = { imageUri = it; showCamera = false }, onDismiss = { showCamera = false }) }
}

@Composable
fun SeverityOption(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) { FilterChip(selected = isSelected, onClick = onClick, label = { Text(label) }) }

@Composable
fun SignatureCanvas(points: SnapshotStateList<Offset>, onPointsChange: (Offset) -> Unit) {
    Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures(onDragStart = { onPointsChange(it) }, onDrag = { change, _ -> onPointsChange(change.position) }, onDragEnd = { onPointsChange(Offset.Unspecified) }) }) {
        val path = Path(); var first = true; points.forEach { if (it == Offset.Unspecified) first = true else { if (first) path.moveTo(it.x, it.y) else path.lineTo(it.x, it.y); first = false } }
        drawPath(path, Color.Black, style = Stroke(5f))
    }
}

fun generateSignatureBitmap(points: List<Offset>, nombre: String, equipos: List<Equipo>): Bitmap {
    val b = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888); val c = android.graphics.Canvas(b); c.drawColor(android.graphics.Color.WHITE)
    val p = Paint().apply { color = android.graphics.Color.BLACK; strokeWidth = 5f; style = Paint.Style.STROKE }
    val path = android.graphics.Path(); var first = true; points.forEach { if (it == Offset.Unspecified) first = true else { if (first) path.moveTo(it.x, it.y) else path.lineTo(it.x, it.y); first = false } }
    c.drawPath(path, p); return b
}

@Composable
fun EquipoSeleccionCard(equipo: Equipo, isSelected: Boolean, onSelect: () -> Unit) { Card(modifier = Modifier.fillMaxWidth().clickable { onSelect() }.border(1.dp, if (isSelected) Color.Blue else Color.Transparent)) { Row(modifier = Modifier.padding(16.dp)) { Checkbox(isSelected, null); Column { Text(equipo.noInventario ?: "", fontWeight = FontWeight.Bold); Text(equipo.nombre ?: "") } } } }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FormularioEquipo(viewModel: InventarioViewModel, equipoExistente: Equipo?, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(equipoExistente?.nombre ?: "") }; val coroutineScope = rememberCoroutineScope(); val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) { Card { Column(modifier = Modifier.padding(16.dp)) { OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }); Button(onClick = { coroutineScope.launch { viewModel.guardarEquipo(Equipo(nombre = nombre), null) { onDismiss() } } }) { Text("GUARDAR") } } } }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraOCRDialog(onResult: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current; val lifecycleOwner = LocalLifecycleOwner.current; val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var detectedOptions by remember { mutableStateOf<List<String>>(emptyList()) }; var isSelecting by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (!isSelecting) {
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx -> PreviewView(ctx).apply { post { val provider = ProcessCameraProvider.getInstance(ctx).get(); val preview = Preview.Builder().build().also { it.setSurfaceProvider(this.surfaceProvider) }; val analyzer = ImageAnalysis.Builder().build().also { it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy -> val image = proxy.image; if (image != null) { recognizer.process(InputImage.fromMediaImage(image, proxy.imageInfo.rotationDegrees)).addOnSuccessListener { visionText -> detectedOptions = visionText.textBlocks.flatMap { it.lines.map { l -> l.text.uppercase() } } }.addOnCompleteListener { proxy.close() } } else proxy.close() } }; provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer) } } })
                Button(onClick = { isSelecting = true }, modifier = Modifier.align(Alignment.BottomCenter)) { Text("CAPTURAR") }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) { items(detectedOptions) { text -> Text(text, modifier = Modifier.clickable { onResult(text) }.padding(16.dp)) }; item { Button(onClick = { isSelecting = false }) { Text("REINTENTAR") } } }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
    }
}

@Composable
fun CameraPhotoDialog(onCaptured: (Uri) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current; val cameraController = remember { LifecycleCameraController(context) }; val lifecycleOwner = LocalLifecycleOwner.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { ctx -> PreviewView(ctx).apply { controller = cameraController; cameraController.bindToLifecycle(lifecycleOwner) } }, modifier = Modifier.fillMaxSize())
            Button(onClick = { val file = java.io.File(context.cacheDir, "${System.currentTimeMillis()}.jpg"); cameraController.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback { override fun onImageSaved(output: ImageCapture.OutputFileResults) { onCaptured(Uri.fromFile(file)) }; override fun onError(exc: ImageCaptureException) { } }) }, modifier = Modifier.align(Alignment.BottomCenter)) { Text("TOMAR FOTO") }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EquipoCard(equipo: Equipo, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onEdit() }) { Column(modifier = Modifier.padding(16.dp)) { Text(equipo.noInventario ?: "S/N", fontWeight = FontWeight.Bold); Text(equipo.nombre ?: "S/N") } }
}

@Composable
fun CampoDato(label: String, value: String?) { Row { Text("$label: ", fontWeight = FontWeight.Bold); Text(value ?: "") } }
