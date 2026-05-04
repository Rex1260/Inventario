import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

export default function Inventario() {
  const [equipos, setEquipos] = useState([])
  const [plantillas, setPlantillas] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [showTemplateSelector, setShowTemplateSelector] = useState(false)
  const router = useRouter()

  // Campos del formulario
  const [noInventario, setNoInventario] = useState('')
  const [nombre, setNombre] = useState('')
  const [descripcion, setDescripcion] = useState('')
  const [categoria, setCategoria] = useState('')
  const [marca, setMarca] = useState('')
  const [modelo, setModelo] = useState('')
  const [estado, setEstado] = useState('FUNCIONAL')
  const [numeroSerie, setNumeroSerie] = useState('')
  const [numeroTag, setNumeroTag] = useState('')

  useEffect(() => {
    checkUser()
  }, [])

  async function checkUser() {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) {
      router.push('/login')
    } else {
      fetchEquipos()
      fetchPlantillas()
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
    }
  }

  function applyTemplate(plantilla) {
    if (plantilla.equipo_nombre) setNombre(plantilla.equipo_nombre)
    if (plantilla.categoria) setCategoria(plantilla.categoria)
    if (plantilla.marca) setMarca(plantilla.marca)
    if (plantilla.modelo) setModelo(plantilla.modelo)
    if (plantilla.descripcion_default) setDescripcion(plantilla.descripcion_default)
    setShowTemplateSelector(false)
    alert('Plantilla "' + plantilla.nombre_plantilla + '" aplicada')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    try {
      if (editing) {
        await supabase
          .from('equipos')
          .update({
            no_inventario: noInventario.toUpperCase(),
            nombre: nombre.toUpperCase(),
            descripcion,
            categoria: categoria.toUpperCase(),
            marca: marca.toUpperCase(),
            modelo: modelo.toUpperCase(),
            estado,
            numero_serie: numeroSerie.toUpperCase(),
            numero_tag: numeroTag.toUpperCase()
          })
          .eq('id', editing.id)
      } else {
        await supabase
          .from('equipos')
          .insert({
            no_inventario: noInventario.toUpperCase(),
            nombre: nombre.toUpperCase(),
            descripcion,
            categoria: categoria.toUpperCase(),
            marca: marca.toUpperCase(),
            modelo: modelo.toUpperCase(),
            estado,
            numero_serie: numeroSerie.toUpperCase(),
            numero_tag: numeroTag.toUpperCase()
          })
      }
      
      resetForm()
      fetchEquipos()
    } catch (error) {
      alert('Error: ' + error.message)
    }
  }

  function resetForm() {
    setEditing(null)
    setNoInventario('')
    setNombre('')
    setDescripcion('')
    setCategoria('')
    setMarca('')
    setModelo('')
    setEstado('FUNCIONAL')
    setNumeroSerie('')
    setNumeroTag('')
  }

  function editEquipo(equipo) {
    setEditing(equipo)
    setNoInventario(equipo.no_inventario)
    setNombre(equipo.nombre)
    setDescripcion(equipo.descripcion || '')
    setCategoria(equipo.categoria || '')
    setMarca(equipo.marca || '')
    setModelo(equipo.modelo || '')
    setEstado(equipo.estado || 'FUNCIONAL')
    setNumeroSerie(equipo.numero_serie || '')
    setNumeroTag(equipo.numero_tag || '')
    setShowForm(true)
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
          <button 
            onClick={() => { resetForm(); setShowForm(true) }}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-bold shadow-md hover:bg-blue-700"
          >
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
                      <button onClick={() => editEquipo(equipo)} className="p-2 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-blue-600 transition-colors">
                        ✏️
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

      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-8 max-w-2xl w-full max-h-screen overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-bold">{editing ? 'Editar' : 'Nuevo'} Equipo</h2>
              <button
                onClick={() => { setShowForm(false); resetForm() }}
                className="text-gray-400 hover:text-gray-900"
              >
                ✕
              </button>
            </div>

            {/* Selector de Plantillas */}
            {!editing && plantillas.length > 0 && (
              <div className="mb-6 p-4 bg-blue-50 rounded-xl">
                <div className="flex justify-between items-center mb-3">
                  <h3 className="text-sm font-bold text-blue-800">Plantillas Disponibles</h3>
                  <button
                    onClick={() => setShowTemplateSelector(true)}
                    className="text-sm font-bold text-blue-600 hover:text-blue-800"
                  >
                    Ver Todas →
                  </button>
                </div>
                <div className="flex flex-wrap gap-2">
                  {plantillas.slice(0, 3).map((p) => (
                    <button
                      key={p.id}
                      onClick={() => applyTemplate(p)}
                      className="px-3 py-1 bg-white border border-blue-200 rounded-lg text-xs font-bold text-blue-700 hover:bg-blue-100 transition-all"
                    >
                      {p.nombre_plantilla}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1">No. Inventario *</label>
                <input
                  type="text"
                  required
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none font-mono"
                  value={noInventario}
                  onChange={(e) => setNoInventario(e.target.value.toUpperCase())}
                  placeholder="ABC00001"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Nombre *</label>
                  <input
                    type="text"
                    required
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value.toUpperCase())}
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Categoría</label>
                  <input
                    type="text"
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={categoria}
                    onChange={(e) => setCategoria(e.target.value.toUpperCase())}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Marca</label>
                  <input
                    type="text"
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={marca}
                    onChange={(e) => setMarca(e.target.value.toUpperCase())}
                  />
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
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1">Descripción</label>
                <textarea
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  rows="3"
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                />
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Estado</label>
                  <select
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={estado}
                    onChange={(e) => setEstado(e.target.value)}
                  >
                    <option value="FUNCIONAL">FUNCIONAL</option>
                    <option value="OBSOLETO">OBSOLETO</option>
                    <option value="DESCOMPUESTO">DESCOMPUESTO</option>
                    <option value="PRESTADO">PRESTADO</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">No. Serie</label>
                  <input
                    type="text"
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={numeroSerie}
                    onChange={(e) => setNumeroSerie(e.target.value.toUpperCase())}
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">No. Tag</label>
                  <input
                    type="text"
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={numeroTag}
                    onChange={(e) => setNumeroTag(e.target.value.toUpperCase())}
                  />
                </div>
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
                  {editing ? 'Actualizar' : 'Guardar'} Equipo
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Plantillas */}
      {showTemplateSelector && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-8 max-w-2xl w-full max-h-screen overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-bold">Seleccionar Plantilla</h2>
              <button onClick={() => setShowTemplateSelector(false)} className="text-gray-400 hover:text-gray-900">✕</button>
            </div>
            <div className="space-y-3">
              {plantillas.map((p) => (
                <div
                  key={p.id}
                  onClick={() => applyTemplate(p)}
                  className="p-4 border border-gray-200 rounded-xl hover:bg-blue-50 cursor-pointer transition-all"
                >
                  <h3 className="font-bold text-blue-700">{p.nombre_plantilla}</h3>
                  {p.equipo_nombre && <p className="text-sm text-gray-600">Equipo: {p.equipo_nombre}</p>}
                  <div className="flex space-x-4 text-xs text-gray-500 mt-2">
                    {p.categoria && <span>Categoría: {p.categoria}</span>}
                    {p.marca && <span>Marca: {p.marca}</span>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
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
