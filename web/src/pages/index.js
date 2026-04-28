import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

// Componente de Tarjeta de Estadística
const StatCard = ({ title, value, colorClass }) => (
  <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
    <p className="text-sm font-bold text-gray-500 uppercase tracking-wider">{title}</p>
    <h3 className={`text-3xl font-black mt-2 ${colorClass}`}>{value}</h3>
  </div>
)

export default function Home() {
  const [stats, setStats] = useState({ total: 0, prestados: 0, mantenimiento: 0 })
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
      fetchStats()
    }
  }

  async function fetchStats() {
    try {
      const { data: equipos, error } = await supabase
        .from('equipos')
        .select('*')
        .is('deleted_at', null)

      if (equipos) {
        setStats({
          total: equipos.length,
          prestados: equipos.filter(e => e.estado === 'PRESTADO').length,
          mantenimiento: equipos.filter(e => e.estado === 'EN MANTENIMIENTO').length
        })
      }
    } catch (error) {
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-gray-900">
      <nav className="bg-white border-b border-gray-200 px-8 py-4 flex justify-between items-center">
        <div>
          <h1 className="text-xl font-black text-blue-600">DIE CUT SOLUTIONS</h1>
          <p className="text-xs text-gray-500 font-bold">INVENTORY HUB v1.0</p>
        </div>
        <button
          onClick={async () => {
            await supabase.auth.signOut()
            router.push('/login')
          }}
          className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-gray-800 transition-colors"
        >
          Cerrar Sesión
        </button>
      </nav>

      <main className="max-w-7xl mx-auto p-8">
        <header className="mb-8">
          <h2 className="text-2xl font-bold">Panel de Control</h2>
          <p className="text-gray-600">Resumen general de activos en tiempo real.</p>
        </header>

        {loading ? (
          <div className="flex items-center space-x-2 text-blue-600 font-bold">
            <div className="animate-spin h-5 w-5 border-2 border-blue-600 border-t-transparent rounded-full"></div>
            <span>Cargando datos...</span>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <StatCard title="Total Equipos" value={stats.total} colorClass="text-gray-900" />
            <StatCard title="En Préstamo" value={stats.prestados} colorClass="text-blue-600" />
            <StatCard title="Dañados" value={stats.mantenimiento} colorClass="text-red-600" />
          </div>
        )}

        <section className="mt-12">
          <h3 className="text-lg font-bold mb-4">Acciones de Administración</h3>
          <div className="flex flex-wrap gap-4">
            <Link href="/inventario">
              <button className="bg-white border border-gray-300 px-6 py-3 rounded-xl font-bold shadow-sm hover:bg-gray-50 transition-all flex items-center space-x-2">
                <span>📦</span>
                <span>Ver Inventario</span>
              </button>
            </Link>

            <Link href="/usuarios">
              <button className="bg-white border border-gray-300 px-6 py-3 rounded-xl font-bold shadow-sm hover:bg-gray-50 transition-all flex items-center space-x-2">
                <span>👥</span>
                <span>Usuarios</span>
              </button>
            </Link>

            <Link href="/pendientes">
              <button className="bg-white border border-gray-300 px-6 py-3 rounded-xl font-bold shadow-sm hover:bg-gray-50 transition-all flex items-center space-x-2">
                <span>⏳</span>
                <span>Pendientes</span>
              </button>
            </Link>

            <Link href="/recibir">
              <button className="bg-white border border-gray-300 px-6 py-3 rounded-xl font-bold shadow-sm hover:bg-gray-50 transition-all flex items-center space-x-2">
                <span>📥</span>
                <span>Recibir Equipo</span>
              </button>
            </Link>

            <button
              onClick={() => alert('Generando reporte... próximamente')}
              className="bg-white border border-gray-300 px-6 py-3 rounded-xl font-bold shadow-sm hover:bg-gray-50 transition-all text-green-600 flex items-center space-x-2"
            >
              <span>📊</span>
              <span>Exportar Excel</span>
            </button>
          </div>
        </section>
      </main>
    </div>
  )
}
