import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

export default function Inventario() {
  const [equipos, setEquipos] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const router = useRouter()

  useEffect(() => {
    checkUser()
  }, [])

  async function checkUser() {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) {
      router.push('/login')
    } else {
      fetchEquipos()
    }
  }

  async function fetchEquipos() {
    try {
      const { data, error } = await supabase
        .from('equipos')
        .select('*')
        .is('deleted_at', null)
        .order('no_inventario', { ascending: true })

      if (data) setEquipos(data)
    } catch (error) {
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  const filtered = equipos.filter(e =>
    e.no_inventario?.toLowerCase().includes(search.toLowerCase()) ||
    e.nombre?.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-gray-900">
      <nav className="bg-white border-b border-gray-200 px-8 py-4 flex justify-between items-center sticky top-0 z-10">
        <div className="flex items-center space-x-4">
          <Link href="/" className="text-gray-400 hover:text-gray-900 transition-colors">
            <span className="text-2xl font-bold">←</span>
          </Link>
          <div>
            <h1 className="text-xl font-black text-blue-600 uppercase">Inventario General</h1>
            <p className="text-xs text-gray-500 font-bold">DIE CUT SOLUTIONS</p>
          </div>
        </div>
        <div className="flex items-center space-x-4">
           <input
             type="text"
             placeholder="Buscar equipo..."
             className="border border-gray-300 rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none w-64 shadow-sm"
             value={search}
             onChange={(e) => setSearch(e.target.value)}
           />
           <button className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-bold shadow-md hover:bg-blue-700">
             + Nuevo Equipo
           </button>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto p-8">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
          <table className="w-full text-left border-collapse">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">No. Inv</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Equipo / Nombre</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Marca/Modelo</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Serie</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr><td colSpan="6" className="px-6 py-12 text-center text-blue-600 font-bold">Cargando equipos...</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan="6" className="px-6 py-12 text-center text-gray-400">No se encontraron equipos</td></tr>
              ) : (
                filtered.map((equipo) => (
                  <tr key={equipo.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 font-black text-blue-600">{equipo.no_inventario}</td>
                    <td className="px-6 py-4 font-bold">{equipo.nombre}</td>
                    <td className="px-6 py-4">
                      <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase ${
                        equipo.estado === 'FUNCIONAL' ? 'bg-green-100 text-green-700' :
                        equipo.estado === 'PRESTADO' ? 'bg-blue-100 text-blue-700' :
                        'bg-red-100 text-red-700'
                      }`}>
                        {equipo.estado}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600 font-medium">
                      {equipo.marca} / {equipo.modelo}
                    </td>
                    <td className="px-6 py-4 text-sm font-mono text-gray-500">
                      {equipo.numero_serie || '---'}
                    </td>
                    <td className="px-6 py-4 flex space-x-2">
                      <button className="p-2 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-blue-600 transition-colors">
                        👁️
                      </button>
                      <button className="p-2 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-red-600 transition-colors">
                        🗑️
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  )
}
