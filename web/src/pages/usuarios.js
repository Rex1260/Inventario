import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

export default function Usuarios() {
  const [perfiles, setPerfiles] = useState([])
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
      fetchPerfiles()
    }
  }

  async function fetchPerfiles() {
    try {
      const { data, error } = await supabase
        .from('perfiles')
        .select('*')
        .order('nombre', { ascending: true })

      if (data) setPerfiles(data)
    } catch (error) {
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  async function updateRol(id, nuevoRol) {
    try {
      const { error } = await supabase
        .from('perfiles')
        .update({ rol: nuevoRol })
        .eq('id', id)

      if (!error) {
        setPerfiles(perfiles.map(p => p.id === id ? { ...p, rol: nuevoRol } : p))
      }
    } catch (error) {
      alert('Error al actualizar rol')
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
            <h1 className="text-xl font-black text-blue-600 uppercase">Gestión de Usuarios</h1>
            <p className="text-xs text-gray-500 font-bold">CONTROL DE ACCESO DCS</p>
          </div>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto p-8">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
          <table className="w-full text-left border-collapse">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Nombre del Usuario</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">ID Único</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Rol de Acceso</th>
                <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr><td colSpan="4" className="px-6 py-12 text-center text-blue-600 font-bold">Cargando perfiles...</td></tr>
              ) : (
                perfiles.map((perfil) => (
                  <tr key={perfil.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xs">
                          {perfil.nombre?.charAt(0).toUpperCase()}
                        </div>
                        <span className="font-bold">{perfil.nombre}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-xs font-mono text-gray-400">
                      {perfil.id}
                    </td>
                    <td className="px-6 py-4">
                      <select
                        value={perfil.rol}
                        onChange={(e) => updateRol(perfil.id, e.target.value)}
                        className={`text-[10px] font-black uppercase px-3 py-1 rounded-full border-none focus:ring-2 focus:ring-blue-500 cursor-pointer ${
                          perfil.rol === 'ADMIN' ? 'bg-blue-100 text-blue-700' :
                          perfil.rol === 'VIEWER' ? 'bg-green-100 text-green-700' :
                          'bg-gray-100 text-gray-700'
                        }`}
                      >
                        <option value="ADMIN">ADMINISTRADOR</option>
                        <option value="USER">OPERADOR</option>
                        <option value="VIEWER">MODO LECTURA</option>
                      </select>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button className="text-red-400 hover:text-red-600 font-bold text-xs p-2">
                        ELIMINAR
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <p className="mt-6 text-center text-sm text-gray-500 font-medium">
          Los usuarios nuevos se registran desde la App móvil y aparecen aquí automáticamente.
        </p>
      </main>
    </div>
  )
}
