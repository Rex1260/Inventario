# Plan: Subagente Supabase + Descope (Hybrid)

## Objetivo
Crear un subagente en OpenCode especializado en Supabase, usando el MCP existente, e integrar Descope para gestionar servicios externos (token vault) de forma segura.

---

## Configuración Actual

| Servicio | Status | Detalles |
|----------|--------|----------|
| **Supabase MCP** | ✅ Configurado | project-ref: `gtzqekmewuwxcpyrojpn` |
| **Descope Project ID** | ✅ Configurado | `P3DaJc3A4ro37nG2IKEDFxBwvufB` |

---

## Arquitectura Final (Hybrid)

```
OpenCode
    ├── Supabase MCP (existente) → Supabase DB
    │       └── list_tables, execute_sql, apply_migration, deploy_edge_function
    │
    └── Descope MCP → Servicios Externos
            ├── Gmail (Outbound App)
            ├── GitHub (Outbound App)
            └── Otros...
                    └── Token Vault (almacenamiento seguro)
```

---

## FASE 1: Configurar MCP Server en Descope

| Paso | Descripción |
|------|-------------|
| 1.1 | Ir a **Agentic Identity Hub → MCP Servers → New** en Descope Dashboard |
| 1.2 | Configurar nombre y descripción del MCP |
| 1.3 | Obtener el `.well-known` URL para OpenCode |
| 1.4 | Definir scopes básicos: `openid`, `profile` |

---

## FASE 2: Integrar Descope MCP en OpenCode

| Paso | Descripción |
|------|-------------|
| 2.1 | Agregar MCP de Descope en `opencode.json` |
| 2.2 | Configurar con project ID: `P3DaJc3A4ro37nG2IKEDFxBwvufB` |
| 2.3 | Verificar herramientas disponibles |

---

## FASE 3: Configurar Outbound Apps en Descope (Servicios Externos)

| Paso | Descripción |
|------|-------------|
| 3.1 | Crear **Outbound App** para Gmail |
| 3.2 | Crear **Outbound App** para GitHub |
| 3.3 | Configurar scopes por servicio |
| 3.4 | Probar conexión OAuth con cada servicio |

---

## FASE 4: Crear Subagente Supabase

| Paso | Descripción |
|------|-------------|
| 4.1 | Crear `.opencode/agents/supabase.md` |
| 4.2 | Definir permissions: edit=allow, bash=allow |
| 4.3 | Configurar tools: solo Supabase MCP |
| 4.4 | Instrucciones específicas del agente |

---

## FASE 5: Crear Subagente Descope (Opcional)

| Paso | Descripción |
|------|-------------|
| 5.1 | Crear `.opencode/agents/descope.md` |
| 5.2 | Tools: gestión de tokens, conexiones |
| 5.3 | Instrucciones para conectar servicios |

---

## FASE 6: Pruebas

| Paso | Descripción |
|------|-------------|
| 6.1 | Probar Supabase MCP: list_tables, execute_sql |
| 6.2 | Probar Descope MCP: listar herramientas |
| 6.3 | Probar conexión con Gmail (Outbound App) |
| 6.4 | Probar conexión con GitHub (Outbound App) |

---

## MCPs Disponibles en OpenCode (Post-Setup)

| MCP | Herramientas | Uso |
|-----|-------------|-----|
| **supabase** | list_tables, execute_sql, apply_migration, deploy_edge_function, etc. | Base de datos, queries, funciones |
| **descope** | search_docs, gestión de tokens, conexiones | Auth, token vault, políticas |
| **context7** | Documentación | Buscar docs |
| **stitch** | Diseño UI | Stitch MCP |

---

## Supabase MCP - Herramientas Disponibles

| Grupo | Herramientas |
|-------|--------------|
| **Database** | `list_tables`, `list_extensions`, `list_migrations`, `apply_migration`, `execute_sql` |
| **Edge Functions** | `list_edge_functions`, `get_edge_function`, `deploy_edge_function` |
| **Logs** | `get_logs`, `get_advisors` |
| **API** | `get_project_url`, `get_publishable_keys`, `generate_typescript_types` |
| **Docs** | `search_docs` |

---

## Descope MCP - Herramientas Disponibles (Por configurar)

| Grupo | Herramientas |
|-------|--------------|
| **MCP Servers** | Gestión de MCP servers |
| **Connections** | Token vault para servicios externos |
| **Policies** | Políticas de acceso |
| **Docs** | Documentación de Descope |

---

## Requisitos del Usuario

- **Supabase project-ref**: `gtzqekmewuwxcpyrojpn`
- **Descope Project ID**: `P3DaJc3A4ro37nG2IKEDFxBwvufB`
- **Nivel de seguridad**: Medio-Alto
- **Servicios externos**: Gmail, GitHub y más (via Outbound Apps)

---

## Próximo Paso
Comenzar con **FASE 1**: Configurar MCP Server en Descope Dashboard (Agentic Identity Hub → MCP Servers → New)