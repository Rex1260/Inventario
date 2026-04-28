import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

export default function Recibir() {
  const [search, setSearch] = useState('')
  const [items, setItems] = useState([]) // Lista de préstamos a recibir
  const [loading, setLoading] = useState(false)
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState(null)
  const router = useRouter()

  useEffect(() => {
    checkUser()
  }, [])

  async function checkUser() {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) router.push('/login')
  }

  async function buscar() {
    if (!search) return
    setLoading(true)
    setError(null)
    setItems([])

    try {
      const searchTerm = search.toUpperCase().trim()

      if (searchTerm.startsWith('PR-')) {
        // BUSCAR POR FOLIO: Traer todos los equipos de ese folio que sigan activos
        const { data, error } = await supabase
          .from('prestamos')
          .select('*, equipos(*)')
          .eq('folio', searchTerm)
          .eq('estado', 'activo')

        if (error) throw error
        if (data && data.length > 0) {
          setItems(data)
        } else {
          setError('No se encontraron préstamos activos para este Folio.')
        }
      } else {
        // BUSCAR POR NO. INVENTARIO: Traer ese equipo y su préstamo activo
        const { data: eData } = await supabase
          .from('equipos')
          .select('*')
          .eq('no_inventario', searchTerm)
          .is('deleted_at', null)
          .maybeSingle()

        if (!eData) {
          setError('Equipo no encontrado.')
          return
        }

        if (eData.estado !== 'PRESTADO') {
          setError(`El equipo ${searchTerm} no está prestado (Estado: ${eData.estado})`)
          return
        }

        const { data: pData } = await supabase
          .from('prestamos')
          .select('*, equipos(*)')
          .eq('id_equipo', eData.id)
          .eq('estado', 'activo')
          .order('fecha_prestamo', { ascending: false })
          .limit(1)
          .maybeSingle()

        if (pData) {
          setItems([pData])
        } else {
          // Caso borde: está marcado como prestado pero no tiene folio activo
          setItems([{ equipos: eData, id_equipo: eData.id, id: null, folio: 'S/N', nombre_comodatario: 'DESCONOCIDO' }])
        }
      }
    } catch (err) {
      setError('Error al conectar con la base de datos.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  async function recibirItem(prestamo) {
    if (!confirm(`¿Confirmas la recepción del equipo ${prestamo.equipos?.no_inventario}?`)) return

    setProcessing(true)
    try {
      const now = new Date().toISOString()

      // 1. Liberar Equipo
      await supabase
        .from('equipos')
        .update({
          estado: 'FUNCIONAL',
          fecha_modificacion: now,
          modificado_por_nombre: 'Web Admin'
        })
        .eq('id', prestamo.id_equipo)

      // 2. Cerrar Préstamo
      if (prestamo.id) {
        await supabase
          .from('prestamos')
          .update({
            estado: 'devuelto',
            fecha_devolucion: now
          })
          .eq('id', prestamo.id)
      }

      // 3. Quitar de la lista local
      setItems(items.filter(i => i.id !== prestamo.id))

      if (items.length <= 1) {
        alert('Equipo recibido. Folio completado.')
        setSearch('')
      }

    } catch (err) {
      alert('Error al procesar')
    } finally {
      setProcessing(false)
    }
  }

  async function recibirTodo() {
    if (!confirm(`¿Confirmas la recepción de los ${items.length} equipos de este folio?`)) return

    setProcessing(true)
    try {
      const now = new Date().toISOString()

      for (const item of items) {
        // Liberar cada equipo
        await supabase
          .from('equipos')
          .update({ estado: 'FUNCIONAL', fecha_modificacion: now, modificado_por_nombre: 'Web Admin' })
          .eq('id', item.id_equipo)

        // Cerrar cada préstamo
        if (item.id) {
          await supabase
            .from('prestamos')
            .update({ estado: 'devuelto', fecha_devolucion: now })
            .eq('id', item.id)
        }
      }

      alert('Todos los equipos han sido recibidos correctamente.')
      setItems([])
      setSearch('')
    } catch (err) {
      alert('Error al procesar la devolución masiva')
    } finally {
      setProcessing(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-gray-900">
      <nav className="bg-white border-b border-gray-200 px-8 py-4 flex justify-between items-center sticky top-0 z-10">
        <div className="flex items-center space-x-4">
          <Link href="/" className="text-gray-400 hover:text-gray-900 transition-colors">
            <span className="text-2xl font-bold">←</span>
          </Link>
          <div>
            <h1 className="text-xl font-black text-blue-600 uppercase">Recepción de Almacén</h1>
            <p className="text-xs text-gray-500 font-bold">DIE CUT SOLUTIONS</p>
          </div>
        </div>
      </nav>

      <main className="max-w-4xl mx-auto p-8">
        <div className="bg-white rounded-3xl shadow-xl p-8 border border-gray-100 mb-8">
          <label className="block text-xs font-black text-gray-400 uppercase mb-4 tracking-wider text-center">
            Buscar por No. de Inventario o Folio (PR-...)
          </label>
          <div className="flex space-x-2">
            <input
              type="text"
              className="flex-1 px-6 py-4 rounded-2xl border-2 border-gray-100 focus:border-blue-500 focus:outline-none font-black text-xl text-center uppercase tracking-widest transition-all"
              placeholder="Ej: LAP-001 o PR-2025-..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && buscar()}
            />
            <button
              onClick={buscar}
              disabled={loading}
              className="bg-blue-600 text-white px-8 rounded-2xl font-black hover:bg-blue-700 transition-all shadow-lg shadow-blue-100 disabled:opacity-50"
            >
              {loading ? '...' : 'BUSCAR'}
            </button>
          </div>

          {error && (
            <div className="mt-6 bg-red-50 text-red-600 p-4 rounded-2xl text-sm font-bold text-center border border-red-100">
              {error}
            </div>
          )}
        </div>

        {items.length > 0 && (
          <div className="space-y-6">
            <div className="flex justify-between items-end px-2">
               <div>
                 <h2 className="text-sm font-black text-gray-400 uppercase tracking-widest">Responsable:</h2>
                 <p className="text-2xl font-black text-gray-900 uppercase">{items[0].nombre_comodatario}</p>
                 <p className="text-xs font-bold text-blue-600">Folio: {items[0].folio}</p>
               </div>
               {items.length > 1 && (
                 <button
                  onClick={recibirTodo}
                  disabled={processing}
                  className="bg-green-600 text-white px-6 py-3 rounded-xl font-black text-xs hover:bg-green-700 transition-all shadow-lg shadow-green-100"
                 >
                   RECIBIR TODO EL FOLIO ({items.length})
                 </button>
               )}
            </div>

            <div className="grid grid-cols-1 gap-4">
              {items.map((item) => (
                <div key={item.id || item.equipos?.id} className="bg-white p-6 rounded-3xl shadow-md border border-gray-100 flex justify-between items-center group hover:border-blue-200 transition-all">
                  <div className="flex items-center space-x-6">
                    <div className="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center font-black text-blue-600 text-lg border border-gray-100">
                      {item.equipos?.no_inventario?.split('-')[0]}
                    </div>
                    <div>
                      <p className="text-[10px] font-black text-gray-400 uppercase tracking-wider">Equipo / No. Inv</p>
                      <h3 className="text-lg font-black text-gray-800">{item.equipos?.nombre}</h3>
                      <p className="text-blue-600 font-black">{item.equipos?.no_inventario}</p>
                    </div>
                  </div>

                  <div className="text-right flex items-center space-x-4">
                    <div className="hidden md:block">
                       <p className="text-[10px] font-black text-gray-400 uppercase tracking-wider">Fecha Salida</p>
                       <p className="font-bold text-gray-600 text-sm">{new Date(item.fecha_prestamo).toLocaleDateString()}</p>
                    </div>
                    <button
                      onClick={() => recibirItem(item)}
                      disabled={processing}
                      className="bg-gray-100 text-gray-900 group-hover:bg-green-600 group-hover:text-white px-6 py-3 rounded-2xl font-black text-xs transition-all uppercase tracking-widest"
                    >
                      Recibir
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
