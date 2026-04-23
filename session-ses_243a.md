# Inventory project review and improvement suggestions

**Session ID:** ses_243a6eec0ffe7m72F6dbIMHdm8
**Created:** 23/4/2026, 15:57:26
**Updated:** 23/4/2026, 15:58:30

---

## User

en la carpeta actual tengo un proyecto para hacer inventario de articulos, revisalo y sugiereme mejoras

---

## Assistant (Plan · Big Pickle · 47.6s)

_Thinking:_

El usuario quiere que revise su proyecto de inventario de artículos y le sugiera mejoras. Estoy en modo plan (solo lectura), así que solo puedo analizar y planificar, no hacer modificaciones.

Primero necesito explorar el proyecto para entender su estructura y contenido.


**Tool: task**

**Input:**
```json
{
  "description": "Explorar proyecto Inventario",
  "prompt": "Explora exhaustivamente el proyecto en el directorio actual C:\\Users\\grobles\\AndroidStudioProjects\\Inventario. Necesito entender:\n\n1. La estructura completa del proyecto (directorios, archivos principales)\n2. Qué tipo de proyecto es (Android, web, etc.)\n3. Lenguajes de programación utilizados\n4. Frameworks o librerías principales\n5. Arquitectura general (patrones de diseño, capas)\n6. Funcionalidades implementadas\n7. Base de datos o almacenamiento utilizado\n8. Cualquier archivo de configuración importante\n\nProporciona un resumen detallado de cada aspecto encontrado.",
  "subagent_type": "explore"
}
```

**Output:**
```
task_id: ses_243a6d286ffeZa59PE6tbh60jf (for resuming to continue this task if needed)

<task_result>


He completado la exploración exhaustiva del proyecto. A continuación presento el resumen detallado:

---

# RESUMEN EXHAUSTIVO DEL PROYECTO INVENTARIO

## 1. ESTRUCTURA COMPLETA DEL PROYECTO

```
Inventario/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/inventario/
│   │   │   │   ├── MainActivity.kt              (Actividad principal con UI Compose)
│   │   │   │   ├── data/
│   │   │   │   │   └── SupabaseClient.kt        (Cliente de Supabase)
│   │   │   │   ├── model/
│   │   │   │   │   └── Equipo.kt                (Modelo de datos)
│   │   │   │   └── ui/
│   │   │   │       ├── InventarioViewModel.kt    (ViewModel)
│   │   │   │       └── theme/
│   │   │   │           ├── Theme.kt   (Tema Compose)
│   │   │   │           ├── Color.kt  (Colores)
│   │   │   │           └── Type.kt   (Tipografía)
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/                             (Recursos)
│   │   ├── test/                               (Tests unitarios)
│   │   └── androidTest/                        (Tests instrumentados)
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                      (Versiones de librerías)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

## 2. TIPO DE PROYECTO

**Es un proyecto Android Nativo** desarrollado para dispositivos móviles Android.

| Característica | Valor |
|---------------|-------|
| Application ID | com.example.inventario |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 16) |
| Compile SDK | 36 |
| Version Code | 1 |
| Version Name | 1.0 |

## 3. LENGUAJES DE PROGRAMACIÓN UTILIZADOS

| Lenguaje | Propósito |
|---------|----------|
| **Kotlin** | Lenguaje principal del código de aplicación |
| **XML** | Recursos, manifestación y configuraciones |
| **Gradle (Kotlin DSL)** | Configuración de build |

## 4. FRAMEWORKS Y LIBRERÍAS PRINCIPALES

### Frameworks de UI (Compose)
- **Jetpack Compose** (BOM 2026.02.01)
  - `compose.ui` - Componentes UI básicos
  - `compose.material3` - Material Design 3
  - `compose.material.icons.extended` - Iconos Material

### Backend/Cloud
- **Supabase** (3.5.0)
  - `postgrest-kt` - Cliente PostgreSQL/PostgREST
  - `storage-kt` - Almacenamiento de archivos

### Networking
- **Ktor Client** (3.0.3) - Cliente HTTP Android
- **Kotlinx Serialization** (1.7.3) - Serialización JSON

### Imagenes
- **Coil** (3.1.0) - Carga de imágenes asíncronas
  - `coil-compose`
  - `coil-network-okhttp`

### Cámara y OCR
- **CameraX** (1.4.1)
  - `camera-core`
  - `camera-camera2`
  - `camera-lifecycle`
  - `camera-view`
- **ML Kit Text Recognition** (19.0.1) - OCR de texto

### Lifecycle
- **AndroidX Lifecycle** (2.8.7)
  - `lifecycle-runtime-ktx`
  - `lifecycle-viewmodel-compose`

### Build
- **Android Gradle Plugin** (9.2.0)
- **Kotlin** (2.2.10)

## 5. ARQUITECTURA GENERAL

### Arquitectura patterns utilizados:

**MVVM (Model-View-ViewModel)** con Compose
- **Model**: `Equipo.kt` - Modelo de datos con serialización Kotlin
- **View**: `MainActivity.kt` - UI con Compose
- **ViewModel**: `InventarioViewModel.kt` - Lógica de negocio y estado

**Clean Architecture simplificada** con capas:
1. **Capa de UI** (`ui/`) - Composables y ViewModels
2. **Capa de Datos** (`data/`) - Cliente Supabase
3. **Capa de Dominio** (`model/`) - Modelos de entidad

### Flujo de datos:
```
MainActivity (Compose UI)
       ↓
InventarioViewModel (Estado y lógica)
       ↓
SupabaseClient (API REST)
       ↓
Supabase Backend (PostgreSQL + Storage)
```

## 6. FUNCIONALIDADES IMPLEMENTADAS

### Gestión de Inventario:
- **CRUD completo** de equipos
  - CREATE: Agregar nuevos equipos
  - READ: Listar y buscar equipos
  - UPDATE: Editar equipos existentes
  - DELETE: Eliminación lógica (a papelera) y física

- **Validaciones**:
  - Número de inventario único (formato: 3 letras + 5 números)
  - Campos obligatorios validados

- **Búsqueda** por número de inventario

- **Sistema de papelera de reciclaje**:
  - Eliminación lógica (soft delete)
  - Restauración de equipos eliminados
  - Borrado permanente

- **Sugerencias** automáticas:
  - Categorías existentes
  - Marcas existentes
  - Modelos existentes
  - Sugerencia de siguiente número de inventario

### Funcionalidades de Cámara:
- **Captura de fotos** de equipos
- **OCR en tiempo real** para escanear:
  - Números de serie
  - Números tag

- **Procesamiento de imágenes**:
  - Redimensionamiento (máx 1200px)
  - Compresión JPEG (50%)
  - Optimización de memoria

### Metadata y Auditoría:
- Registro de quién creó/modificó cada registro
  - Modelo del dispositivo
  - Nombre del dispositivo
- Fechas de registro y modificación

## 7. BASE DE DATOS O ALMACENAMIENTO

### Backend: **Supabase** (Backend-as-a-Service)

**URL**: `https://gtzqekmewuwxcpyrojpn.supabase.co`

**PostgreSQL** (vía PostgREST):
- Tabla `equipos` con campos:
  - `id` (UUID)
  - `no_inventario` (string, único)
  - `nombre`, `descripcion`, `categoria`, `marca`, `modelo`
  - `estado` (FUNCIONAL, OBSOLETO, DESCOMPUESTO, PRESTADO)
  - `numero_serie`, `numerotag`
  - `imagen_url` (string)
  - `fecha_registro`, `fecha_modificacion` (timestamps)
  - `deleted_at` (soft delete)
  - `creado_por_modelo`, `creado_por_nombre`
  - `modificado_por_modelo`, `modificado_por_nombre`

**Storage** (Supabase Storage):
- Bucket `fotos_equipos` - Almacenamiento de imágenes

## 8. ARCHIVOS DE CONFIGURACIÓN IMPORTANTES

| Archivo | Propósito |
|---------|-----------|
| `app/build.gradle.kts` | Configuración del módulo app (deps, SDK, build) |
| `build.gradle.kts` | Configuración root del proyecto |
| `settings.gradle.kts` | Configuración de repos y módulos |
| `gradle/libs.versions.toml` | Catálogo de versiones (centralizado) |
| `gradle.properties` | Propiedades de Gradle (JVM args) |
| `app/src/main/AndroidManifest.xml` | Manifiesto de Android (permisos, activities) |
| `app/proguard-rules.pro` | Reglas de obfuscación |

### Permisos requeridos (AndroidManifest.xml):
- `INTERNET` - Conexión a Supabase
- `CAMERA` - Captura de fotos y OCR

---

## RESUMEN VISUAL DE ARQUITECTURA

```
┌─────────────────────────────────────────────────────────┐
│                    APP ANDROID                          │
├─────────────────────────────────────────────────────────┤
│  UI LAYER (Compose)                                    │
│  ┌──────────────────────────────────────────────┐    │
│  │ MainActivity.kt                              │    │
│  │ - InventarioViewModel → Estado                │    │
│  │ - Composable UI: Screens, Cards, Dialogs      │    │
│  │ - Camera OCR & Photo Integration            │    │
│  └──────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────┤
│  DATA LAYER                                           │
│  ┌─────────────┐  ┌─────────────────────────────┐     │
│  │ Equipoy.kt │  │ SupabaseClient.kt            │     │
│  │ (Modelo)   │  │ - Postgrest (REST API)      │     │
│  │           │  │ - Storage (Imágenes)       │     │
│  └─────────────┘  └─────────────────────────────┘     │
├─────────────────────────────────────────────────────────┤
│  BACKEND (Supabase Cloud)                              │
│  ┌────────────────┐  ┌────────────────────────┐   │
│  │ PostgreSQL     │  │ Storage                 │   │
│  │ Tabla: equipos │  │ Bucket: fotos_equipos   │   │
│  └────────────────┘  └────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

Este es un proyecto Android moderno y bien estructurado que utiliza las últimas tecnologías de Android (Jetpack Compose, Material 3, CameraX, ML Kit) con un backend en la nube (Supabase) para gestión completa de inventario con soporte para imágenes y OCR.
</task_result>
```

---

## Assistant (Plan · Big Pickle · 6.5s)

_Thinking:_

Ahora tengo una visión completa del proyecto. Es un proyecto Android de inventario bastante bien estructurado. Déjame leer los archivos principales para dar recomendaciones más específicas.

Voy a leer algunos archivos clave para entender mejor el código y dar mejoras concretas.




Excelente, es un proyecto Android de inventario bastante completo. Déjame revisar el código principal para darte mejoras específicas:


**Tool: read**

**Input:**
```json
{
  "filePath": "C:\\Users\\grobles\\AndroidStudioProjects\\Inventario\\app\\src\\main\\java\\com\\example\\inventario\\ui\\InventarioViewModel.kt"
}
```

**Output:**
```
<path>C:\Users\grobles\AndroidStudioProjects\Inventario\app\src\main\java\com\example\inventario\ui\InventarioViewModel.kt</path>
<type>file</type>
<content>
1: package com.example.inventario.ui
2: 
3: import android.net.Uri
4: import androidx.compose.runtime.derivedStateOf
5: import androidx.compose.runtime.getValue
6: import androidx.compose.runtime.mutableStateListOf
7: import androidx.compose.runtime.mutableStateOf
8: import androidx.compose.runtime.setValue
9: import androidx.lifecycle.ViewModel
10: import androidx.lifecycle.viewModelScope
11: import com.example.inventario.data.supabase
12: import com.example.inventario.model.Equipo
13: import io.github.jan.supabase.postgrest.from
14: import io.github.jan.supabase.postgrest.query.Order
15: import io.github.jan.supabase.postgrest.query.filter.FilterOperator
16: import io.github.jan.supabase.storage.storage
17: import kotlinx.coroutines.launch
18: import java.text.SimpleDateFormat
19: import java.util.Date
20: import java.util.Locale
21: import java.util.UUID
22: 
23: class InventarioViewModel : ViewModel() {
24:     private val _equipos = mutableStateListOf<Equipo>()
25:     var isLoading by mutableStateOf(false)
26:     var errorMessage by mutableStateOf<String?>(null)
27:     var searchQuery by mutableStateOf("")
28:     var isTrashMode by mutableStateOf(false)
29: 
30:     // Listas para sugerencias
31:     val categoriasExistentes = mutableStateListOf<String>()
32:     val marcasExistentes = mutableStateListOf<String>()
33:     val modelosExistentes = mutableStateListOf<String>()
34: 
35:     val filteredEquipos by derivedStateOf {
36:         if (searchQuery.isBlank()) {
37:             if (_equipos.isNotEmpty()) listOf(_equipos.first()) else emptyList()
38:         } else {
39:             _equipos.filter { equipo ->
40:                 equipo.noInventario?.contains(searchQuery, ignoreCase = true) == true
41:             }
42:         }
43:     }
44: 
45:     fun fetchEquipos() {
46:         viewModelScope.launch {
47:             isLoading = true
48:             errorMessage = null
49:             try {
50:                 // Filtramos según el modo (Normal o Papelera)
51:                 val results = supabase.from("equipos")
52:                     .select() {
53:                         filter {
54:                             if (isTrashMode) {
55:                                 // Modo Papelera: los que SÍ están eliminados (deleted_at NO es NULL)
56:                                 filterNot("deleted_at", FilterOperator.IS, null)
57:                             } else {
58:                                 // Modo Normal: los que NO están eliminados (deleted_at ES NULL)
59:                                 filter("deleted_at", FilterOperator.IS, null)
60:                             }
61:                         }
62:                         order("fecha_registro", Order.DESCENDING)
63:                     }
64:                     .decodeList<Equipo>()
65:                 _equipos.clear()
66:                 _equipos.addAll(results)
67:                 
68:                 // Actualizar sugerencias solo con equipos activos para que no salgan cosas borradas
69:                 if (!isTrashMode) {
70:                     categoriasExistentes.clear()
71:                     categoriasExistentes.addAll(results.mapNotNull { it.categoria }.distinct())
72:                     marcasExistentes.clear()
73:                     marcasExistentes.addAll(results.mapNotNull { it.marca }.distinct())
74:                     modelosExistentes.clear()
75:                     modelosExistentes.addAll(results.mapNotNull { it.modelo }.distinct())
76:                 }
77:                 
78:             } catch (e: Exception) {
79:                 errorMessage = "Error: ${e.message}"
80:                 e.printStackTrace()
81:             } finally {
82:                 isLoading = false
83:             }
84:         }
85:     }
86: 
87:     fun isNoInventarioDuplicado(noInventario: String, currentId: String?): Boolean {
88:         // Buscamos en la lista local (que ya tiene todos los registros cargados)
89:         // si existe otro equipo con ese número que no sea el mismo que estamos editando
90:         return _equipos.any { it.noInventario == noInventario && it.id != currentId }
91:     }
92: 
93:     fun toggleTrashMode() {
94:         isTrashMode = !isTrashMode
95:         fetchEquipos()
96:     }
97: 
98:     fun restaurarEquipo(equipo: Equipo, context: android.content.Context, onSuccess: () -> Unit) {
99:         viewModelScope.launch {
100:             isLoading = true
101:             try {
102:                 supabase.from("equipos").update(
103:                     mapOf(
104:                         "deleted_at" to null,
105:                         "modificado_por_modelo" to android.os.Build.MODEL,
106:                         "modificado_por_nombre" to (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido")
107:                     )
108:                 ) {
109:                     filter { eq("id", equipo.id!!) }
110:                 }
111:                 fetchEquipos()
112:                 onSuccess()
113:             } catch (e: Exception) {
114:                 errorMessage = "Error al restaurar: ${e.message}"
115:             } finally {
116:                 isLoading = false
117:             }
118:         }
119:     }
120: 
121:     fun eliminarFisico(equipo: Equipo, onSuccess: () -> Unit) {
122:         viewModelScope.launch {
123:             isLoading = true
124:             try {
125:                 supabase.from("equipos").delete {
126:                     filter { eq("id", equipo.id!!) }
127:                 }
128:                 fetchEquipos()
129:                 onSuccess()
130:             } catch (e: Exception) {
131:                 errorMessage = "Error en borrado permanente: ${e.message}"
132:             } finally {
133:                 isLoading = false
134:             }
135:         }
136:     }
137: 
138:     fun sugerirSiguienteNumero(prefijo: String): String {
139:         if (prefijo.length != 3) return ""
140:         val maxNumero = _equipos
141:             .filter { it.noInventario?.startsWith(prefijo, ignoreCase = true) == true }
142:             .mapNotNull { 
143:                 it.noInventario?.substring(3)?.toIntOrNull()
144:             }
145:             .maxOrNull() ?: 0
146:         
147:         return prefijo.uppercase() + (maxNumero + 1).toString().padStart(5, '0')
148:     }
149: 
150:     fun guardarEquipo(
151:         equipo: Equipo,
152:         imageUri: Uri?,
153:         onSuccess: () -> Unit
154:     ) {
155:         viewModelScope.launch {
156:             isLoading = true
157:             try {
158:                 // 2. Insertar en DB
159:                 val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
160:                 val now = sdf.format(Date())
161:                 val equipoConFechas = equipo.copy(
162:                     fechaRegistro = now,
163:                     fechaModificacion = now
164:                 )
165:                 
166:                 supabase.from("equipos").insert(equipoConFechas)
167:                 fetchEquipos()
168:                 onSuccess()
169:             } catch (e: Exception) {
170:                 errorMessage = "Error al guardar: ${e.message}"
171:             } finally {
172:                 isLoading = false
173:             }
174:         }
175:     }
176: 
177:     fun actualizarEquipo(
178:         equipo: Equipo,
179:         oldImagenUrl: String?,
180:         nuevaImagenUri: Uri?,
181:         onSuccess: () -> Unit
182:     ) {
183:         viewModelScope.launch {
184:             isLoading = true
185:             try {
186:                 // 1. Si hay una imagen anterior y se subió una nueva, borramos la vieja del Storage
187:                 if (!oldImagenUrl.isNullOrEmpty() && nuevaImagenUri != null) {
188:                     try {
189:                         val fileName = oldImagenUrl.substringAfterLast("/")
190:                         supabase.storage.from("fotos_equipos").delete(fileName)
191:                     } catch (e: Exception) {
192:                         // Continuamos aunque falle el borrado de la imagen vieja
193:                     }
194:                 }
195: 
196:                 // 2. Actualizar en DB
197:                 val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
198:                 val now = sdf.format(Date())
199:                 
200:                 // Preparamos los datos a actualizar (excluimos campos que no cambian como fecha_registro o id)
201:                 val updates = mapOf(
202:                     "no_inventario" to equipo.noInventario,
203:                     "nombre" to equipo.nombre,
204:                     "descripcion" to equipo.descripcion,
205:                     "categoria" to equipo.categoria,
206:                     "marca" to equipo.marca,
207:                     "modelo" to equipo.modelo,
208:                     "estado" to equipo.estado,
209:                     "numero_serie" to equipo.numeroSerie,
210:                     "numerotag" to equipo.numerotag,
211:                     "imagen_url" to equipo.imagenUrl,
212:                     "fecha_modificacion" to now,
213:                     "modificado_por_modelo" to equipo.modificadoPorModelo,
214:                     "modificado_por_nombre" to equipo.modificadoPorNombre
215:                 )
216: 
217:                 supabase.from("equipos").update(updates) {
218:                     filter { eq("id", equipo.id!!) }
219:                 }
220: 
221:                 fetchEquipos()
222:                 onSuccess()
223:             } catch (e: Exception) {
224:                 errorMessage = "Error al actualizar: ${e.message}"
225:             } finally {
226:                 isLoading = false
227:             }
228:         }
229:     }
230: 
231:     fun eliminarEquipoLogico(equipo: Equipo, context: android.content.Context, onSuccess: () -> Unit) {
232:         viewModelScope.launch {
233:             isLoading = true
234:             try {
235:                 // 1. Si tiene imagen, la borramos físicamente de Storage
236:                 if (!equipo.imagenUrl.isNullOrEmpty()) {
237:                     try {
238:                         val fileName = equipo.imagenUrl.substringAfterLast("/")
239:                         supabase.storage.from("fotos_equipos").delete(fileName)
240:                     } catch (e: Exception) {
241:                         // Si falla el borrado de imagen, continuamos con el registro
242:                     }
243:                 }
244: 
245:                 // 2. Actualización lógica en DB
246:                 val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
247:                 val now = sdf.format(Date())
248:                 
249:                 supabase.from("equipos").update(
250:                     mapOf(
251:                         "deleted_at" to now,
252:                         "imagen_url" to null,
253:                         "modificado_por_modelo" to android.os.Build.MODEL,
254:                         "modificado_por_nombre" to (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido")
255:                     )
256:                 ) {
257:                     filter {
258:                         eq("id", equipo.id!!)
259:                     }
260:                 }
261: 
262:                 fetchEquipos()
263:                 onSuccess()
264:             } catch (e: Exception) {
265:                 errorMessage = "Error al eliminar: ${e.message}"
266:             } finally {
267:                 isLoading = false
268:             }
269:         }
270:     }
271: }

(End of file - total 271 lines)
</content>
```

**Tool: read**

**Input:**
```json
{
  "filePath": "C:\\Users\\grobles\\AndroidStudioProjects\\Inventario\\app\\src\\main\\java\\com\\example\\inventario\\MainActivity.kt"
}
```

**Output:**
```
<path>C:\Users\grobles\AndroidStudioProjects\Inventario\app\src\main\java\com\example\inventario\MainActivity.kt</path>
<type>file</type>
<content>
1: package com.example.inventario
2: 
3: import android.Manifest
4: import android.app.Activity
5: import android.content.Context
6: import android.graphics.Bitmap
7: import android.graphics.BitmapFactory
8: import android.net.Uri
9: import android.os.Bundle
10: import android.util.Log
11: import android.widget.Toast
12: import androidx.activity.ComponentActivity
13: import androidx.activity.compose.rememberLauncherForActivityResult
14: import androidx.activity.compose.setContent
15: import androidx.activity.enableEdgeToEdge
16: import androidx.activity.result.contract.ActivityResultContracts
17: import androidx.camera.core.*
18: import androidx.camera.lifecycle.ProcessCameraProvider
19: import androidx.camera.view.LifecycleCameraController
20: import androidx.camera.view.PreviewView
21: import androidx.compose.foundation.background
22: import androidx.compose.foundation.border
23: import androidx.compose.foundation.clickable
24: import androidx.compose.foundation.combinedClickable
25: import androidx.compose.foundation.ExperimentalFoundationApi
26: import androidx.compose.foundation.layout.*
27: import androidx.compose.foundation.lazy.LazyColumn
28: import androidx.compose.foundation.lazy.items
29: import androidx.compose.foundation.rememberScrollState
30: import androidx.compose.foundation.shape.RoundedCornerShape
31: import androidx.compose.foundation.verticalScroll
32: import androidx.compose.material.icons.Icons
33: import androidx.compose.material.icons.filled.*
34: import androidx.compose.material3.*
35: import androidx.compose.runtime.*
36: import androidx.compose.runtime.saveable.rememberSaveable
37: import androidx.compose.ui.Alignment
38: import androidx.compose.ui.Modifier
39: import androidx.compose.ui.draw.clip
40: import androidx.compose.ui.graphics.Color
41: import androidx.compose.ui.layout.ContentScale
42: import androidx.compose.ui.platform.LocalContext
43: import androidx.compose.ui.platform.LocalLifecycleOwner
44: import androidx.compose.foundation.gestures.rememberTransformableState
45: import androidx.compose.foundation.gestures.transformable
46: import androidx.compose.ui.graphics.graphicsLayer
47: import androidx.compose.ui.geometry.Offset
48: import androidx.compose.ui.input.pointer.pointerInput
49: import androidx.compose.foundation.gestures.detectTapGestures
50: import androidx.compose.ui.text.font.FontWeight
51: import androidx.compose.ui.unit.dp
52: import androidx.compose.ui.unit.sp
53: import androidx.compose.ui.viewinterop.AndroidView
54: import androidx.compose.ui.window.Dialog
55: import androidx.compose.ui.window.DialogProperties
56: import androidx.compose.ui.window.PopupProperties
57: import androidx.core.content.ContextCompat
58: import androidx.lifecycle.viewmodel.compose.viewModel
59: import coil3.compose.AsyncImage
60: import com.example.inventario.data.supabase
61: import com.example.inventario.model.Equipo
62: import com.example.inventario.ui.InventarioViewModel
63: import com.example.inventario.ui.theme.InventarioTheme
64: import com.google.mlkit.vision.common.InputImage
65: import com.google.mlkit.vision.text.TextRecognition
66: import com.google.mlkit.vision.text.latin.TextRecognizerOptions
67: import io.github.jan.supabase.storage.storage
68: import kotlinx.coroutines.Dispatchers
69: import kotlinx.coroutines.launch
70: import kotlinx.coroutines.withContext
71: import java.io.ByteArrayOutputStream
72: import java.text.SimpleDateFormat
73: import java.util.*
74: import java.util.concurrent.ExecutorService
75: import java.util.concurrent.Executors
76: 
77: class MainActivity : ComponentActivity() {
78:     @OptIn(ExperimentalMaterial3Api::class)
79:     override fun onCreate(savedInstanceState: Bundle?) {
80:         super.onCreate(savedInstanceState)
81:         enableEdgeToEdge()
82:         setContent {
83:             InventarioTheme {
84:                 val viewModel: InventarioViewModel = viewModel()
85:                 val context = LocalContext.current
86:                 var showForm by rememberSaveable { mutableStateOf(false) }
87:                 var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
88:                 
89:                 LaunchedEffect(Unit) {
90:                     viewModel.fetchEquipos()
91:                 }
92: 
93:                 Scaffold(
94:                     modifier = Modifier.fillMaxSize(),
95:                     topBar = {
96:                         CenterAlignedTopAppBar(
97:                             title = { 
98:                                 Text(
99:                                     if (viewModel.isTrashMode) "Papelera de Reciclaje" else "Inventario de Equipos", 
100:                                     fontWeight = FontWeight.Bold
101:                                 ) 
102:                             },
103:                             colors = TopAppBarDefaults.topAppBarColors(
104:                                 containerColor = if (viewModel.isTrashMode) Color(0xFF424242) else MaterialTheme.colorScheme.primaryContainer,
105:                                 titleContentColor = if (viewModel.isTrashMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
106:                             ),
107:                             actions = {
108:                                 IconButton(onClick = { viewModel.toggleTrashMode() }) {
109:                                     Icon(
110:                                         imageVector = if (viewModel.isTrashMode) Icons.Default.Inventory else Icons.Default.Delete,
111:                                         contentDescription = "Cambiar modo",
112:                                         tint = if (viewModel.isTrashMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
113:                                     )
114:                                 }
115:                                 IconButton(onClick = { (context as? Activity)?.finish() }) {
116:                                     Icon(
117:                                         Icons.Default.Close, 
118:                                         contentDescription = "Cerrar Aplicación",
119:                                         tint = if (viewModel.isTrashMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
120:                                     )
121:                                 }
122:                             }
123:                         )
124:                     },
125:                     floatingActionButton = {
126:                         if (!viewModel.isTrashMode) {
127:                             FloatingActionButton(onClick = { 
128:                                 equipoAEditar = null
129:                                 showForm = true 
130:                             }) {
131:                                 Icon(Icons.Default.Add, contentDescription = "Agregar Equipo")
132:                             }
133:                         }
134:                     }
135:                 ) { innerPadding ->
136:                     Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
137:                         // Cuadro de búsqueda
138:                         OutlinedTextField(
139:                             value = viewModel.searchQuery,
140:                             onValueChange = { viewModel.searchQuery = it },
141:                             modifier = Modifier.fillMaxWidth().padding(16.dp),
142:                             placeholder = { Text(if (viewModel.isTrashMode) "Buscar en Papelera..." else "Buscar por No. Inventario...") },
143:                             leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
144:                             singleLine = true,
145:                             shape = MaterialTheme.shapes.medium
146:                         )
147: 
148:                         Box(modifier = Modifier.fillMaxSize()) {
149:                             if (viewModel.isLoading) {
150:                                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
151:                             } else if (viewModel.errorMessage != null) {
152:                                 // ... (resto del código de error igual)
153:                                 Column(
154:                                     modifier = Modifier.align(Alignment.Center).padding(16.dp),
155:                                     horizontalAlignment = Alignment.CenterHorizontally
156:                                 ) {
157:                                     Text("Error al cargar datos", color = Color.Red, fontWeight = FontWeight.Bold)
158:                                     Text(viewModel.errorMessage!!, modifier = Modifier.padding(top = 8.dp))
159:                                     Button(onClick = { viewModel.fetchEquipos() }, modifier = Modifier.padding(top = 16.dp)) {
160:                                         Text("Reintentar")
161:                                     }
162:                                 }
163:                             } else {
164:                                 if (viewModel.filteredEquipos.isEmpty()) {
165:                                     Text(
166:                                         text = if (viewModel.isTrashMode) "La papelera está vacía" else "No hay equipos registrados",
167:                                         modifier = Modifier.align(Alignment.Center),
168:                                         color = Color.Gray
169:                                     )
170:                                 } else {
171:                                     LazyColumn(
172:                                         modifier = Modifier.fillMaxSize(),
173:                                         contentPadding = PaddingValues(16.dp),
174:                                         verticalArrangement = Arrangement.spacedBy(12.dp)
175:                                     ) {
176:                                         items(viewModel.filteredEquipos) { equipo ->
177:                                             EquipoCard(
178:                                                 equipo = equipo,
179:                                                 onEdit = {
180:                                                     equipoAEditar = equipo
181:                                                     showForm = true
182:                                                 }
183:                                             )
184:                                         }
185:                                     }
186:                                 }
187:                             }
188:                         }
189:                     }
190: 
191:                     if (showForm) {
192:                         FormularioEquipo(
193:                             viewModel = viewModel,
194:                             equipoExistente = equipoAEditar,
195:                             onDismiss = { 
196:                                 showForm = false
197:                                 equipoAEditar = null
198:                             }
199:                         )
200:                     }
201:                 }
202:             }
203:         }
204:     }
205: }
206: 
207: @Composable
208: fun FormularioEquipo(
209:     viewModel: InventarioViewModel, 
210:     equipoExistente: Equipo? = null,
211:     onDismiss: () -> Unit
212: ) {
213:     val context = LocalContext.current
214:     val coroutineScope = rememberCoroutineScope()
215:     
216:     val isEditMode = equipoExistente != null
217: 
218:     val permissionLauncher = rememberLauncherForActivityResult(
219:         ActivityResultContracts.RequestPermission()
220:     ) { isGranted ->
221:         if (!isGranted) {
222:             Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
223:         }
224:     }
225: 
226:     LaunchedEffect(Unit) {
227:         permissionLauncher.launch(Manifest.permission.CAMERA)
228:     }
229: 
230:     var nombre by rememberSaveable { mutableStateOf(equipoExistente?.nombre ?: "") }
231:     var descripcion by rememberSaveable { mutableStateOf(equipoExistente?.descripcion ?: "") }
232:     var categoria by rememberSaveable { mutableStateOf(equipoExistente?.categoria ?: "") }
233:     var marca by rememberSaveable { mutableStateOf(equipoExistente?.marca ?: "") }
234:     var modelo by rememberSaveable { mutableStateOf(equipoExistente?.modelo ?: "") }
235:     var estado by rememberSaveable { mutableStateOf(equipoExistente?.estado ?: "FUNCIONAL") }
236:     var numeroSerie by rememberSaveable { mutableStateOf(equipoExistente?.numeroSerie ?: "") }
237:     var noInventario by rememberSaveable { mutableStateOf(equipoExistente?.noInventario ?: "") }
238:     var numerotag by rememberSaveable { mutableStateOf(equipoExistente?.numerotag ?: "") }
239:     var imageUriString by rememberSaveable { mutableStateOf<String?>(equipoExistente?.imagenUrl) }
240:     val imageUri = if (imageUriString?.startsWith("http") == true) null else imageUriString?.let { Uri.parse(it) }
241:     
242:     var showCameraOCR by rememberSaveable { mutableStateOf(false) }
243:     var ocrTargetField by rememberSaveable { mutableStateOf("") } // "serie" o "tag"
244:     var showCameraPhoto by rememberSaveable { mutableStateOf(false) }
245:     var showDiscardDialog by remember { mutableStateOf(false) }
246: 
247:     // Estados para los menús desplegables
248:     var expandedEstado by remember { mutableStateOf(false) }
249:     val estados = listOf("FUNCIONAL", "OBSOLETO", "DESCOMPUESTO", "PRESTADO")
250: 
251:     val hasChanges = remember(
252:         nombre, descripcion, categoria, marca, modelo, estado, numeroSerie, noInventario, numerotag, imageUriString
253:     ) {
254:         nombre != (equipoExistente?.nombre ?: "") ||
255:         descripcion != (equipoExistente?.descripcion ?: "") ||
256:         categoria != (equipoExistente?.categoria ?: "") ||
257:         marca != (equipoExistente?.marca ?: "") ||
258:         modelo != (equipoExistente?.modelo ?: "") ||
259:         estado != (equipoExistente?.estado ?: "FUNCIONAL") ||
260:         numeroSerie != (equipoExistente?.numeroSerie ?: "") ||
261:         noInventario != (equipoExistente?.noInventario ?: "") ||
262:         numerotag != (equipoExistente?.numerotag ?: "") ||
263:         imageUriString != (equipoExistente?.imagenUrl)
264:     }
265: 
266:     Dialog(
267:         onDismissRequest = { if (hasChanges) showDiscardDialog = true else onDismiss() },
268:         properties = DialogProperties(usePlatformDefaultWidth = false)
269:     ) {
270:         Surface(
271:             modifier = Modifier.fillMaxSize(),
272:             color = MaterialTheme.colorScheme.background
273:         ) {
274:             Column(
275:                 modifier = Modifier
276:                     .fillMaxSize()
277:                     .padding(16.dp)
278:                     .verticalScroll(rememberScrollState())
279:             ) {
280:                 Row(verticalAlignment = Alignment.CenterVertically) {
281:                     IconButton(onClick = { if (hasChanges) showDiscardDialog = true else onDismiss() }) { 
282:                         Icon(Icons.Default.ArrowBack, null) 
283:                     }
284:                     Text(
285:                         if (isEditMode) "Editar Equipo" else "Nuevo Equipo", 
286:                         style = MaterialTheme.typography.titleLarge, 
287:                         fontWeight = FontWeight.Bold
288:                     )
289:                 }
290: 
291:                 Spacer(modifier = Modifier.height(16.dp))
292: 
293:                 // Foto del equipo
294:                 Box(
295:                     modifier = Modifier
296:                         .fillMaxWidth()
297:                         .height(200.dp)
298:                         .clip(RoundedCornerShape(12.dp))
299:                         .background(MaterialTheme.colorScheme.surfaceVariant)
300:                         .clickable { showCameraPhoto = true },
301:                     contentAlignment = Alignment.Center
302:                 ) {
303:                     if (!imageUriString.isNullOrEmpty()) {
304:                         AsyncImage(
305:                             model = imageUriString,
306:                             contentDescription = null,
307:                             modifier = Modifier.fillMaxSize(),
308:                             contentScale = ContentScale.Crop
309:                         )
310:                     } else {
311:                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
312:                             Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(48.dp))
313:                             Text("Tomar foto del equipo")
314:                         }
315:                     }
316:                 }
317: 
318:                 Spacer(modifier = Modifier.height(16.dp))
319: 
320:                 // No. Inventario (Validación 8 caracteres: 3 letras + 5 números)
321:                 val isDuplicated = viewModel.isNoInventarioDuplicado(noInventario, equipoExistente?.id)
322:                 OutlinedTextField(
323:                     value = noInventario,
324:                     onValueChange = { 
325:                         val input = it.uppercase().take(8)
326:                         noInventario = input
327:                         if (input.length == 3 && !isEditMode) {
328:                             noInventario = viewModel.sugerirSiguienteNumero(input)
329:                         }
330:                     },
331:                     label = { Text("No. Inventario (Ej: ABC00001)") },
332:                     modifier = Modifier.fillMaxWidth(),
333:                     isError = (noInventario.length != 8 && noInventario.isNotEmpty()) || isDuplicated,
334:                     supportingText = {
335:                         if (isDuplicated) Text("¡Este número de inventario ya existe!", color = Color.Red)
336:                         else if (noInventario.length > 0 && noInventario.length != 8) Text("Debe tener 8 caracteres")
337:                     }
338:                 )
339: 
340:                 CampoTextoMayusculas(nombre, "Nombre del Equipo") { nombre = it }
341:                 CampoTextoMayusculas(descripcion, "Descripción") { descripcion = it }
342:                 
343:                 // Categoría (Sugerencias)
344:                 ExposedDropdownSugerencias(
345:                     value = categoria,
346:                     label = "Categoría",
347:                     options = viewModel.categoriasExistentes,
348:                     onValueChange = { categoria = it.uppercase() }
349:                 )
350: 
351:                 ExposedDropdownSugerencias(
352:                     value = marca,
353:                     label = "Marca",
354:                     options = viewModel.marcasExistentes,
355:                     onValueChange = { marca = it.uppercase() }
356:                 )
357: 
358:                 ExposedDropdownSugerencias(
359:                     value = modelo,
360:                     label = "Modelo",
361:                     options = viewModel.modelosExistentes,
362:                     onValueChange = { modelo = it.uppercase() }
363:                 )
364: 
365:                 // Estado (Combo fijo)
366:                 Box {
367:                     OutlinedTextField(
368:                         value = estado,
369:                         onValueChange = {},
370:                         readOnly = true,
371:                         label = { Text("Estado") },
372:                         modifier = Modifier.fillMaxWidth(),
373:                         trailingIcon = {
374:                             IconButton(onClick = { expandedEstado = true }) {
375:                                 Icon(Icons.Default.ArrowDropDown, null)
376:                             }
377:                         }
378:                     )
379:                     DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
380:                         estados.forEach { opt ->
381:                             DropdownMenuItem(
382:                                 text = { Text(opt) },
383:                                 onClick = { estado = opt; expandedEstado = false }
384:                             )
385:                         }
386:                     }
387:                 }
388: 
389:                 // Número Tag con OCR
390:                 CampoConOCR(
391:                     value = numerotag,
392:                     label = "Número Tag",
393:                     onValueChange = { numerotag = it.uppercase() },
394:                     onOCRClick = { ocrTargetField = "tag"; showCameraOCR = true }
395:                 )
396: 
397:                 // Número de Serie con OCR
398:                 CampoConOCR(
399:                     value = numeroSerie,
400:                     label = "Número de Serie",
401:                     onValueChange = { numeroSerie = it.uppercase() },
402:                     onOCRClick = { ocrTargetField = "serie"; showCameraOCR = true }
403:                 )
404: 
405:                 Spacer(modifier = Modifier.height(24.dp))
406: 
407:                 Button(
408:                     onClick = {
409:                         if (noInventario.length == 8 && !isDuplicated) {
410:                             coroutineScope.launch {
411:                                 // Si hay una nueva imagen (uri local), la subimos
412:                                 val finalUrl = if (imageUri != null) {
413:                                     subirImagen(context, imageUri)
414:                                 } else {
415:                                     imageUriString // Mantenemos la URL actual si es de internet
416:                                 }
417: 
418:                                 val equipoData = Equipo(
419:                                     id = equipoExistente?.id,
420:                                     nombre = nombre,
421:                                     descripcion = descripcion,
422:                                     categoria = categoria,
423:                                     marca = marca,
424:                                     modelo = modelo,
425:                                     estado = estado,
426:                                     numeroSerie = numeroSerie,
427:                                     numerotag = numerotag,
428:                                     noInventario = noInventario,
429:                                     imagenUrl = finalUrl,
430:                                     creadoPorModelo = equipoExistente?.creadoPorModelo ?: android.os.Build.MODEL,
431:                                     creadoPorNombre = equipoExistente?.creadoPorNombre ?: (android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido"),
432:                                     modificadoPorModelo = android.os.Build.MODEL,
433:                                     modificadoPorNombre = android.provider.Settings.Global.getString(context.contentResolver, "device_name") ?: "Desconocido"
434:                                 )
435: 
436:                                 if (isEditMode) {
437:                                     viewModel.actualizarEquipo(equipoData, equipoExistente?.imagenUrl, imageUri) {
438:                                         onDismiss()
439:                                         Toast.makeText(context, "Actualizado exitosamente", Toast.LENGTH_SHORT).show()
440:                                     }
441:                                 } else {
442:                                     viewModel.guardarEquipo(equipoData, null) {
443:                                         onDismiss()
444:                                         Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
445:                                     }
446:                                 }
447:                             }
448:                         } else {
449:                             val msg = if (isDuplicated) "Número de inventario ya existe" else "Revisa los campos obligatorios"
450:                             Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
451:                         }
452:                     },
453:                     modifier = Modifier.fillMaxWidth(),
454:                     enabled = !viewModel.isLoading
455:                 ) {
456:                     if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
457:                     else Text(if (isEditMode) "ACTUALIZAR DATOS" else "GUARDAR EQUIPO")
458:                 }
459:             }
460:         }
461:     }
462: 
463:     if (showDiscardDialog) {
464:         AlertDialog(
465:             onDismissRequest = { showDiscardDialog = false },
466:             title = { Text("¿Descartar cambios?") },
467:             text = { Text("Tienes cambios sin guardar. ¿Deseas salir de todas formas?") },
468:             confirmButton = {
469:                 TextButton(onClick = { 
470:                     showDiscardDialog = false
471:                     onDismiss() 
472:                 }) { Text("SÍ, DESCARTAR", color = Color.Red) }
473:             },
474:             dismissButton = {
475:                 TextButton(onClick = { showDiscardDialog = false }) { Text("CONTINUAR EDITANDO") }
476:             }
477:         )
478:     }
479: 
480:     if (showCameraOCR) {
481:         CameraOCRDialog(
482:             onResult = { result ->
483:                 if (ocrTargetField == "serie") numeroSerie = result.uppercase()
484:                 else if (ocrTargetField == "tag") numerotag = result.uppercase()
485:                 showCameraOCR = false
486:             },
487:             onDismiss = { showCameraOCR = false }
488:         )
489:     }
490: 
491:     if (showCameraPhoto) {
492:         CameraPhotoDialog(
493:             onCaptured = { uri ->
494:                 imageUriString = uri.toString()
495:                 showCameraPhoto = false
496:             },
497:             onDismiss = { showCameraPhoto = false }
498:         )
499:     }
500: }
501: 
502: @Composable
503: fun CampoTextoMayusculas(value: String, label: String, onValueChange: (String) -> Unit) {
504:     OutlinedTextField(
505:         value = value,
506:         onValueChange = { onValueChange(it.uppercase()) },
507:         label = { Text(label) },
508:         modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
509:     )
510: }
511: 
512: @Composable
513: fun CampoConOCR(value: String, label: String, onValueChange: (String) -> Unit, onOCRClick: () -> Unit) {
514:     OutlinedTextField(
515:         value = value,
516:         onValueChange = { onValueChange(it.uppercase()) },
517:         label = { Text(label) },
518:         modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
519:         trailingIcon = {
520:             IconButton(onClick = onOCRClick) {
521:                 Icon(Icons.Default.QrCodeScanner, contentDescription = "OCR")
522:             }
523:         }
524:     )
525: }
526: 
527: @OptIn(ExperimentalMaterial3Api::class)
528: @Composable
529: fun ExposedDropdownSugerencias(value: String, label: String, options: List<String>, onValueChange: (String) -> Unit) {
530:     var expanded by remember { mutableStateOf(false) }
531:     
532:     Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
533:         OutlinedTextField(
534:             value = value,
535:             onValueChange = { onValueChange(it); expanded = true },
536:             label = { Text(label) },
537:             modifier = Modifier.fillMaxWidth(),
538:             trailingIcon = {
539:                 IconButton(onClick = { expanded = !expanded }) {
540:                     Icon(Icons.Default.ArrowDropDown, null)
541:                 }
542:             }
543:         )
544:         if (expanded && options.isNotEmpty()) {
545:             DropdownMenu(
546:                 expanded = expanded,
547:                 onDismissRequest = { expanded = false },
548:                 properties = PopupProperties(focusable = false)
549:             ) {
550:                 options.filter { it.contains(value, ignoreCase = true) }.forEach { opt ->
551:                     DropdownMenuItem(
552:                         text = { Text(opt) },
553:                         onClick = { onValueChange(opt); expanded = false }
554:                     )
555:                 }
556:             }
557:         }
558:     }
559: }
560: 
561: @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
562: @Composable
563: fun CameraOCRDialog(onResult: (String) -> Unit, onDismiss: () -> Unit) {
564:     val context = LocalContext.current
565:     val lifecycleOwner = LocalLifecycleOwner.current
566:     val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
567:     
568:     var detectedOptions by remember { mutableStateOf<List<String>>(emptyList()) }
569:     var isSelecting by remember { mutableStateOf(false) }
570:     var currentLiveText by remember { mutableStateOf("") }
571:     
572:     DisposableEffect(Unit) {
573:         onDispose {
574:             val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
575:             try {
576:                 val cameraProvider = cameraProviderFuture.get()
577:                 cameraProvider.unbindAll()
578:             } catch (e: Exception) {
579:                 Log.e("OCR", "Error unbinding camera", e)
580:             }
581:         }
582:     }
583: 
584:     Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
585:         Box(modifier = Modifier.fillMaxSize().background(if (isSelecting) MaterialTheme.colorScheme.background else Color.Black)) {
586:             if (!isSelecting) {
587:                 AndroidView(
588:                     modifier = Modifier.fillMaxSize(),
589:                     factory = { ctx ->
590:                         val previewView = PreviewView(ctx)
591:                         val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
592:                         cameraProviderFuture.addListener({
593:                             val cameraProvider = cameraProviderFuture.get()
594:                             val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
595:                             
596:                             val imageAnalyzer = ImageAnalysis.Builder()
597:                                 .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
598:                                 .build()
599:                                 .also {
600:                                     it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
601:                                         @androidx.camera.core.ExperimentalGetImage
602:                                         val mediaImage = imageProxy.image
603:                                         if (mediaImage != null) {
604:                                             val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
605:                                             recognizer.process(image)
606:                                                 .addOnSuccessListener { visionText ->
607:                                                     // Filtrar bloques en el ROI central
608:                                                     val roiBlocks = visionText.textBlocks.filter { block ->
609:                                                         val box = block.boundingBox ?: return@filter false
610:                                                         val centerX = box.centerX()
611:                                                         val centerY = box.centerY()
612:                                                         
613:                                                         centerX > image.width * 0.20 && centerX < image.width * 0.80 &&
614:                                                         centerY > image.height * 0.30 && centerY < image.height * 0.70
615:                                                     }
616:                                                     
617:                                                     // Guardamos todas las líneas de texto encontradas en el centro
618:                                                     val allStrings = roiBlocks.flatMap { block -> 
619:                                                         block.lines.map { it.text.trim().uppercase() } 
620:                                                     }.filter { it.length > 2 }.distinct()
621:                                                     
622:                                                     detectedOptions = allStrings
623:                                                     currentLiveText = allStrings.firstOrNull() ?: ""
624:                                                 }
625:                                                 .addOnCompleteListener { imageProxy.close() }
626:                                         } else {
627:                                             imageProxy.close()
628:                                         }
629:                                     }
630:                                 }
631: 
632:                             try {
633:                                 cameraProvider.unbindAll()
634:                                 cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
635:                             } catch (e: Exception) {
636:                                 Log.e("OCR", "Binding failed", e)
637:                             }
638:                         }, ContextCompat.getMainExecutor(ctx))
639:                         previewView
640:                     }
641:                 )
642:                 
643:                 // Guía visual
644:                 Box(
645:                     modifier = Modifier
646:                         .size(width = 320.dp, height = 140.dp)
647:                         .align(Alignment.Center)
648:                         .border(2.dp, if (detectedOptions.isNotEmpty()) Color.Green else Color.White, RoundedCornerShape(12.dp))
649:                         .background(Color.White.copy(alpha = 0.1f))
650:                 )
651: 
652:                 Column(
653:                     modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
654:                     horizontalAlignment = Alignment.CenterHorizontally
655:                 ) {
656:                     if (currentLiveText.isNotEmpty()) {
657:                         Text(
658:                             "Detectado: $currentLiveText",
659:                             color = Color.White,
660:                             modifier = Modifier.background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp)).padding(8.dp)
661:                         )
662:                         Spacer(modifier = Modifier.height(16.dp))
663:                     }
664:                     
665:                     Button(
666:                         onClick = { if (detectedOptions.isNotEmpty()) isSelecting = true },
667:                         enabled = detectedOptions.isNotEmpty(),
668:                         modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
669:                         shape = RoundedCornerShape(30.dp)
670:                     ) {
671:                         Text("ESCANEAR ÁREA", fontWeight = FontWeight.Bold)
672:                     }
673:                 }
674:             } else {
675:                 // Pantalla de selección de resultados
676:                 Column(
677:                     modifier = Modifier.fillMaxSize().padding(24.dp),
678:                     horizontalAlignment = Alignment.CenterHorizontally
679:                 ) {
680:                     Text(
681:                         "Resultados Encontrados", 
682:                         style = MaterialTheme.typography.headlineSmall,
683:                         fontWeight = FontWeight.Bold
684:                     )
685:                     Text(
686:                         "Toca el texto correcto para capturarlo",
687:                         style = MaterialTheme.typography.bodyMedium,
688:                         color = Color.Gray,
689:                         modifier = Modifier.padding(bottom = 24.dp)
690:                     )
691: 
692:                     LazyColumn(
693:                         modifier = Modifier.weight(1f),
694:                         verticalArrangement = Arrangement.spacedBy(12.dp)
695:                     ) {
696:                         items(detectedOptions) { text ->
697:                             OutlinedButton(
698:                                 onClick = { onResult(text) },
699:                                 modifier = Modifier.fillMaxWidth(),
700:                                 shape = RoundedCornerShape(12.dp),
701:                                 contentPadding = PaddingValues(16.dp)
702:                             ) {
703:                                 Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
704:                             }
705:                         }
706:                     }
707: 
708:                     Spacer(modifier = Modifier.height(16.dp))
709:                     
710:                     TextButton(onClick = { isSelecting = false }) {
711:                         Text("VOLVER A INTENTAR")
712:                     }
713:                 }
714:             }
715:             
716:             IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
717:                 Icon(Icons.Default.Close, null, tint = if (isSelecting) Color.Black else Color.White)
718:             }
719:         }
720:     }
721: }
722: 
723: @Composable
724: fun CameraPhotoDialog(onCaptured: (Uri) -> Unit, onDismiss: () -> Unit) {
725:     val context = LocalContext.current
726:     val lifecycleOwner = LocalLifecycleOwner.current
727:     val cameraController = remember { LifecycleCameraController(context) }
728:     
729:     Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
730:         Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
731:             AndroidView(
732:                 factory = { ctx ->
733:                     PreviewView(ctx).apply {
734:                         controller = cameraController
735:                         cameraController.bindToLifecycle(lifecycleOwner)
736:                     }
737:                 },
738:                 modifier = Modifier.fillMaxSize()
739:             )
740: 
741:             Button(
742:                 onClick = {
743:                     val file = java.io.File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
744:                     val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
745:                     cameraController.takePicture(
746:                         outputOptions,
747:                         ContextCompat.getMainExecutor(context),
748:                         object : ImageCapture.OnImageSavedCallback {
749:                             override fun onImageSaved(output: ImageCapture.OutputFileResults) {
750:                                 onCaptured(Uri.fromFile(file))
751:                             }
752:                             override fun onError(exc: ImageCaptureException) {
753:                                 Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
754:                             }
755:                         }
756:                     )
757:                 },
758:                 modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).size(80.dp),
759:                 shape = RoundedCornerShape(40.dp)
760:             ) {
761:                 Icon(Icons.Default.Camera, null, modifier = Modifier.size(40.dp))
762:             }
763: 
764:             IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
765:                 Icon(Icons.Default.Close, null, tint = Color.White)
766:             }
767:         }
768:     }
769: }
770: 
771: suspend fun subirImagen(context: Context, uri: Uri): String? {
772:     return withContext(Dispatchers.IO) {
773:         val startTime = System.currentTimeMillis()
774:         try {
775:             // Optimización 1: Obtener dimensiones sin cargar el bitmap completo en memoria
776:             val options = BitmapFactory.Options().apply {
777:                 inJustDecodeBounds = true
778:             }
779:             context.contentResolver.openInputStream(uri)?.use { 
780:                 BitmapFactory.decodeStream(it, null, options)
781:             }
782:             
783:             // Optimización 2: Calcular el factor de escala (inSampleSize)
784:             // Limitamos la imagen a un máximo de 1200px para balancear calidad y memoria
785:             options.inSampleSize = calculateInSampleSize(options, 1200, 1200)
786:             options.inJustDecodeBounds = false
787:             
788:             // Optimización 3: Decodificar el bitmap con el tamaño reducido
789:             val bitmap = context.contentResolver.openInputStream(uri)?.use {
790:                 BitmapFactory.decodeStream(it, null, options)
791:             } ?: return@withContext null
792: 
793:             val bos = ByteArrayOutputStream()
794:             // Compresión eficiente al 50%
795:             bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
796:             val bytes = bos.toByteArray()
797:             
798:             // Liberar memoria del bitmap inmediatamente
799:             bitmap.recycle()
800:             
801:             val fileName = "equipo_${System.currentTimeMillis()}.jpg"
802:             supabase.storage.from("fotos_equipos").upload(fileName, bytes)
803:             
804:             val duration = System.currentTimeMillis() - startTime
805:             Log.d("Performance", "Imagen procesada y subida en ${duration}ms. Tamaño final: ${bytes.size / 1024}KB")
806:             
807:             // Obtener URL pública
808:             supabase.storage.from("fotos_equipos").publicUrl(fileName)
809:         } catch (e: Exception) {
810:             Log.e("Performance", "Error en subirImagen: ${e.message}")
811:             null
812:         }
813:     }
814: }
815: 
816: /**
817:  * Calcula el factor de escala para reducir el uso de memoria al decodificar Bitmaps.
818:  */
819: fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
820:     val (height: Int, width: Int) = options.outHeight to options.outWidth
821:     var inSampleSize = 1
822: 
823:     if (height > reqHeight || width > reqWidth) {
824:         val halfHeight: Int = height / 2
825:         val halfWidth: Int = width / 2
826:         while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
827:             inSampleSize *= 2
828:         }
829:     }
830:     return inSampleSize
831: }
832: 
833: @OptIn(ExperimentalFoundationApi::class)
834: @Composable
835: fun EquipoCard(equipo: Equipo, onEdit: () -> Unit) {
836:     val viewModel: InventarioViewModel = viewModel()
837:     val context = LocalContext.current
838:     var showZoom by remember { mutableStateOf(false) }
839:     var showDeleteDialog by remember { mutableStateOf(false) }
840:     var showRestoreDialog by remember { mutableStateOf(false) }
841:     var showPermanentDeleteDialog by remember { mutableStateOf(false) }
842: 
843:     Card(
844:         modifier = Modifier
845:             .fillMaxWidth()
846:             .combinedClickable(
847:                 onClick = { /* Click normal */ },
848:                 onLongClick = { 
849:                     if (viewModel.isTrashMode) {
850:                         showRestoreDialog = true
851:                     } else {
852:                         showDeleteDialog = true 
853:                     }
854:                 }
855:             )
856:             .border(
857:                 width = if (viewModel.isTrashMode) 1.dp else 0.dp,
858:                 color = if (viewModel.isTrashMode) Color.Red.copy(alpha = 0.5f) else Color.Transparent,
859:                 shape = RoundedCornerShape(12.dp)
860:             ),
861:         elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
862:         colors = CardDefaults.cardColors(
863:             containerColor = if (viewModel.isTrashMode) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.surface
864:         )
865:     ) {
866:         Column(modifier = Modifier.padding(16.dp)) {
867:             Row(
868:                 modifier = Modifier.fillMaxWidth(),
869:                 horizontalArrangement = Arrangement.SpaceBetween,
870:                 verticalAlignment = Alignment.CenterVertically
871:             ) {
872:                 Text(
873:                     text = if (viewModel.isTrashMode) "EQUIPO ELIMINADO" else "INFORMACIÓN DEL EQUIPO",
874:                     style = MaterialTheme.typography.labelSmall,
875:                     color = if (viewModel.isTrashMode) Color.Red else MaterialTheme.colorScheme.secondary,
876:                     fontWeight = FontWeight.Bold
877:                 )
878:                 
879:                 if (viewModel.isTrashMode) {
880:                     Row {
881:                         IconButton(onClick = { showRestoreDialog = true }) {
882:                             Icon(Icons.Default.RestoreFromTrash, "Restaurar", tint = Color.DarkGray)
883:                         }
884:                         IconButton(onClick = { showPermanentDeleteDialog = true }) {
885:                             Icon(Icons.Default.DeleteForever, "Borrar Permanente", tint = Color.Red)
886:                         }
887:                     }
888:                 } else {
889:                     Row(verticalAlignment = Alignment.CenterVertically) {
890:                         IconButton(onClick = onEdit) {
891:                             Icon(
892:                                 imageVector = Icons.Default.Edit,
893:                                 contentDescription = "Editar",
894:                                 tint = MaterialTheme.colorScheme.primary,
895:                                 modifier = Modifier.size(20.dp)
896:                             )
897:                         }
898:                         Icon(
899:                             imageVector = Icons.Default.Delete,
900:                             contentDescription = "Mantén presionado para borrar",
901:                             tint = Color.Gray.copy(alpha = 0.3f),
902:                             modifier = Modifier.size(16.dp)
903:                         )
904:                     }
905:                 }
906:             }
907:             HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
908:             
909:             // ... (Imagen y campos igual)
910:             if (!equipo.imagenUrl.isNullOrEmpty()) {
911:                 AsyncImage(
912:                     model = equipo.imagenUrl,
913:                     contentDescription = "Imagen del equipo",
914:                     modifier = Modifier
915:                         .fillMaxWidth()
916:                         .height(200.dp)
917:                         .padding(vertical = 8.dp)
918:                         .clip(RoundedCornerShape(8.dp))
919:                         .clickable { if (!viewModel.isTrashMode) showZoom = true },
920:                     contentScale = ContentScale.Crop
921:                 )
922:             }
923:             // ... (campos de datos)
924: 
925:             CampoDato("No. Inventario:", equipo.noInventario)
926:             CampoDato("Nombre:", equipo.nombre)
927:             CampoDato("Descripción:", equipo.descripcion)
928:             CampoDato("Categoría:", equipo.categoria)
929:             CampoDato("Marca:", equipo.marca)
930:             CampoDato("Modelo:", equipo.modelo)
931:             CampoDato("Estado:", equipo.estado)
932:             CampoDato("Numero Tag:", equipo.numerotag)
933:             CampoDato("Número de Serie:", equipo.numeroSerie)
934:             CampoDato("Fecha Registro:", equipo.fechaRegistro)
935:             CampoDato("Fecha Modificación:", equipo.fechaModificacion)
936:             
937:             HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
938:             Text("Registro de Auditoría", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
939:             CampoDato("Creado por (Modelo):", equipo.creadoPorModelo)
940:             CampoDato("Creado por (Nombre):", equipo.creadoPorNombre)
941:             CampoDato("Modif. por (Modelo):", equipo.modificadoPorModelo)
942:             CampoDato("Modif. por (Nombre):", equipo.modificadoPorNombre)
943:         }
944:     }
945: 
946:     if (showDeleteDialog) {
947:         AlertDialog(
948:             onDismissRequest = { showDeleteDialog = false },
949:             title = { Text("¿Eliminar equipo?") },
950:             text = { Text("Esta acción ocultará el equipo y liberará el espacio de su imagen. ¿Deseas continuar?") },
951:             confirmButton = {
952:                 TextButton(
953:                     onClick = {
954:                         viewModel.eliminarEquipoLogico(equipo, context) {
955:                             showDeleteDialog = false
956:                             Toast.makeText(context, "Equipo eliminado", Toast.LENGTH_SHORT).show()
957:                         }
958:                     }
959:                 ) {
960:                     Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
961:                 }
962:             },
963:             dismissButton = {
964:                 TextButton(onClick = { showDeleteDialog = false }) {
965:                     Text("CANCELAR")
966:                 }
967:             }
968:         )
969:     }
970: 
971:     if (showRestoreDialog) {
972:         AlertDialog(
973:             onDismissRequest = { showRestoreDialog = false },
974:             title = { Text("¿Restaurar equipo?") },
975:             text = { Text("El equipo volverá a aparecer en la lista principal.") },
976:             confirmButton = {
977:                 TextButton(onClick = {
978:                     viewModel.restaurarEquipo(equipo, context) {
979:                         showRestoreDialog = false
980:                         Toast.makeText(context, "Equipo restaurado", Toast.LENGTH_SHORT).show()
981:                     }
982:                 }) { Text("RESTAURAR", fontWeight = FontWeight.Bold) }
983:             },
984:             dismissButton = {
985:                 TextButton(onClick = { showRestoreDialog = false }) { Text("CANCELAR") }
986:             }
987:         )
988:     }
989: 
990:     if (showPermanentDeleteDialog) {
991:         AlertDialog(
992:             onDismissRequest = { showPermanentDeleteDialog = false },
993:             title = { Text("¿ELIMINAR DEFINITIVAMENTE?") },
994:             text = { Text("Esta acción es IRREVERSIBLE. El registro desaparecerá de la base de datos para siempre.") },
995:             confirmButton = {
996:                 TextButton(onClick = {
997:                     viewModel.eliminarFisico(equipo) {
998:                         showPermanentDeleteDialog = false
999:                         Toast.makeText(context, "Eliminado permanentemente", Toast.LENGTH_SHORT).show()
1000:                     }
1001:                 }) { Text("ELIMINAR PARA SIEMPRE", color = Color.Red, fontWeight = FontWeight.Bold) }
1002:             },
1003:             dismissButton = {
1004:                 TextButton(onClick = { showPermanentDeleteDialog = false }) { Text("CANCELAR") }
1005:             }
1006:         )
1007:     }
1008: 
1009:     if (showZoom && !equipo.imagenUrl.isNullOrEmpty()) {
1010:         var scale by remember { mutableStateOf(1f) }
1011:         var offset by remember { mutableStateOf(Offset.Zero) }
1012:         val state = rememberTransformableState { zoomChange, offsetChange, _ ->
1013:             scale *= zoomChange
1014:             offset += offsetChange
1015:         }
1016: 
1017:         Dialog(
1018:             onDismissRequest = { showZoom = false },
1019:             properties = DialogProperties(usePlatformDefaultWidth = false)
1020:         ) {
1021:             Box(
1022:                 modifier = Modifier
1023:                     .fillMaxSize()
1024:                     .background(Color.Black)
1025:                     .pointerInput(Unit) {
1026:                         detectTapGestures(onTap = { showZoom = false })
1027:                     },
1028:                 contentAlignment = Alignment.Center
1029:             ) {
1030:                 AsyncImage(
1031:                     model = equipo.imagenUrl,
1032:                     contentDescription = null,
1033:                     modifier = Modifier
1034:                         .fillMaxSize()
1035:                         .graphicsLayer(
1036:                             scaleX = maxOf(1f, scale),
1037:                             scaleY = maxOf(1f, scale),
1038:                             translationX = offset.x,
1039:                             translationY = offset.y
1040:                         )
1041:                         .transformable(state = state)
1042:                         .pointerInput(Unit) {
1043:                             // Este pointerInput vacío evita que el tap del fondo
1044:                             // se active cuando tocas la imagen directamente
1045:                             detectTapGestures(onTap = { /* No hacer nada al tocar la imagen */ })
1046:                         },
1047:                     contentScale = ContentScale.Fit
1048:                 )
1049:                 IconButton(
1050:                     onClick = { showZoom = false },
1051:                     modifier = Modifier
1052:                         .align(Alignment.TopEnd)
1053:                         .padding(16.dp)
1054:                         .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
1055:                 ) {
1056:                     Icon(Icons.Default.Close, null, tint = Color.White)
1057:                 }
1058:             }
1059:         }
1060:     }
1061: }
1062: 
1063: @Composable
1064: fun CampoDato(titulo: String, valor: String?) {
1065:     Column(modifier = Modifier.padding(vertical = 2.dp)) {
1066:         Row {
1067:             Text(
1068:                 text = titulo,
1069:                 fontWeight = FontWeight.Bold,
1070:                 modifier = Modifier.width(130.dp),
1071:                 fontSize = 14.sp
1072:             )
1073:             Text(
1074:                 text = valor ?: "Sin datos",
1075:                 color = if (valor == null) Color.Gray else Color.Unspecified,
1076:                 fontSize = 14.sp
1077:             )
1078:         }
1079:     }
1080: }

(End of file - total 1080 lines)
</content>
```

**Tool: read**

**Input:**
```json
{
  "filePath": "C:\\Users\\grobles\\AndroidStudioProjects\\Inventario\\app\\src\\main\\java\\com\\example\\inventario\\data\\SupabaseClient.kt"
}
```

**Output:**
```
<path>C:\Users\grobles\AndroidStudioProjects\Inventario\app\src\main\java\com\example\inventario\data\SupabaseClient.kt</path>
<type>file</type>
<content>
1: package com.example.inventario.data
2: 
3: import io.github.jan.supabase.createSupabaseClient
4: import io.github.jan.supabase.postgrest.Postgrest
5: import io.github.jan.supabase.storage.Storage
6: 
7: val supabase = createSupabaseClient(
8:     supabaseUrl = "https://gtzqekmewuwxcpyrojpn.supabase.co",
9:     supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0enFla21ld3V3eGNweXJvanBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4OTMyMjgsImV4cCI6MjA5MDQ2OTIyOH0.h05nkqSl61K7ChVXe3rOIPNvDMDSBaAkCr7g1Myx4fA" // TODO: Reemplazar con la clave real
10: ) {
11:     install(Postgrest)
12:     install(Storage)
13: }

(End of file - total 13 lines)
</content>
```

**Tool: read**

**Input:**
```json
{
  "filePath": "C:\\Users\\grobles\\AndroidStudioProjects\\Inventario\\app\\src\\main\\java\\com\\example\\inventario\\model\\Equipo.kt"
}
```

**Output:**
```
<path>C:\Users\grobles\AndroidStudioProjects\Inventario\app\src\main\java\com\example\inventario\model\Equipo.kt</path>
<type>file</type>
<content>
1: package com.example.inventario.model
2: 
3: import kotlinx.serialization.SerialName
4: import kotlinx.serialization.Serializable
5: 
6: @Serializable
7: data class Equipo(
8:     @SerialName("id")
9:     val id: String? = null,
10:     @SerialName("no_inventario")
11:     val noInventario: String? = null,
12:     @SerialName("nombre")
13:     val nombre: String? = null,
14:     @SerialName("descripcion")
15:     val descripcion: String? = null,
16:     @SerialName("categoria")
17:     val categoria: String? = null,
18:     @SerialName("marca")
19:     val marca: String? = null,
20:     @SerialName("modelo")
21:     val modelo: String? = null,
22:     @SerialName("estado")
23:     val estado: String? = null,
24:     @SerialName("numero_serie")
25:     val numeroSerie: String? = null,
26:     @SerialName("numerotag")
27:     val numerotag: String? = null,
28:     @SerialName("fecha_registro")
29:     val fechaRegistro: String? = null,
30:     @SerialName("fecha_modificacion")
31:     val fechaModificacion: String? = null,
32:     @SerialName("imagen_url")
33:     val imagenUrl: String? = null,
34:     @SerialName("creado_por_modelo")
35:     val creadoPorModelo: String? = null,
36:     @SerialName("creado_por_nombre")
37:     val creadoPorNombre: String? = null,
38:     @SerialName("modificado_por_modelo")
39:     val modificadoPorModelo: String? = null,
40:     @SerialName("modificado_por_nombre")
41:     val modificadoPorNombre: String? = null,
42:     @SerialName("deleted_at")
43:     val deletedAt: String? = null
44: )

(End of file - total 44 lines)
</content>
```

---

## Assistant (Plan · Big Pickle · 9.2s)

_Thinking:_

Ahora tengo una buena visión del proyecto. Voy a analizarlo y proporcionar sugerencias de mejora concretas.




## Resumen del Proyecto

Tu app de inventario Android es bastante completa: MVVM + Compose + Supabase + OCR con CameraX/ML Kit. Aquí van mis sugerencias de mejora:

---

### **1. SEGURIDAD (Crítico)**
- **SupabaseClient.kt:9** - La clave API está hardcodeada. Mover a `local.properties` o variables de entorno y nunca commitearla.
- Considera usar **RLS policies** en Supabase para proteger datos.

### **2. ARQUITECTURA**
- **Separar capas**: El código de UI (1080 líneas) es muy largo. Extraer a:
  - `screens/` → `InventarioScreen.kt`, `FormularioScreen.kt`
  - `components/` → `EquipoCard.kt`, `CamposFormulario.kt`, `DialogosCamara.kt`
  - `utils/` → `ImageUtils.kt`, `DateUtils.kt`

### **3. GESTIÓN DE ESTADO**
- Usar **StateFlow** en lugar de `mutableStateListOf`/`mutableStateOf` en el ViewModel para mejor testabilidad.
- Implementar **UiState sealed class** para estados (Loading, Success, Error, Empty).

### **4. INYECCIÓN DE DEPENDENCIAS**
- Agregar **Hilt/Koin** para inyectar `SupabaseClient` y otras dependencias.

### **5. MEJORAS DE UX**
- **Pull-to-refresh** en la lista principal.
- **Paginación** si hay muchos equipos (actualmente carga todo).
- Agregar **Snackbar** para confirmaciones en lugar de Toast.
- Implementar **animaciones de transición** entre pantallas.

### **6. VALIDACIONES**
- Crear un **ViewModel separado para el formulario** con validación más robusta.
- Agregar **regex** para número de inventario (3 letras + 5 números).
- Validar que `nombre` y `noInventario` sean obligatorios.

### **7. MANTENIMIENTO**
- Agregar **logging централизованный** ( Timber o similar).
- Implementar **manejo de errores centralizado**.
- Agregar **tests unitarios** del ViewModel.

### **8. OPTIMIZACIONES**
- El ViewModel hace `fetchEquipos()` en cada operación. Considerar cache local con **Room** para offline.
- Lazy loading de imágenes con placeholder.
- Considerar **Paging 3** para listas grandes.

---

¿Quieres que profundice en alguna de estas mejoras o que te ayude a implementar alguna específica?

---

