package com.example.inventario.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://gtzqekmewuwxcpyrojpn.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0enFla21ld3V3eGNweXJvanBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4OTMyMjgsImV4cCI6MjA5MDQ2OTIyOH0.h05nkqSl61K7ChVXe3rOIPNvDMDSBaAkCr7g1Myx4fA" // TODO: Reemplazar con la clave real
) {
    install(Postgrest)
    install(Storage)
}
