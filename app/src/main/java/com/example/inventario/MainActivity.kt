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
import androidx.compose.foundation.gestures.detectTapGestures
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

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventarioTheme {
                val viewModel: InventarioViewModel = viewModel()
                val context = LocalContext.current
                var showForm by rememberSaveable { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    viewModel.fetchEquipos()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Inventario de Equipos", fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            actions = {
                                IconButton(onClick = { (context as? Activity)?.finish() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar Aplicación")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showForm = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar Equipo")
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        // Cuadro de búsqueda
                        OutlinedTextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            placeholder = { Text("Buscar por No. Inventario...") },
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
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(viewModel.filteredEquipos) { equipo ->
                                        EquipoCard(equipo)
                                    }
                                }
                            }
                        }
                    }

                    if (showForm) {
                        FormularioEquipo(
                            viewModel = viewModel,
                            onDismiss = { showForm = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioEquipo(viewModel: InventarioViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
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

    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var marca by rememberSaveable { mutableStateOf("") }
    var modelo by rememberSaveable { mutableStateOf("") }
    var estado by rememberSaveable { mutableStateOf("FUNCIONAL") }
    var numeroSerie by rememberSaveable { mutableStateOf("") }
    var noInventario by rememberSaveable { mutableStateOf("") }
    var numerotag by rememberSaveable { mutableStateOf("") }
    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val imageUri = imageUriString?.let { Uri.parse(it) }
    
    var showCameraOCR by rememberSaveable { mutableStateOf(false) }
    var ocrTargetField by rememberSaveable { mutableStateOf("") } // "serie" o "tag"
    var showCameraPhoto by rememberSaveable { mutableStateOf(false) }

    // Estados para los menús desplegables
    var expandedEstado by remember { mutableStateOf(false) }
    val estados = listOf("FUNCIONAL", "OBSOLETO", "DESCOMPUESTO")

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
                    Text("Nuevo Equipo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
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
                OutlinedTextField(
                    value = noInventario,
                    onValueChange = { 
                        val input = it.uppercase().take(8)
                        noInventario = input
                        if (input.length == 3) {
                            noInventario = viewModel.sugerirSiguienteNumero(input)
                        }
                    },
                    label = { Text("No. Inventario (Ej: ABC00001)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = noInventario.length != 8 && noInventario.isNotEmpty()
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

                // Número de Serie con OCR
                CampoConOCR(
                    value = numeroSerie,
                    label = "Número de Serie",
                    onValueChange = { numeroSerie = it.uppercase() },
                    onOCRClick = { ocrTargetField = "serie"; showCameraOCR = true }
                )

                // Número Tag con OCR
                CampoConOCR(
                    value = numerotag,
                    label = "Número Tag",
                    onValueChange = { numerotag = it.uppercase() },
                    onOCRClick = { ocrTargetField = "tag"; showCameraOCR = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (noInventario.length == 8) {
                            coroutineScope.launch {
                                val url = if (imageUri != null) subirImagen(context, imageUri!!) else null
                                val nuevoEquipo = Equipo(
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    categoria = categoria,
                                    marca = marca,
                                    modelo = modelo,
                                    estado = estado,
                                    numeroSerie = numeroSerie,
                                    numerotag = numerotag,
                                    noInventario = noInventario,
                                    imagenUrl = url,
                                    creadoPorModelo = android.os.Build.MODEL,
                                    creadoPorNombre = android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido",
                                    modificadoPorModelo = android.os.Build.MODEL,
                                    modificadoPorNombre = android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido"
                                )
                                viewModel.guardarEquipo(nuevoEquipo, null) {
                                    onDismiss()
                                    Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "No. Inventario debe tener 8 caracteres", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
                    else Text("GUARDAR EQUIPO")
                }
            }
        }
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
    var currentText by remember { mutableStateOf("") }
    
    // Unbind camera when the dialog is closed
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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
                                                val detectedText = visionText.textBlocks.firstOrNull()?.text
                                                if (!detectedText.isNullOrBlank()) {
                                                    currentText = detectedText
                                                }
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
                    .size(width = 300.dp, height = 120.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(2.dp, if (currentText.isNotEmpty()) Color.Green else Color.White, RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentText.isNotEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = currentText,
                            color = Color.White,
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Button(
                    onClick = { if (currentText.isNotEmpty()) onResult(currentText) },
                    enabled = currentText.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("CAPTURAR TEXTO", fontWeight = FontWeight.Bold)
                }
            }
            
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
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

@Composable
fun EquipoCard(equipo: Equipo) {
    var showZoom by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "INFORMACIÓN DEL EQUIPO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (!equipo.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = equipo.imagenUrl,
                    contentDescription = "Imagen del equipo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showZoom = true },
                    contentScale = ContentScale.Crop
                )
            }

            CampoDato("No. Inventario:", equipo.noInventario)
            CampoDato("Nombre:", equipo.nombre)
            CampoDato("Descripción:", equipo.descripcion)
            CampoDato("Categoría:", equipo.categoria)
            CampoDato("Marca:", equipo.marca)
            CampoDato("Modelo:", equipo.modelo)
            CampoDato("Estado:", equipo.estado)
            CampoDato("Número de Serie:", equipo.numeroSerie)
            CampoDato("Numero Tag:", equipo.numerotag)
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
