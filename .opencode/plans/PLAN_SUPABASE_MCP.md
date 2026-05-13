# Plan: MCP Server Custom con Descope + Rol mcp_agent

## Objetivo
Crear un MCP server propio en Supabase Edge Functions, protegido con Descope MCP Auth, con un rol `mcp_agent` en PostgreSQL que tenga privilegios superiores a `anon`.

---

## Configuración Actual

| Servicio | Status | Detalles |
|----------|--------|----------|
| **Supabase MCP (hosted)** | ✅ Configurado | project-ref: `gtzqekmewuwxcpyrojpn` |
| **Descope Project ID** | ✅ Configurado | `P3DaJc3A4ro37nG2IKEDFxBwvufB` |
| **Rol mcp_agent** | ✅ Creado | bypassrls, acceso total |
| **Descope MCP Server** | ✅ Creado | ID: `MS3DdzDIXQaszZU1Twybossko98Cz` |
| **Edge Function mcp-agent** | ✅ Desplegada | URL: `https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/mcp-agent` |

---

## Arquitectura Final

```
OpenCode
    │
    ├── Supabase MCP (hosted) → Consulta rápida, sin auth
    │
    └── Tu MCP Server Custom → Supabase Edge Functions
            │
            ├── Descope MCP Auth (OAuth 2.1)
            │       └── Validación de tokens, scopes, auditoría
            │
            ├── mcp_agent (PostgreSQL role)
            │       └── bypassrls, acceso total a DB
            │
            └── Supabase Database
                    └── Todas las tablas, funciones, migraciones
```

---

## FASE 1: Crear Rol mcp_agent en PostgreSQL ✅

| Paso | Status |
|------|--------|
| Crear rol con login y password | ✅ |
| Permisos de conexión | ✅ |
| Permisos de esquemas | ✅ |
| Permisos en tablas | ✅ |
| Permisos en secuencias | ✅ |
| Permisos en funciones | ✅ |
| BYPASS RLS | ✅ |
| Storage | ✅ |
| Tabla audit_log | ✅ |

**Password**: `McP_Agent_Secure_2026!xK9#Lp2$vN8@qR4&mT7*wZ3`

---

## FASE 2: Configurar MCP Server en Descope ✅

| Paso | Status |
|------|--------|
| Nombre: mcp-agent | ✅ |
| Descope MCP Server ID | ✅ `MS3DdzDIXQaszZU1Twybossko98Cz` |
| Scopes definidos | ✅ |

---

## FASE 3: Crear MCP Server en Supabase Edge Functions ✅

| Paso | Status |
|------|--------|
| Edge Function mcp-agent | ✅ Desplegada |
| URL | ✅ `https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/mcp-agent` |
| Herramientas | ✅ execute_sql, list_tables, get_schema, apply_migration, health_check |
| verify_jwt | ✅ false (por ahora) |

---

## FASE 4: Integrar MCP en OpenCode ✅

| Paso | Status |
|------|--------|
| mcp-agent en opencode.json | ✅ |
| descope en opencode.json | ✅ |

**opencode.json actualizado:**
```json
{
  "mcp": {
    "mcp-agent": {
      "type": "remote",
      "url": "https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/mcp-agent",
      "enabled": true
    },
    "descope": {
      "type": "remote",
      "url": "https://mcp.descope.com/mcp",
      "enabled": true,
      "headers": {
        "DESCOPE_PROJECT_ID": "P3DaJc3A4ro37nG2IKEDFxBwvufB"
      }
    }
  }
}
```

---

## FASE 5: Crear Subagente Supabase ✅

| Paso | Status |
|------|--------|
| .opencode/agents/supabase.md | ✅ Creado |
| ~/.config/opencode/agents/supabase.md | ✅ Creado |
| Permissions | ✅ edit=allow, bash=allow |
| Tools | ✅ mcp_agent_*, supabase_* |

---

## FASE 6: Pruebas (Pendiente)

| Paso | Descripción | Status |
|------|-------------|--------|
| 6.1 | Probar conexión con Descope | ⏳ |
| 6.2 | Probar autenticación OAuth | ⏳ |
| 6.3 | Probar execute_sql con rol mcp_agent | ⏳ |
| 6.4 | Verificar auditoría en audit_log | ⏳ |

---

## MCPs Disponibles en OpenCode

| MCP | Tipo | Uso |
|-----|------|-----|
| **supabase** (hosted) | Local (npx) | Consultas rápidas sin auth |
| **mcp-agent** (custom) | Remote | Acceso completo con Descope |
| **descope** | Remote | Gestión de tokens y políticas |
| **context7** | Remote | Documentación |
| **stitch** | Remote | Diseño UI |

---

## Permisos del Rol mcp_agent

| Permiso | Valor |
|---------|-------|
| **Lectura/Escritura** | ✅ Todas las tablas |
| **Bypass RLS** | ✅ Sí |
| **Migraciones** | ✅ Sí |
| **Edge Functions** | ✅ Deploy/manage |
| **Storage** | ✅ Acceso completo |
| **Auditoría** | ✅ Registro en audit_log |

---

## Archivos Creados

| Archivo | Ubicación |
|---------|-----------|
| Edge Function | `supabase/functions/mcp-agent/index.ts` |
| Subagente (local) | `.opencode/agents/supabase.md` |
| Subagente (global) | `~/.config/opencode/agents/supabase.md` |
| Plan | `.local/share/opencode/plans/PLAN_SUPABASE_MCP.md` |

---

## Próximos Pasos

1. **Probar el MCP** - Invocar el mcp-agent desde OpenCode
2. **Configurar Descope Auth** - Integrar OAuth 2.1 en la Edge Function
3. **Probar execute_sql** - Verificar que mcp_agent funciona
4. **Revisar auditoría** - Ver logs en audit_log

---

## Notas de Seguridad

⚠️ **El rol mcp_agent tiene bypassrls** - acceso total a la base de datos.
⚠️ **No compartir las credenciales** - usar solo para agentes IA.
⚠️ **El password debe rotarse** periódicamente.

---

## Credenciales Importantes

| Dato | Valor |
|------|-------|
| **Supabase project-ref** | `gtzqekmewuwxcpyrojpn` |
| **Descope Project ID** | `P3DaJc3A4ro37nG2IKEDFxBwvufB` |
| **Descope MCP Server ID** | `MS3DdzDIXQaszZU1Twybossko98Cz` |
| **mcp_agent password** | `McP_Agent_Secure_2026!xK9#Lp2$vN8@qR4&mT7*wZ3` |
| **Edge Function URL** | `https://gtzqekmewuwxcpyrojpn.supabase.co/functions/v1/mcp-agent` |