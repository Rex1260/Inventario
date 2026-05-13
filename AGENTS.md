# AGENTS.md

## Session History System

Este proyecto tiene un sistema de historial de sesiones para agentes LLM. Permite que cualquier agente (OpenCode, Gemini CLI, Claude Code, etc.) pueda continuar el trabajo donde otro agente lo dejó.

### Tablas de Sesión

| Tabla | Descripción |
|-------|-------------|
| `session_history` | Registro de sesiones con contexto completo |
| `session_messages` | Mensajes de cada sesión |
| `llm_agents_registry` | Registro de agentes soportados |

### Funciones Disponibles

| Función | Uso |
|---------|-----|
| `start_session(llm_tool, project_path)` | Iniciar nueva sesión |
| `save_context(session_id, context, instructions, files_md, todo)` | Auto-guardar |
| `end_session(session_id, summary)` | Terminar sesión |
| `get_last_session(project_path)` | Obtener última sesión |
| `resume_session(session_id)` | Continuar sesión |
| `add_session_message(session_id, role, content)` | Agregar mensaje |
| `register_agent(agent_type, version, capabilities)` | Registrar agente |

### Session ID Actual

```
6393f9f4-489f-4d68-b53e-6fcecf31dc97
```

### Instrucciones para Nuevo Agente

Cuando un nuevo agente se conecte a este proyecto:

1. **Verificar sesiones pendientes:**
   ```sql
   SELECT * FROM session_history 
   WHERE project_path = 'C:\Users\grobles\AndroidStudioProjects\Inventario' 
   AND session_end IS NULL;
   ```

2. **Si hay sesión activa, continuar:**
   ```sql
   SELECT resume_session('session_id');
   ```

3. **Crear nueva sesión si no hay activa:**
   ```sql
   SELECT start_session('opencode', 'C:\Users\grobles\AndroidStudioProjects\Inventario');
   ```

### Auto-Guardado

El agente debe guardar contexto periódicamente usando `save_context()`:
- Cada 20 minutos (configurable)
- Antes de cerrar sesión
- Después de completar tareas importantes

#### Edge Function: auto-save-session

Esta función ejecuta auto-guardado capturando contexto de git y guardándolo en la sesión activa.

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/auto_save` | POST | Ejecuta auto-guardado (captura git, actualiza sesión) |
| `/health` | GET | Verifica que la función está activa |

**URL**: `https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/auto-save-session`

**Configuración**: `auto_save_config` (intervalo 20 minutos)

#### Cómo usar auto-guardado

1. **Manual**: El agente puede llamar directamente:
   ```
   curl -X POST https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/auto-save-session/auto_save
   ```

2. **Automático (si pg_cron está habilitado)**:
   - Se ejecuta cada 20 minutos como backup
   - Captura: branch, último commit, cambios sin commit

3. **En código** (usando MCP supabase):
   ```sql
   SELECT save_context(
     'session_id',
     '{"last_auto_save": "timestamp"}'::jsonb,
     '{}'::jsonb,
     '[]'::jsonb,
     '[]'::jsonb
   );
   ```

#### Tabla: auto_save_config

| Campo | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `interval_minutes` | integer | 20 | Intervalo de guardado |
| `capture_git` | boolean | true | Capturar info de git |
| `capture_files_md` | boolean | true | Listar archivos .md |
| `capture_db_state` | boolean | true | Snapshot de BD |

####pg_cron (opcional - si está habilitado)

Para programar auto-guardado cada 20 minutos:

```sql
SELECT cron.schedule(
  'auto-save-inventario',
  '*/20 * * * *',
  $$SELECT net.http_post(
    url:='https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/auto-save-session/auto_save',
    headers:='{"Content-Type":"application/json"}'::jsonb
  )$$
);
```

Si pg_cron no está habilitado, el agente debe llamar manualmente la función de auto-guardado.

### Formato de Contexto

```json
{
  "current_branch": "main",
  "last_commit": "hash",
  "pending_changes": [],
  "todo_list": [],
  "files_md": [],
  "database_state": {},
  "agent_notes": ""
}
```

---

## MCP Configuration (OpenCode)

Este proyecto tiene configurados múltiples MCPs de Supabase con diferente nivel de acceso.

### MCPs Disponibles

| MCP | Tipo | Uso | Auth |
|-----|------|-----|------|
| **supabase** | Local (npx) | Consultas normales, edge functions, logs | Anon/Service Role |
| **mcp-agent** | Remote (Edge Function) | Operaciones con bypassrls | Basic |
| **descope** | Remote | Gestión de tokens y auth | Descope Project |

### Cuándo Usar Cada MCP

| Operación | MCP Recomendado |
|-----------|----------------|
| SELECT simple | `supabase` |
| INSERT/UPDATE con RLS | `supabase` |
| Edge Functions | `supabase` |
| Ver logs, advisors | `supabase` |
| Tablas con RLS restrictiva | `mcp-agent` |
| DDL (CREATE, ALTER, DROP) | `mcp-agent` |
| Migraciones | `mcp-agent` |
| Operaciones que requieren bypassrls | `mcp-agent` |

### Credenciales Importantes

| Dato | Valor |
|------|-------|
| **Supabase project-ref** | `gtzqekmewuwxcpyrojpn` |
| **Descope Project ID** | `P3DaJc3A4ro37nG2IKEDFxBwvufB` |
| **mcp_agent password** | `McP_Agent_Secure_2026!xK9#Lp2$vN8@qR4&mT7*wZ3` |
| **mcp_agent role** | bypassrls habilitado |
| **Edge Function mcp-agent** | `https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/mcp-agent` |

---

## Repo Layout
- No root `package.json` — not a JavaScript monorepo
- `app/`: Android application module (Gradle, Kotlin)
- `web/`: Standalone Next.js 14 project (own `package.json`, `node_modules`)

## Web App (`web/`)
- **Package manager**: npm (uses `package-lock.json` — no yarn/pnpm)
- **Setup**: Run `npm install` in `web/` first (node_modules is gitignored)
- **Commands**:
  - Dev server: `npm run dev` (runs `next dev`)
  - Build: `npm run build`
  - Lint: `npm run lint` (Next.js built-in lint)
- **No test/typecheck**: No test scripts or TypeScript configured
- **Tailwind quirk**: Content paths only include `src/pages/**/*` and `src/components/**/*` — files in `src/lib` will not have Tailwind classes applied
- **No Next.js config**: Uses default settings (no `next.config.js`)
- **Supabase**: Requires `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` env vars (no `.env` files committed)

## Android App
- Gradle wrapper: `gradlew.bat` (Windows) or `./gradlew` (Unix)
- Only `:app` module; no multi-module setup

## Implementation Plan: Dynamic Templates Module

**Goal**: Reusable template system to auto-fill equipment forms, saving time and reducing errors.

### Database Changes Needed

**`plantillas` table (REQUIRED):**
```sql
CREATE TABLE plantillas (
    id SERIAL PRIMARY KEY,
    nombre_plantilla TEXT UNIQUE NOT NULL,
    equipo_nombre TEXT,
    categoria TEXT,
    marca TEXT,
    modelo TEXT,
    descripcion_default TEXT,
    specs_json JSONB DEFAULT '{}'::jsonb,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**`system_dna` table (already exists):**
- Table already present in Supabase with business rules and directives
- Only need to register the action (optional, secondary):

```sql
INSERT INTO system_dna (category, parent_entity, component_key, display_name, description)
VALUES ('ACTION', 'inventario', 'ACT_APPLY_TEMPLATE', 'Cargar Plantilla', 'Lógica para auto-completar el formulario de creación con valores predefinidos.')
ON CONFLICT (component_key) DO NOTHING;
```

### Phases
1. **DB & DNA**: Run SQL above in Supabase, verify tables exist
2. **Dynamic Insertion**: ViewModel accepts template data injection via `applyTemplate(plantilla)`
3. **UI**: Add `TemplateSelector` to "Nuevo Equipo" form header, admin section for template management
4. **Web Sync**: Implement template support in `web/` (CRUD + auto-fill)

### Creating Templates (Two Methods)

**Method 1 - Admin Menu**: Create template from scratch via admin section

**Method 2 - Clone from Equipment** ("Clonar a Plantilla"):
- Available when editing an existing equipment
- Opens dialog asking for unique `nombre_plantilla`
- Creates NEW record in `plantillas` (equipment is NOT modified)

#### Field Mapping: Equipment → Template
| `plantillas` field | Value source (from `equipos`) | Reason |
|-------------------|-------------------------------|--------|
| `nombre_plantilla` | **User provides** | Unique identifier for the template |
| `equipo_nombre` | `nombre` | Predefined equipment name |
| `categoria` | `categoria` | Predefined category |
| `marca` | `marca` | Predefined brand |
| `modelo` | `modelo` | Predefined model |
| `descripcion_default` | `descripcion` | Base description text |
| `specs_json` | `{}` (default) | Optional extras, not from equipment |
| `is_active` | `TRUE` (default) | New template is active by default |

#### Fields NOT Cloned (and why):
- `no_inventario`: Unique per equipment, templates don't predefine it
- `estado`: Templates define equipment type, not operational state
- `numero_serie`, `numerotag`: Unique per physical device
- `imagen_url`: Images are equipment-specific, not template-specific
- Audit fields (`fecha_*`, `creado_por_*`, `modificado_por_*`): Auto-generated on template creation

### Key Details
- Template auto-fills: Nombre, Categoría, Marca, Modelo, Descripción
- Cursor positions to **No. Inventario** after template load (only required manual field)
- Templates sync to **web app** (`web/`) as well

### Verification
1. Select template → verify auto-fill works
2. Edit fields after template load → verify saves correctly
3. New templates appear in next session
