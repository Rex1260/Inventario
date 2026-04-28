import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://gtzqekmewuwxcpyrojpn.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0enFla21ld3V3eGNweXJvanBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4OTMyMjgsImV4cCI6MjA5MDQ2OTIyOH0.h05nkqSl61K7ChVXe3rOIPNvDMDSBaAkCr7g1Myx4fA'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
