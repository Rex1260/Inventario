import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

export default function Pendientes() {
  const [prestamos, setPrestamos] = useState([])
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    checkUser()
  }, [])

  async function checkUser() {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) {
      router.push('/login')
    } else {
      fetchPendientes()
    }
  }

  async function fetchPendientes() {
    try {
      // 1. Obtener préstamos activos con info del equipo
      const { data, error } = await supabase
        .from('prestamos')
        .select('*, equipos(*)')
        .eq('estado', 'activo')
        .order('fecha_prestamo', { ascending: false })

      if (data) setPrestamos(data)
    } catch (error) {
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  // Agrupar préstamos por persona
  const agrupados = prestamos.reduce((acc, curr) => {
    const nombre = curr.nombre_comodatario || 'SIN NOMBRE'
    if (!acc[nombre]) acc[nombre] = []
    acc[nombre].push(curr)
    return acc
  }, {})

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-gray-900">
      <nav className="bg-white border-b border-gray-200 px-8 py-4 flex justify-between items-center sticky top-0 z-10">
        <div className="flex items-center space-x-4">
          <Link href="/" className="text-gray-400 hover:text-gray-900 transition-colors">
            <span className="text-2xl font-bold">←</span>
          </Link>
          <div>
            <h1 className="text-xl font-black text-blue-600 uppercase">Préstamos Pendientes</h1>
            <p className="text-xs text-gray-500 font-bold">CONTROL DE SALIDAS DCS</p>
          </div>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto p-8">
        {loading ? (
          <div className="text-center py-12 text-blue-600 font-bold">Cargando préstamos...</div>
        ) : Object.keys(agrupados).length === 0 ? (
          <div className="bg-white rounded-2xl p-12 text-center shadow-sm border border-gray-100">
            <p className="text-gray-400 font-medium text-lg">No hay préstamos activos en este momento.</p>
          </div>
        ) : (
          <div className="space-y-8">
            {Object.entries(agrupados).map(([nombre, items]) => (
              <div key={nombre} className="bg-white rounded-2xl shadow-md overflow-hidden border border-gray-100">
                <div className="bg-gray-50 px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                  <h2 className="font-black text-gray-800 text-lg uppercase tracking-tight">{nombre}</h2>
                  <span className="bg-blue-600 text-white text-[10px] font-black px-2 py-1 rounded-full">
                    {items.length} ARTÍCULO(S)
                  </span>
                </div>
                <div className="p-0">
                  <table className="w-full text-left">
                    <thead className="text-xs font-black text-gray-400 uppercase bg-gray-50/50">
                      <tr>
                        <th className="px-6 py-3">No. Inv / Equipo</th>
                        <th className="px-6 py-3 text-center">Desde</th>
                        <th className="px-6 py-3 text-center">Vencimiento</th>
                        <th className="px-6 py-3 text-right">Acciones</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                      {items.map((item) => (
                        <tr key={item.id} className="hover:bg-blue-50/30 transition-colors">
                          <td className="px-6 py-4">
                            <div className="flex flex-col">
                              <span className="font-black text-blue-600 text-sm">{item.equipos?.no_inventario}</span>
                              <span className="font-bold text-gray-700">{item.equipos?.nombre}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4 text-center text-sm font-medium text-gray-500">
                            {new Date(item.fecha_prestamo).toLocaleDateString()}
                          </td>
                          <td className="px-6 py-4 text-center">
                            {item.fecha_vencimiento ? (
                               <span className={`text-xs font-black px-2 py-1 rounded ${
                                 new Date(item.fecha_vencimiento) < new Date() ? 'bg-red-100 text-red-600' : 'bg-yellow-100 text-yellow-700'
                               }`}>
                                 {new Date(item.fecha_vencimiento).toLocaleDateString()}
                               </span>
                            ) : (
                               <span className="text-xs text-gray-400 font-bold uppercase tracking-widest italic">Indefinido</span>
                            )}
                          </td>
                          <td className="px-6 py-4 text-right">
                            <button className="bg-white border border-gray-200 text-gray-600 px-3 py-1 rounded-lg text-xs font-black hover:border-blue-500 hover:text-blue-600 transition-all shadow-sm">
                              VER FOLIO: {item.folio}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
