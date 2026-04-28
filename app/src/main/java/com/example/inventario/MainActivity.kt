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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventarioTheme {
                val viewModel: InventarioViewModel = viewModel()
                var currentScreen by rememberSaveable { mutableStateOf("splash") }

                LaunchedEffect(Unit) {
                    viewModel.verificarSesion()
                }

                if (!viewModel.sessionInitialized && currentScreen == "splash") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (currentScreen == "splash") {
                        currentScreen = if (viewModel.isLogged()) "home" else "login"
                    }

                    when (currentScreen) {
                        "login" -> LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { currentScreen = "home" }
                        )
                        "home" -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToInventario = { currentScreen = "inventario" },
                            onNavigateToPrestamos = { currentScreen = "prestamos" },
                            onNavigateToUsuarios = { currentScreen = "usuarios" },
                            onLogout = { currentScreen = "login" }
                        )
                        "inventario" -> InventarioScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "home" }
                        )
                        "prestamos" -> PrestamosScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "home" }
                        )
                        "usuarios" -> GestionUsuariosScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: InventarioViewModel,
    onNavigateToInventario: () -> Unit,
    onNavigateToPrestamos: () -> Unit,
    onNavigateToUsuarios: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DIE CUT SOLUTIONS", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = { 
                        viewModel.cerrarSesion { onLogout() }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Salir")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bienvenida y Logo
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Bienvenido,", fontSize = 14.sp, color = Color.Gray)
                    Text(viewModel.currentUserPerfil?.nombre ?: "Usuario", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Surface(
                        color = when {
                            viewModel.isAdmin() -> Color(0xFFE3F2FD)
                            viewModel.isViewer() -> Color(0xFFF1F8E9)
                            else -> Color(0xFFF5F5F5)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = when {
                                viewModel.isAdmin() -> "ADMINISTRADOR"
                                viewModel.isViewer() -> "LECTURA"
                                else -> "OPERADOR"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                viewModel.isAdmin() -> Color(0xFF1976D2)
                                viewModel.isViewer() -> Color(0xFF388E3C)
                                else -> Color.Gray
                            }
                        )
                    }
                }
                
                // Logo de la empresa
                Image(
                    painter = coil3.compose.rememberAsyncImagePainter(R.drawable.logo_dcs),
                    contentDescription = "Logo DCS",
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Tablero de Estadísticas
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RESUMEN DE INVENTARIO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("Total", viewModel.statsTotalEquipos.toString(), Icons.Default.Inventory, Color.DarkGray)
                        StatItem("Prestados", viewModel.statsPrestados.toString(), Icons.Default.Outbond, Color(0xFF2196F3))
                        StatItem("Dañados", viewModel.statsMantenimiento.toString(), Icons.Default.ReportProblem, Color(0xFFE53935))
                        StatItem("Vencidos", viewModel.statsVencidos.toString(), Icons.Default.History, Color(0xFFFBC02D))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedCard(
                onClick = onNavigateToInventario,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(160.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("GESTIÓN DE INVENTARIO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ElevatedCard(
                onClick = onNavigateToPrestamos,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(160.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("CONTROL DE PRÉSTAMOS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (viewModel.isAdmin()) {
                Spacer(modifier = Modifier.height(32.dp))
                ElevatedCard(
                    onClick = onNavigateToUsuarios,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(120.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("GESTIÓN DE USUARIOS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            Text("v2.5.0 - Enterprise Edition", fontSize = 12.sp, color = Color.Gray)
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar Aplicación") },
            text = { Text("¿Estás seguro que deseas salir del sistema?") },
            confirmButton = {
                Button(onClick = { (context as? Activity)?.finish() }) { Text("SALIR") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("CANCELAR") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(viewModel: InventarioViewModel, onBack: () -> Unit) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchEquipos()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (viewModel.isTrashMode) "PAPELERA DE RECICLAJE" else "INVENTARIO GENERAL", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (viewModel.isTrashMode) Color.DarkGray else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (viewModel.isTrashMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (viewModel.isAdmin()) {
                        IconButton(onClick = { viewModel.toggleTrashMode() }) {
                            Icon(if (viewModel.isTrashMode) Icons.Default.Inventory else Icons.Default.Delete, null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!viewModel.isTrashMode && viewModel.isAdmin()) {
                FloatingActionButton(
                    onClick = {
                        equipoAEditar = null
                        showForm = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Agregar Equipo", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar por No. Inv, Marca o Serie...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (viewModel.errorMessage != null) {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.fetchEquipos() }) { Text("REINTENTAR") }
                    }
                } else {
                    val items = if (viewModel.isTrashMode) {
                        viewModel.filteredEquipos // filteredEquipos ya maneja el modo trash en el VM
                    } else {
                        viewModel.filteredEquipos
                    }

                    if (items.isEmpty()) {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Text("No se encontraron equipos", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items) { equipo ->
                                EquipoCard(
                                    equipo = equipo,
                                    isTrash = viewModel.isTrashMode,
                                    isReadOnly = !viewModel.isAdmin(),
                                    onEdit = {
                                        equipoAEditar = equipo
                                        showForm = true
                                    },
                                    onRestore = {
                                        viewModel.restaurarEquipo(equipo, context) {
                                            Toast.makeText(context, "Equipo restaurado", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onDeletePermanent = {
                                        viewModel.eliminarFisico(equipo) {
                                            Toast.makeText(context, "Equipo eliminado permanentemente", Toast.LENGTH_SHORT).show()
                                        }
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
                    title = { Text("CONTROL DE PRÉSTAMOS", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                        }
                    }
                )
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("PRESTAR", fontSize = 12.sp) }, icon = { Icon(Icons.Default.Outbond, null) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("RECIBIR", fontSize = 12.sp) }, icon = { Icon(Icons.Default.MoveToInbox, null) })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("PENDIENTES", fontSize = 12.sp) }, icon = { Icon(Icons.Default.History, null) })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("HISTORIAL", fontSize = 12.sp) }, icon = { Icon(Icons.Default.FactCheck, null) })
                }
            }
        }
    ) { innerPadding ->
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
    var searchQuery by remember { mutableStateOf("") }
    var showLoanForm by remember { mutableStateOf(false) }
    
    val filteredDisponibles by remember(searchQuery, viewModel.equiposDisponiblesParaPrestamo) {
        derivedStateOf {
            if (searchQuery.isBlank()) viewModel.equiposDisponiblesParaPrestamo
            else viewModel.equiposDisponiblesParaPrestamo.filter {
                it.noInventario?.contains(searchQuery, ignoreCase = true) == true ||
                it.nombre?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar No. Inventario o Nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredDisponibles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay equipos disponibles para préstamo", color = Color.Gray)
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

        if (viewModel.equiposSeleccionados.isNotEmpty() && !viewModel.isViewer()) {
            ExtendedFloatingActionButton(
                onClick = { showLoanForm = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Default.Check, null) },
                text = { Text("GENERAR PRÉSTAMO (${viewModel.equiposSeleccionados.size})") }
            )
        }
    }

    if (showLoanForm) {
        FormularioResguardo(
            viewModel = viewModel,
            onDismiss = { showLoanForm = false },
            onSuccess = { showLoanForm = false }
        )
    }
}

@Composable
fun RetornoEquiposView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showOCR by remember { mutableStateOf(false) }
    
    // Lista de préstamos encontrados para devolver
    var prestamosEncontrados = remember { mutableStateListOf<Prestamo>() }
    var equipoParaDano by remember { mutableStateOf<Pair<Equipo, Prestamo?>?>(null) }
    var showReturnChoiceDialog by remember { mutableStateOf<Prestamo?>(null) }
    var showDamageForm by remember { mutableStateOf(false) }
    var showBulkReturnDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Función de búsqueda unificada
    fun buscar() {
        val term = searchQuery.trim().uppercase()
        if (term.isEmpty()) return
        
        coroutineScope.launch {
            viewModel.isLoading = true
            try {
                if (term.startsWith("PR-")) {
                    // Buscar por Folio
                    val results = supabase.from("prestamos").select {
                        filter { eq("folio", term); eq("estado", "activo") }
                    }.decodeList<Prestamo>()
                    
                    prestamosEncontrados.clear()
                    prestamosEncontrados.addAll(results)
                    if (results.isEmpty()) {
                        Toast.makeText(context, "No se encontraron préstamos activos para este folio", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Buscar por No Inventario
                    val equipo = viewModel.equiposPrestados.find { it.noInventario == term }
                    if (equipo != null) {
                        val result = supabase.from("prestamos").select {
                            filter { eq("id_equipo", equipo.id!!); eq("estado", "activo") }
                            order("fecha_prestamo", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            limit(1)
                        }.decodeSingleOrNull<Prestamo>()
                        
                        prestamosEncontrados.clear()
                        if (result != null) {
                            prestamosEncontrados.add(result)
                        } else {
                            // Está marcado como prestado pero no tiene folio activo (caso raro)
                            prestamosEncontrados.add(Prestamo(idEquipo = equipo.id, nombreComodatario = "DESCONOCIDO", folio = "S/N"))
                        }
                    } else {
                        Toast.makeText(context, "Equipo no encontrado o no está prestado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                viewModel.isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("No. Inv o Folio (PR-...)") },
                leadingIcon = {
                    IconButton(onClick = { showOCR = true }) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            prestamosEncontrados.clear()
                        }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { buscar() },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, null)
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            if (prestamosEncontrados.isNotEmpty()) {
                Column {
                    // Cabecera del Folio
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("RESPONSABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(prestamosEncontrados[0].nombreComodatario ?: "DESCONOCIDO", fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Folio: ${prestamosEncontrados[0].folio}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            if (prestamosEncontrados.size > 1) {
                                Button(
                                    onClick = { showBulkReturnDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("TODO (${prestamosEncontrados.size})")
                                }
                            }
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(prestamosEncontrados) { prestamo ->
                            val equipo = viewModel.equiposPrestados.find { it.id == prestamo.idEquipo } ?: Equipo(nombre = "Equipo no encontrado", noInventario = "???")
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.1f))) {
                                        if (!equipo.imagenUrl.isNullOrEmpty()) {
                                            AsyncImage(model = equipo.imagenUrl, contentDescription = null, contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(Icons.Default.Devices, null, modifier = Modifier.align(Alignment.Center), tint = Color.LightGray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(equipo.noInventario ?: "S/N", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(equipo.nombre ?: "Sin nombre", fontSize = 14.sp, maxLines = 1)
                                    }
                                    Button(
                                        onClick = { showReturnChoiceDialog = prestamo },
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("RECIBIR", fontSize = 12.sp)
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
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ingresa No. de Inventario o Folio", color = Color.Gray)
                }
            }
        }
    }

    // Diálogo de Condición (Individual)
    if (showReturnChoiceDialog != null) {
        val p = showReturnChoiceDialog!!
        val eq = viewModel.equiposPrestados.find { it.id == p.idEquipo }!!
        
        AlertDialog(
            onDismissRequest = { showReturnChoiceDialog = null },
            title = { Text("Condición de Entrega") },
            text = { Text("¿En qué condiciones se recibe el equipo ${eq.noInventario}?") },
            confirmButton = {
                Button(
                    onClick = { 
                        coroutineScope.launch {
                            viewModel.registrarDevolucion(eq, p, context) {
                                prestamosEncontrados.remove(p)
                                if (prestamosEncontrados.isEmpty()) searchQuery = ""
                                showReturnChoiceDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("FUNCIONAL") }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        equipoParaDano = eq to p
                        showReturnChoiceDialog = null
                        showDamageForm = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("TIENE DAÑOS") }
            }
        )
    }

    // Diálogo de Recepción Masiva
    if (showBulkReturnDialog) {
        AlertDialog(
            onDismissRequest = { showBulkReturnDialog = false },
            title = { Text("Recibir Todo") },
            text = { Text("¿Confirmas la recepción de los ${prestamosEncontrados.size} equipos de este folio en buen estado?") },
            confirmButton = {
                Button(
                    onClick = {
                        showBulkReturnDialog = false
                        coroutineScope.launch {
                            val itemsToProcess = prestamosEncontrados.toList()
                            itemsToProcess.forEach { p ->
                                val eq = viewModel.equiposPrestados.find { it.id == p.idEquipo }
                                if (eq != null) {
                                    viewModel.registrarDevolucion(eq, p, context) {
                                        prestamosEncontrados.remove(p)
                                    }
                                }
                            }
                            searchQuery = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("SÍ, RECIBIR TODO") }
            },
            dismissButton = {
                TextButton(onClick = { showBulkReturnDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    if (showDamageForm && equipoParaDano != null) {
        FormularioDano(
            viewModel = viewModel,
            equipo = equipoParaDano!!.first,
            prestamo = equipoParaDano!!.second,
            onDismiss = { showDamageForm = false },
            onSuccess = { 
                prestamosEncontrados.remove(equipoParaDano!!.second)
                if (prestamosEncontrados.isEmpty()) searchQuery = ""
                showDamageForm = false
                equipoParaDano = null
            }
        )
    }

    if (showOCR) {
        CameraOCRDialog(
            onResult = { 
                searchQuery = it.uppercase()
                showOCR = false
                buscar()
            },
            onDismiss = { showOCR = false }
        )
    }
}

@Composable
fun PendientesView(viewModel: InventarioViewModel) {
    val prestamosPorPersona = remember(viewModel.prestamosActivos.size) {
        viewModel.prestamosActivos.groupBy { it.nombreComodatario ?: "SIN NOMBRE" }
    }

    if (prestamosPorPersona.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AssignmentTurnedIn, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Text("No hay préstamos pendientes", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            prestamosPorPersona.forEach { (nombre, prestamos) ->
                item {
                    Column {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        prestamos.forEach { prestamo ->
                            val equipo = viewModel.equiposPrestados.find { it.id == prestamo.idEquipo }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Devices, null, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Inv: ${equipo?.noInventario ?: "N/A"}", fontWeight = FontWeight.Bold)
                                        Text("${equipo?.nombre ?: "N/A"} - Folio: ${prestamo.folio}", fontSize = 12.sp)
                                        Text("Desde: ${prestamo.fechaPrestamo?.substringBefore("T")}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialView(viewModel: InventarioViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val filteredHistorial by remember(searchQuery, viewModel.historialPrestamos.size) {
        derivedStateOf {
            if (searchQuery.isBlank()) viewModel.historialPrestamos
            else viewModel.historialPrestamos.filter { p ->
                val equipo = viewModel.filteredEquipos.find { it.id == p.idEquipo }
                p.folio?.contains(searchQuery, ignoreCase = true) == true ||
                p.nombreComodatario?.contains(searchQuery, ignoreCase = true) == true ||
                equipo?.noInventario?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Buscar por Folio, Persona o Equipo...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (filteredHistorial.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay registros en el historial", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredHistorial) { prestamo ->
                    val equipo = viewModel.filteredEquipos.find { it.id == prestamo.idEquipo }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("FOLIO: ${prestamo.folio}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                    Text("DEVUELTO", fontSize = 10.sp, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            Text("Equipo: ${equipo?.noInventario} - ${equipo?.nombre}", fontWeight = FontWeight.Bold)
                            Text("Comodatario: ${prestamo.nombreComodatario}", fontSize = 14.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("FECHA SALIDA", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(prestamo.fechaPrestamo?.substringBefore("T") ?: "---", fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("FECHA REGRESO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(prestamo.fechaDevolucion?.substringBefore("T") ?: "---", fontSize = 13.sp)
                                }
                            }

                            if (!prestamo.firmaUrl.isNullOrEmpty()) {
                                var showFirma by remember { mutableStateOf(false) }
                                
                                OutlinedButton(
                                    onClick = { showFirma = true },
                                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Draw, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("VER FIRMA DE RECIBIDO")
                                }

                                if (showFirma) {
                                    FirmaView(url = prestamo.firmaUrl!!, onDismiss = { showFirma = false })
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.descargarContratoYCompartir(context, prestamo) },
                                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !viewModel.isLoading
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DESCARGAR CONTRATO PDF")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirmaView(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.9f)) {
            Box(modifier = Modifier.fillMaxSize()) {
                ZoomableImage(
                    model = url,
                    modifier = Modifier.fillMaxSize()
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
                
                Text(
                    "Firma Digital de Aceptación",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FormularioResguardo(viewModel: InventarioViewModel, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val points = remember { mutableStateListOf<Offset>() }
    val coroutineScope = rememberCoroutineScope()
    
    // Fecha de devolución
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()
    
    val formattedDate = remember(selectedDateMillis) {
        if (selectedDateMillis == null) "Indefinido"
        else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                    Text("Detalle del Resguardo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Fecha de Devolución
                Text("Vencimiento (Opcional):", fontWeight = FontWeight.Bold)
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fecha de Devolución:", fontSize = 11.sp, color = Color.Gray)
                            Text(formattedDate, fontWeight = FontWeight.Bold)
                        }
                        if (selectedDateMillis != null) {
                            IconButton(onClick = { selectedDateMillis = null }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Artículos Seleccionados:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        viewModel.equiposSeleccionados.forEach {
                            Text("• ${it.noInventario} - ${it.nombre}", modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Comodatario (Recibe):", fontWeight = FontWeight.Bold)
                if (viewModel.usuarioSeleccionado == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            viewModel.buscarUsuarios(it)
                        },
                        placeholder = { Text("Buscar nombre o correo...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.PersonSearch, null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    if (viewModel.usuariosEncontrados.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                viewModel.usuariosEncontrados.forEach { usuario ->
                                    ListItem(
                                        headlineContent = { Text(usuario.nombre ?: "", fontWeight = FontWeight.Bold) },
                                        supportingContent = { Text(usuario.email ?: "") },
                                        modifier = Modifier.clickable { 
                                            viewModel.usuarioSeleccionado = usuario
                                            searchQuery = ""
                                            viewModel.usuariosEncontrados.clear()
                                        }
                                    )
                                    HorizontalDivider(thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(viewModel.usuarioSeleccionado!!.nombre ?: "", fontWeight = FontWeight.Bold)
                                Text(viewModel.usuarioSeleccionado!!.email ?: "", fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.usuarioSeleccionado = null }) {
                                Icon(Icons.Default.Edit, null)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Firma del Comodatario:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    SignatureCanvas(
                        points = points,
                        onPointsChange = { points.add(it) }
                    )
                    
                    if (points.isNotEmpty()) {
                        TextButton(
                            onClick = { points.clear() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Text("LIMPIAR", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            "Firme aquí",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.LightGray,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        val usuario = viewModel.usuarioSeleccionado
                        if (usuario != null && points.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    val bitmap = generateSignatureBitmap(points, usuario.nombre ?: "", viewModel.equiposSeleccionados)
                                    val bos = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos)
                                    
                                    val vDate = selectedDateMillis?.let {
                                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date(it))
                                    }

                                    viewModel.realizarPrestamo(
                                        nombre = usuario.nombre ?: "",
                                        idUsuario = usuario.id,
                                        firmaBytes = bos.toByteArray(),
                                        dispositivoModelo = android.os.Build.MODEL,
                                        dispositivoNombre = "Android Device",
                                        fechaVencimiento = vDate,
                                        onSuccess = { onSuccess() }
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val msg = if (usuario == null) "Selecciona un usuario" else "Se requiere la firma"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("FINALIZAR Y FIRMAR")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("ACEPTAR") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FormularioDano(viewModel: InventarioViewModel, equipo: Equipo, prestamo: Prestamo?, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var gravedad by remember { mutableStateOf("leve") }
    var descripcion by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Reporte de Daño", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Equipo: ${equipo.noInventario}", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Nivel de Gravedad:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeverityOption("LEVE", Color(0xFF4CAF50), gravedad == "leve", Modifier.weight(1f)) { gravedad = "leve" }
                    SeverityOption("GRAVE", Color(0xFFE53935), gravedad == "grave", Modifier.weight(1f)) { gravedad = "grave" }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del daño") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .clickable { showCamera = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        IconButton(onClick = { imageUri = null }, modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(40.dp))
                            Text("AÑADIR FOTO DE EVIDENCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (descripcion.isBlank()) {
                            Toast.makeText(context, "Describe el daño", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            val url = if (imageUri != null) viewModel.subirImagenDano(context, imageUri!!) else null
                            viewModel.registrarDevolucion(
                                equipo, 
                                prestamo, 
                                context, 
                                Dano(gravedad = gravedad, descripcion = descripcion, imagenUrl = url)
                            ) {
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("ENVIAR REPORTE")
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("CANCELAR")
                }
            }
        }
    }
    
    if (showCamera) {
        CameraPhotoDialog(
            onCaptured = { 
                imageUri = it
                showCamera = false
            },
            onDismiss = { showCamera = false }
        )
    }
}

@Composable
fun SeverityOption(label: String, color: Color, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color else color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(label, color = if (isSelected) Color.White else color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun SignatureCanvas(points: SnapshotStateList<Offset>, onPointsChange: (Offset) -> Unit) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onPointsChange(it) },
                    onDrag = { change, _ -> 
                        change.consume()
                        onPointsChange(change.position) 
                    },
                    onDragEnd = { onPointsChange(Offset.Unspecified) }
                )
            }
    ) {
        val path = Path()
        var first = true
        points.forEach { offset ->
            if (offset == Offset.Unspecified) {
                first = true
            } else {
                if (first) {
                    path.moveTo(offset.x, offset.y)
                    first = false
                } else {
                    path.lineTo(offset.x, offset.y)
                }
            }
        }
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

fun generateSignatureBitmap(points: List<Offset>, nombre: String, equipos: List<Equipo>): Bitmap {
    val width = 1200
    val height = 800
    val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(b)
    c.drawColor(android.graphics.Color.WHITE)

    // Dibujar firma
    val p = Paint().apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    
    val path = android.graphics.Path()
    var first = true
    points.forEach { offset ->
        if (offset == Offset.Unspecified) {
            first = true
        } else {
            if (first) {
                path.moveTo(offset.x * (width / 800f), offset.y * (height / 600f)) // Ajuste de escala si es necesario
                first = false
            } else {
                path.lineTo(offset.x * (width / 800f), offset.y * (height / 600f))
            }
        }
    }
    c.drawPath(path, p)

    // Dibujar info pie de firma
    val textPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 30f
        isAntiAlias = true
    }
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    c.drawText("Comodatario: $nombre", 50f, height - 100f, textPaint)
    c.drawText("Fecha: $date", 50f, height - 60f, textPaint)
    c.drawText("Equipos: ${equipos.size} artículos", 50f, height - 20.dp.value, textPaint)
    
    return b
}

@Composable
fun EquipoSeleccionCard(equipo: Equipo, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(equipo.noInventario ?: "S/N", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(equipo.nombre ?: "Sin nombre", fontSize = 14.sp)
                Text("Serie: ${equipo.numeroSerie ?: "N/A"}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FormularioEquipo(viewModel: InventarioViewModel, equipoExistente: Equipo?, onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados de los campos
    var noInventario by remember { mutableStateOf(equipoExistente?.noInventario ?: "") }
    var nombre by remember { mutableStateOf(equipoExistente?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(equipoExistente?.descripcion ?: "") }
    var categoria by remember { mutableStateOf(equipoExistente?.categoria ?: "") }
    var marca by remember { mutableStateOf(equipoExistente?.marca ?: "") }
    var modelo by remember { mutableStateOf(equipoExistente?.modelo ?: "") }
    var serie by remember { mutableStateOf(equipoExistente?.numeroSerie ?: "") }
    var tag by remember { mutableStateOf(equipoExistente?.numerotag ?: "") }
    var estado by remember { mutableStateOf(equipoExistente?.estado ?: "FUNCIONAL") }
    
    var imagenUrl by remember { mutableStateOf(equipoExistente?.imagenUrl) }
    var nuevaImagenUri by remember { mutableStateOf<Uri?>(null) }
    
    var showCamera by remember { mutableStateOf(false) }
    var showOCR by remember { mutableStateOf(false) }
    var ocrTarget by remember { mutableStateOf("") } // "inv" o "serie" o "tag"
    
    var showDanosHistorial by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                CenterAlignedTopAppBar(
                    title = { Text(if (equipoExistente == null) "NUEVO EQUIPO" else "EDITAR EQUIPO", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    },
                    actions = {
                        if (equipoExistente != null) {
                            IconButton(onClick = { 
                                viewModel.fetchDanosEquipo(equipoExistente.id!!)
                                showDanosHistorial = true 
                            }) {
                                Icon(Icons.Default.History, "Historial de Daños", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        TextButton(
                            onClick = {
                                if (noInventario.isBlank() || nombre.isBlank()) {
                                    Toast.makeText(context, "No. Inv y Nombre son obligatorios", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                                
                                if (viewModel.isNoInventarioDuplicado(noInventario, equipoExistente?.id)) {
                                    Toast.makeText(context, "El No. de Inventario ya existe", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }

                                coroutineScope.launch {
                                    val equipo = Equipo(
                                        id = equipoExistente?.id,
                                        noInventario = noInventario.uppercase(),
                                        nombre = nombre.uppercase(),
                                        descripcion = descripcion,
                                        categoria = categoria.uppercase(),
                                        marca = marca.uppercase(),
                                        modelo = modelo.uppercase(),
                                        numeroSerie = serie.uppercase(),
                                        numerotag = tag.uppercase(),
                                        estado = estado,
                                        imagenUrl = imagenUrl,
                                        modificadoPorModelo = android.os.Build.MODEL,
                                        modificadoPorNombre = viewModel.currentUserPerfil?.nombre ?: "Admin"
                                    )
                                    
                                    if (equipoExistente == null) {
                                        viewModel.guardarEquipo(equipo, nuevaImagenUri) { onDismiss() }
                                    } else {
                                        viewModel.actualizarEquipo(equipo, equipoExistente.imagenUrl, nuevaImagenUri) { onDismiss() }
                                    }
                                }
                            }
                        ) {
                            Text("GUARDAR", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Foto principal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { showCamera = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (nuevaImagenUri != null) {
                            AsyncImage(model = nuevaImagenUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else if (!imagenUrl.isNullOrEmpty()) {
                            AsyncImage(model = imagenUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                Text("Tocar para tomar foto", color = Color.Gray)
                            }
                        }
                    }

                    // Campos de texto
                    OutlinedTextField(
                        value = noInventario,
                        onValueChange = { noInventario = it.uppercase() },
                        label = { Text("Número de Inventario *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Tag, null) },
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { 
                                    ocrTarget = "inv"
                                    showOCR = true 
                                }) { Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.primary) }
                                
                                if (noInventario.length == 3) {
                                    IconButton(onClick = { noInventario = viewModel.sugerirSiguienteNumero(noInventario) }) {
                                        Icon(Icons.Default.AutoFixHigh, null, tint = Color.Blue)
                                    }
                                }
                            }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Equipo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownField(
                            label = "Categoría",
                            value = categoria,
                            options = viewModel.categoriasExistentes,
                            onValueChange = { categoria = it },
                            modifier = Modifier.weight(1f)
                        )
                        ExposedDropdownField(
                            label = "Estado",
                            value = estado,
                            options = listOf("FUNCIONAL", "OBSOLETO", "DESCOMPUESTO", "EN MANTENIMIENTO"),
                            onValueChange = { estado = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownField(
                            label = "Marca",
                            value = marca,
                            options = viewModel.marcasExistentes,
                            onValueChange = { marca = it },
                            modifier = Modifier.weight(1f)
                        )
                        ExposedDropdownField(
                            label = "Modelo",
                            value = modelo,
                            options = viewModel.modelosExistentes,
                            onValueChange = { modelo = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = serie,
                        onValueChange = { serie = it.uppercase() },
                        label = { Text("Número de Serie") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { 
                                ocrTarget = "serie"
                                showOCR = true 
                            }) { Icon(Icons.Default.QrCodeScanner, null) }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it.uppercase() },
                        label = { Text("Número de TAG") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { 
                                ocrTarget = "tag"
                                showOCR = true 
                            }) { Icon(Icons.Default.QrCodeScanner, null) }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción / Notas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    if (equipoExistente != null && !viewModel.isTrashMode) {
                        Button(
                            onClick = {
                                viewModel.eliminarEquipoLogico(equipoExistente, context) {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MOVER A PAPELERA")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showCamera) {
        CameraPhotoDialog(onCaptured = { nuevaImagenUri = it; showCamera = false }, onDismiss = { showCamera = false })
    }

    if (showOCR) {
        CameraOCRDialog(
            onResult = { result ->
                when (ocrTarget) {
                    "inv" -> noInventario = result.uppercase()
                    "serie" -> serie = result.uppercase()
                    "tag" -> tag = result.uppercase()
                }
                showOCR = false
            },
            onDismiss = { showOCR = false }
        )
    }

    if (showDanosHistorial && equipoExistente != null) {
        Dialog(onDismissRequest = { showDanosHistorial = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HISTORIAL DE DAÑOS", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    Text("Equipo: ${equipoExistente.noInventario}", fontSize = 12.sp, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (viewModel.danosDelEquipo.isEmpty()) {
                        Text("No hay reportes de daños registrados.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(viewModel.danosDelEquipo) { dano ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(dano.gravedad?.uppercase() ?: "LEVE", fontWeight = FontWeight.Bold, color = if(dano.gravedad == "grave") Color.Red else Color(0xFFFBC02D))
                                            Text(dano.fechaReporte?.substringBefore("T") ?: "", fontSize = 11.sp)
                                        }
                                        Text(dano.descripcion ?: "Sin descripción", fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                                        if (dano.folioPrestamo != null) {
                                            Text("Folio: ${dano.folioPrestamo}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (!dano.imagenUrl.isNullOrEmpty()) {
                                            var showFullImage by remember { mutableStateOf(false) }
                                            AsyncImage(
                                                model = dano.imagenUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { showFullImage = true },
                                                contentScale = ContentScale.Crop
                                            )
                                            if (showFullImage) {
                                                Dialog(onDismissRequest = { showFullImage = false }) {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        ZoomableImage(model = dano.imagenUrl!!, modifier = Modifier.fillMaxSize())
                                                        IconButton(onClick = { showFullImage = false }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                                            Icon(Icons.Default.Close, null, tint = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { showDanosHistorial = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("CERRAR")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownField(label: String, value: String, options: List<String>, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(),
            singleLine = true
        )
        if (options.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraOCRDialog(onResult: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    
    // Para selección de área
    var selectionRect by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var detectedTextInSelection by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (capturedBitmap == null) {
                // Vista de Cámara en Vivo
                val cameraController = remember { LifecycleCameraController(context) }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                        }
                    }
                )
                
                // Guía visual (rectángulo central)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(width = 280.dp, height = 100.dp).border(2.dp, Color.Cyan, RoundedCornerShape(8.dp)))
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Encuadra el código o texto", color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                    FloatingActionButton(
                        onClick = {
                            cameraController.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.capacity())
                                        buffer.get(bytes)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        
                                        // Rotar si es necesario
                                        val matrix = android.graphics.Matrix()
                                        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
                                        capturedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                        
                                        image.close()
                                    }
                                }
                            )
                        },
                        containerColor = Color.White
                    ) { Icon(Icons.Default.Camera, null) }
                }
            } else {
                // Vista de Selección de Texto sobre Foto Capturada
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // LIMPIAR estados previos para permitir nueva selección
                                    detectedTextInSelection = ""
                                    selectionRect = android.graphics.Rect(offset.x.toInt(), offset.y.toInt(), offset.x.toInt(), offset.y.toInt())
                                },
                                onDrag = { change, _ ->
                                    selectionRect?.let {
                                        it.right = change.position.x.toInt()
                                        it.bottom = change.position.y.toInt()
                                        selectionRect = android.graphics.Rect(it)
                                    }
                                },
                                onDragEnd = {
                                    selectionRect?.let { rect ->
                                        if (kotlin.math.abs(rect.width()) > 10 && kotlin.math.abs(rect.height()) > 10) {
                                            isAnalyzing = true
                                            val cropped = cropBitmap(capturedBitmap!!, rect, size.width, size.height)
                                            recognizer.process(InputImage.fromBitmap(cropped, 0))
                                                .addOnSuccessListener { visionText ->
                                                    detectedTextInSelection = visionText.text.trim()
                                                }
                                                .addOnCompleteListener { isAnalyzing = false }
                                        }
                                    }
                                }
                            )
                        },
                        contentScale = ContentScale.Fit
                    )

                    // Dibujar el rectángulo de selección
                    selectionRect?.let { rect ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                color = Color.Cyan,
                                topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                                size = androidx.compose.ui.geometry.Size((rect.right - rect.left).toFloat(), (rect.bottom - rect.top).toFloat()),
                                style = Stroke(width = 4f)
                            )
                        }
                    }

                    if (isAnalyzing) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Cyan)

                    // Resultado
                    if (detectedTextInSelection.isNotEmpty()) {
                        Popup(
                            alignment = Alignment.BottomCenter,
                            offset = IntOffset(0, -200),
                            properties = PopupProperties(focusable = true)
                        ) {
                            Card(
                                modifier = Modifier.padding(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Texto Detectado:", fontWeight = FontWeight.Bold)
                                    Text(detectedTextInSelection, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = { 
                                            detectedTextInSelection = ""
                                            selectionRect = null 
                                        }) {
                                            Text("REINTENTAR")
                                        }
                                        Button(onClick = { onResult(detectedTextInSelection) }) { 
                                            Text("USAR TEXTO") 
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
                        Button(onClick = { capturedBitmap = null; detectedTextInSelection = ""; selectionRect = null }) { Text("REINTENTAR FOTO") }
                    }
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

private fun cropBitmap(original: Bitmap, rect: android.graphics.Rect, viewWidth: Int, viewHeight: Int): Bitmap {
    // Mapear coordenadas de la vista a las coordenadas reales del Bitmap
    val scaleX = original.width.toFloat() / viewWidth
    val scaleY = original.height.toFloat() / viewHeight
    
    val left = (rect.left * scaleX).toInt().coerceIn(0, original.width)
    val top = (rect.top * scaleY).toInt().coerceIn(0, original.height)
    val right = (rect.right * scaleX).toInt().coerceIn(0, original.width)
    val bottom = (rect.bottom * scaleY).toInt().coerceIn(0, original.height)
    
    val width = (right - left).coerceAtLeast(1)
    val height = (bottom - top).coerceAtLeast(1)
    
    return Bitmap.createBitmap(original, left, top, width, height)
}

@Composable
fun CameraPhotoDialog(onCaptured: (Uri) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
            
            // Botones de control
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) { Icon(Icons.Default.Close, null, tint = Color.White) }

            FloatingActionButton(
                onClick = {
                    val file = java.io.File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    cameraController.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onCaptured(Uri.fromFile(file))
                            }
                            override fun onError(exc: ImageCaptureException) {
                                Log.e("Camera", "Error capturando: ${exc.message}")
                            }
                        }
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                containerColor = Color.White
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EquipoCard(
    equipo: Equipo, 
    isTrash: Boolean,
    isReadOnly: Boolean = false,
    onEdit: () -> Unit, 
    onRestore: () -> Unit,
    onDeletePermanent: () -> Unit
) {
    var showZoom by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = { if (!isTrash && !isReadOnly) onEdit() },
            onLongClick = { if (!isTrash && !isReadOnly) onEdit() }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Miniatura con Zoom
            Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha = 0.1f)).clickable { if (!equipo.imagenUrl.isNullOrEmpty()) showZoom = true }) {
                if (!equipo.imagenUrl.isNullOrEmpty()) {
                    AsyncImage(model = equipo.imagenUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Image, null, modifier = Modifier.align(Alignment.Center), tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.noInventario ?: "S/N", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Text(equipo.nombre ?: "Sin Nombre", fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when(equipo.estado) {
                        "FUNCIONAL" -> Color(0xFF4CAF50)
                        "PRESTADO" -> Color(0xFF2196F3)
                        "EN MANTENIMIENTO" -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(equipo.estado ?: "DESCONOCIDO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
                
                Text("Serie: ${equipo.numeroSerie ?: "---"}", fontSize = 12.sp, color = Color.Gray)
            }
            
            if (isTrash) {
                Row {
                    IconButton(onClick = onRestore) { Icon(Icons.Default.Restore, "Restaurar", tint = Color.Blue) }
                    IconButton(onClick = onDeletePermanent) { Icon(Icons.Default.DeleteForever, "Eliminar", tint = Color.Red) }
                }
            } else {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
            }
        }
    }

    if (showZoom && !equipo.imagenUrl.isNullOrEmpty()) {
        Dialog(onDismissRequest = { showZoom = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                ZoomableImage(model = equipo.imagenUrl!!, modifier = Modifier.fillMaxSize())
                IconButton(onClick = { showZoom = false }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(model: String, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .transformable(state = state)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    scale = if (scale > 1f) 1f else 3f
                    offset = Offset.Zero
                })
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.coerceIn(1f, 5f),
                    scaleY = scale.coerceIn(1f, 5f),
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(viewModel: InventarioViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        viewModel.fetchTodosLosPerfiles()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GESTIÓN DE USUARIOS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.todosLosPerfiles) { perfil ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(perfil.nombre ?: "Sin nombre", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("ID: ${perfil.id.take(8)}...", fontSize = 11.sp, color = Color.Gray)
                            }
                            
                            // Selector de Rol
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                FilterChip(
                                    selected = true,
                                    onClick = { expanded = true },
                                    label = { Text(perfil.rol ?: "USER") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when(perfil.rol) {
                                            "ADMIN" -> Color(0xFFE3F2FD)
                                            "VIEWER" -> Color(0xFFF1F8E9)
                                            else -> Color(0xFFF5F5F5)
                                        },
                                        selectedLabelColor = when(perfil.rol) {
                                            "ADMIN" -> Color(0xFF1976D2)
                                            "VIEWER" -> Color(0xFF388E3C)
                                            else -> Color.DarkGray
                                        }
                                    )
                                )
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("ADMINISTRADOR") },
                                        onClick = { 
                                            viewModel.actualizarRolUsuario(perfil.id, "ADMIN")
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("OPERADOR (USER)") },
                                        onClick = { 
                                            viewModel.actualizarRolUsuario(perfil.id, "USER")
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("SOLO LECTURA (VIEWER)") },
                                        onClick = { 
                                            viewModel.actualizarRolUsuario(perfil.id, "VIEWER")
                                            expanded = false
                                        }
                                    )
                                }
                            }

                            if (perfil.id != viewModel.currentUserPerfil?.id) {
                                IconButton(onClick = { viewModel.eliminarUsuario(perfil.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginScreen(viewModel: InventarioViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo o Título
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "DIE CUT SOLUTIONS",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "SISTEMA DE INVENTARIO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        var isRegistering by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (isRegistering) {
                        viewModel.registrarse(email, password) {
                            Log.d("Auth", "Callback de éxito en UI")
                            isRegistering = false
                            Toast.makeText(context, "Registro exitoso. Ahora inicia sesión.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        viewModel.iniciarSesion(email, password, onLoginSuccess)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(if (isRegistering) "REGISTRARSE" else "INICIAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(if (isRegistering) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("v2.5.0 Security Patch", fontSize = 10.sp, color = Color.LightGray)
    }
}



