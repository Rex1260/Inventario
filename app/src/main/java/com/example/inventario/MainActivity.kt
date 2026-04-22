package com.example.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventario.model.Equipo
import com.example.inventario.ui.InventarioViewModel
import com.example.inventario.ui.theme.InventarioTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventarioTheme {
                val viewModel: InventarioViewModel = viewModel()
                val context = LocalContext.current
                
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
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar Aplicación"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        // Cuadro de búsqueda
                        OutlinedTextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                }
            }
        }
    }
}

@Composable
fun EquipoCard(equipo: Equipo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título de la tarjeta (Nombre del equipo)
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
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Campos ordenados: no. inventario, nombre, descripcion, categoria, marca, modelo, estado, numero de serie y fecha (ID oculto)
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
