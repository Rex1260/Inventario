import { useEffect, useState } from 'react'
import Link from 'next/link'
import { supabase } from '../lib/supabase'

export default function Plantillas() {
  const [plantillas, setPlantillas] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  
  // Campos del formulario
  const [nombrePlantilla, setNombrePlantilla] = useState('')
  const [equipoNombre, setEquipoNombre] = useState('')
  const [categoria, setCategoria] = useState('')
  const [marca, setMarca] = useState('')
  const [modelo, setModelo] = useState('')
  const [descripcion, setDescripcion] = useState('')

  useEffect(() => {
    checkUser()
  }, [])

  async function checkUser() {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) {
      window.location.href = '/login'
    } else {
      fetchPlantillas()
    }
  }

  async function fetchPlantillas() {
    try {
      const { data, error } = await supabase
        .from('plantillas')
        .select('*')
        .eq('is_active', true)
        .order('nombre_plantilla', { ascending: true })
      
      if (data) setPlantillas(data)
    } catch (error) {
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    try {
      if (editing) {
        await supabase
          .from('plantillas')
          .update({
            nombre_plantilla: nombrePlantilla,
            equipo_nombre: equipoNombre,
            categoria,
            marca,
            modelo,
            descripcion_default: descripcion
          })
          .eq('id', editing.id)
      } else {
        await supabase
          .from('plantillas')
          .insert({
            nombre_plantilla: nombrePlantilla,
            equipo_nombre: equipoNombre,
            categoria,
            marca,
            modelo,
            descripcion_default: descripcion
          })
      }
      
      setShowForm(false)
      resetForm()
      fetchPlantillas()
    } catch (error) {
      alert('Error: ' + error.message)
    }
  }

  function resetForm() {
    setEditing(null)
    setNombrePlantilla('')
    setEquipoNombre('')
    setCategoria('')
    setMarca('')
    setModelo('')
    setDescripcion('')
  }

  function editPlantilla(p) {
    setEditing(p)
    setNombrePlantilla(p.nombre_plantilla)
    setEquipoNombre(p.equipo_nombre || '')
    setCategoria(p.categoria || '')
    setMarca(p.marca || '')
    setModelo(p.modelo || '')
    setDescripcion(p.descripcion_default || '')
    setShowForm(true)
  }

  async function deletePlantilla(id) {
    if (!confirm('¿Eliminar esta plantilla?')) return
    try {
      await supabase
        .from('plantillas')
        .update({ is_active: false })
        .eq('id', id)
      fetchPlantillas()
    } catch (error) {
      alert('Error: ' + error.message)
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
            <h1 className="text-xl font-black text-blue-600 uppercase">Plantillas</h1>
            <p className="text-xs text-gray-500 font-bold">DIE CUT SOLUTIONS</p>
          </div>
        </div>
        <button
          onClick={() => { resetForm(); setShowForm(true) }}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-bold shadow-md hover:bg-blue-700"
        >
          + Nueva Plantilla
        </button>
      </nav>

      <main className="max-w-7xl mx-auto p-8">
        {loading ? (
          <div className="flex items-center space-x-2 text-blue-600 font-bold">
            <div className="animate-spin h-5 w-5 border-2 border-blue-600 border-t-transparent rounded-full"></div>
            <span>Cargando...</span>
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
            <table className="w-full text-left border-collapse">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Nombre</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Equipo</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Categoría</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Marca/Modelo</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-wider">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {plantillas.length === 0 ? (
                  <tr><td colSpan="5" className="px-6 py-12 text-center text-gray-400">No hay plantillas creadas</td></tr>
                ) : (
                  plantillas.map((p) => (
                    <tr key={p.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 font-black text-blue-600">{p.nombre_plantilla}</td>
                      <td className="px-6 py-4 font-bold">{p.equipo_nombre || '---'}</td>
                      <td className="px-6 py-4 text-sm">{p.categoria || '---'}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{p.marca || '---'} / {p.modelo || '---'}</td>
                      <td className="px-6 py-4 flex space-x-2">
                        <button onClick={() => editPlantilla(p)} className="p-2 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-blue-600 transition-colors">
                          ✏️
                        </button>
                        <button onClick={() => deletePlantilla(p.id)} className="p-2 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-red-600 transition-colors">
                          🗑️
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-8 max-w-2xl w-full max-h-screen overflow-y-auto">
            <h2 className="text-xl font-bold mb-6">{editing ? 'Editar' : 'Nueva'} Plantilla</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1">Nombre de la Plantilla *</label>
                <input
                  type="text"
                  required
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  value={nombrePlantilla}
                  onChange={(e) => setNombrePlantilla(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1">Nombre del Equipo</label>
                <input
                  type="text"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  value={equipoNombre}
                  onChange={(e) => setEquipoNombre(e.target.value.toUpperCase())}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Categoría</label>
                  <input
                    type="text"
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={categoria}
                    onChange={(e) => setCategoria(e.target.value.toUpperCase())}
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Marca</label>
                  <input
                    type="text"
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={marca}
                    onChange={(e) => setMarca(e.target.value.toUpperCase())}
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1">Modelo</label>
                <input
                  type="text"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  value={modelo}
                  onChange={(e) => setModelo(e.target.value.toUpperCase())}
                />
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1">Descripción Default</label>
                <textarea
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  rows="3"
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                />
              </div>
              <div className="flex space-x-4 pt-4">
                <button
                  type="button"
                  onClick={() => { setShowForm(false); resetForm() }}
                  className="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg font-bold hover:bg-gray-300"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg font-bold hover:bg-blue-700"
                >
                  {editing ? 'Actualizar' : 'Crear'} Plantilla
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
