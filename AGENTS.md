# AGENTS.md

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
