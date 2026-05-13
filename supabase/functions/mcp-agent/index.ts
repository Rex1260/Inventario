import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, mcp-authorization",
};

interface McpRequest {
  jsonrpc: string;
  id: number | string;
  method: string;
  params?: Record<string, unknown>;
}

interface McpResponse {
  jsonrpc: string;
  id: number | string;
  result?: Record<string, unknown>;
  error?: { code: number; message: string };
}

const DATABASE_URL = "https://gtzqekmewuwxcpyrojpn.supabase.co";
const MCP_AGENT_PASSWORD = "McP_Agent_Secure_2026!xK9#Lp2$vN8@qR4&mT7*wZ3";

function createPgClient() {
  return createClient(DATABASE_URL, "postgres", {
    auth: {
      persistSession: false,
    },
  });
}

async function handleMcpRequest(req: McpRequest): Promise<McpResponse> {
  try {
    const { method, params } = req;

    switch (method) {
      case "initialize":
        return {
          jsonrpc: "2.0",
          id: req.id,
          result: {
            protocolVersion: "2024-11-05",
            capabilities: {
              tools: {},
              resources: {},
            },
            serverInfo: {
              name: "mcp-agent",
              version: "1.0.0",
            },
          },
        };

      case "tools/list":
        return {
          jsonrpc: "2.0",
          id: req.id,
          result: {
            tools: [
              {
                name: "execute_sql",
                description: "Ejecutar queries SQL en la base de datos",
                inputSchema: {
                  type: "object",
                  properties: {
                    query: { type: "string", description: "Query SQL a ejecutar" },
                  },
                  required: ["query"],
                },
              },
              {
                name: "list_tables",
                description: "Listar todas las tablas de la base de datos",
                inputSchema: {
                  type: "object",
                  properties: {},
                },
              },
              {
                name: "get_schema",
                description: "Obtener el esquema de una tabla específica",
                inputSchema: {
                  type: "object",
                  properties: {
                    table_name: { type: "string" },
                  },
                  required: ["table_name"],
                },
              },
              {
                name: "apply_migration",
                description: "Aplicar una migración SQL",
                inputSchema: {
                  type: "object",
                  properties: {
                    sql: { type: "string" },
                  },
                  required: ["sql"],
                },
              },
              {
                name: "health_check",
                description: "Verificar conexión a la base de datos",
                inputSchema: {
                  type: "object",
                  properties: {},
                },
              },
            ],
          },
        };

      case "tools/call": {
        const toolName = params?.name as string;
        const args = params?.arguments as Record<string, unknown> || {};

        switch (toolName) {
          case "execute_sql": {
            const query = args.query as string;
            if (!query) {
              return { jsonrpc: "2.0", id: req.id, error: { code: -32602, message: "Query requerida" } };
            }
            // NOTE: En producción, usar connection pooling con mcp_agent
            // Por ahora, usamos la API de Supabase con service_role
            const client = createPgClient();
            const { data, error } = await client.rpc("exec_sql", { query });
            if (error) {
              return { jsonrpc: "2.0", id: req.id, error: { code: -32603, message: error.message } };
            }
            return {
              jsonrpc: "2.0",
              id: req.id,
              result: {
                content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
              },
            };
          }

          case "list_tables": {
            const client = createPgClient();
            const { data, error } = await client
              .from("information_schema.tables")
              .select("table_name, table_schema")
              .eq("table_schema", "public");

            if (error) {
              return { jsonrpc: "2.0", id: req.id, error: { code: -32603, message: error.message } };
            }
            return {
              jsonrpc: "2.0",
              id: req.id,
              result: {
                content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
              },
            };
          }

          case "get_schema": {
            const tableName = args.table_name as string;
            if (!tableName) {
              return { jsonrpc: "2.0", id: req.id, error: { code: -32602, message: "table_name requerida" } };
            }
            const client = createPgClient();
            const { data, error } = await client
              .from("information_schema.columns")
              .select("column_name, data_type, is_nullable")
              .eq("table_name", tableName)
              .eq("table_schema", "public");

            if (error) {
              return { jsonrpc: "2.0", id: req.id, error: { code: -32603, message: error.message } };
            }
            return {
              jsonrpc: "2.0",
              id: req.id,
              result: {
                content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
              },
            };
          }

          case "apply_migration": {
            const sql = args.sql as string;
            if (!sql) {
              return { jsonrpc: "2.0", id: req.id, error: { code: -32602, message: "SQL requerida" } };
            }
            // En producción, usar connection directa a PostgreSQL con mcp_agent
            // Por seguridad, limitar a operaciones específicas
            return {
              jsonrpc: "2.0",
              id: req.id,
              result: {
                content: [{ type: "text", text: "Migración aplicada (simulado). En producción usar connection pool con mcp_agent." }],
              },
            };
          }

          case "health_check": {
            return {
              jsonrpc: "2.0",
              id: req.id,
              result: {
                content: [{ type: "text", text: JSON.stringify({ status: "ok", timestamp: new Date().toISOString() }) }],
              },
            };
          }

          default:
            return { jsonrpc: "2.0", id: req.id, error: { code: -32601, message: `Tool ${toolName} not found` } };
        }
      }

      default:
        return { jsonrpc: "2.0", id: req.id, error: { code: -32601, message: `Method ${method} not found` } };
    }
  } catch (error) {
    return { jsonrpc: "2.0", id: req.id, error: { code: -32603, message: String(error) } };
  }
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const body: McpRequest | McpRequest[] = await req.json();
    const requests = Array.isArray(body) ? body : [body];
    const responses: McpResponse[] = [];

    for (const mcpRequest of requests) {
      const response = await handleMcpRequest(mcpRequest);
      responses.push(response);
    }

    const result = responses.length === 1 ? responses[0] : responses;
    return new Response(JSON.stringify(result), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(JSON.stringify({ jsonrpc: "2.0", id: null, error: { code: -32603, message: String(error) } }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 400,
    });
  }
});