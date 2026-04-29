package com.example.inventario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventario.model.SupabaseUsageStats
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(onBack: () -> Unit) {
    val viewModel: MonitoringViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.fetchStats()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MONITOREO SUPABASE", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerRefresh() }) {
                        Icon(Icons.Default.Refresh, "Refrescar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.errorMessage != null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.fetchStats() }) { Text("REINTENTAR") }
                }
            } else if (viewModel.stats == null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("No hay datos de monitoreo disponibles", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.triggerRefresh() }) { Text("FORZAR PRIMERA LECTURA") }
                }
            } else {
                val stats = viewModel.stats!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Resumen de Plan Gratuito",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Última captura: ${stats.capturedAt?.substringBefore(".")?.replace("T", " ") ?: "N/A"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    UsageCard(
                        title = "Database Storage",
                        icon = Icons.Default.Storage,
                        currentValue = stats.dbSizeBytes,
                        limitValue = SupabaseUsageStats.LIMIT_DB_SIZE,
                        unit = "MB",
                        isBytes = true
                    )

                    UsageCard(
                        title = "Storage Objects",
                        icon = Icons.Default.Folder,
                        currentValue = stats.storageSizeBytes,
                        limitValue = SupabaseUsageStats.LIMIT_STORAGE_SIZE,
                        unit = "MB",
                        isBytes = true
                    )

                    UsageCard(
                        title = "Storage Bandwidth (Egress)",
                        icon = Icons.Default.CloudDownload,
                        currentValue = stats.storageEgressBytes,
                        limitValue = SupabaseUsageStats.LIMIT_STORAGE_EGRESS,
                        unit = "MB",
                        isBytes = true
                    )

                    UsageCard(
                        title = "Edge Function Invocations",
                        icon = Icons.Default.Code,
                        currentValue = stats.edgeInvocations,
                        limitValue = SupabaseUsageStats.LIMIT_EDGE_INVOCATIONS,
                        unit = "invocaciones"
                    )

                    UsageCard(
                        title = "Auth (Active Users)",
                        icon = Icons.Default.Group,
                        currentValue = stats.authMau,
                        limitValue = SupabaseUsageStats.LIMIT_AUTH_MAU,
                        unit = "usuarios"
                    )

                    UsageCard(
                        title = "Realtime Messages",
                        icon = Icons.Default.Message,
                        currentValue = stats.realtimeMessages,
                        limitValue = SupabaseUsageStats.LIMIT_REALTIME_MSGS,
                        unit = "mensajes"
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun UsageCard(
    title: String,
    icon: ImageVector,
    currentValue: Long,
    limitValue: Long,
    unit: String,
    isBytes: Boolean = false
) {
    val percentage = (currentValue.toFloat() / limitValue.toFloat()).coerceIn(0f, 1f)
    val color = when {
        percentage > 0.9f -> Color.Red
        percentage > 0.7f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    val displayValue = if (isBytes) {
        val mb = currentValue / (1024.0 * 1024.0)
        DecimalFormat("#.##").format(mb)
    } else {
        DecimalFormat("#,###").format(currentValue)
    }

    val displayLimit = if (isBytes) {
        val mb = limitValue / (1024.0 * 1024.0)
        DecimalFormat("#.##").format(mb)
    } else {
        DecimalFormat("#,###").format(limitValue)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${(percentage * 100).toInt()}%", fontWeight = FontWeight.Black, color = color)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = color.copy(alpha = 0.1f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$displayValue $unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Límite: $displayLimit $unit", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
