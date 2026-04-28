import { useState } from 'react'
import { useRouter } from 'next/router'
import { supabase } from '../lib/supabase'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const router = useRouter()

  async function handleLogin(e) {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const { error } = await supabase.auth.signInWithPassword({
        email: email.trim().toLowerCase(),
        password: password.trim(),
      })

      if (error) throw error
      router.push('/')
    } catch (err) {
      setError(err.message === 'Invalid login credentials' ? 'Correo o contraseña incorrectos' : err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6 font-sans">
      <div className="max-w-md w-full bg-white rounded-3xl shadow-xl p-10 border border-gray-100">
        <div className="text-center mb-10">
          <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-blue-200">
            <span className="text-white text-3xl font-black">C</span>
          </div>
          <h1 className="text-2xl font-black text-gray-900 uppercase tracking-tight">Die Cut Solutions</h1>
          <p className="text-gray-500 font-bold text-xs uppercase tracking-widest mt-1">Inventory Hub</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label className="block text-xs font-black text-gray-400 uppercase mb-2 tracking-wider">Correo Electrónico</label>
            <input
              type="email"
              required
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 focus:outline-none font-medium transition-all"
              placeholder="admin@convertsolutions.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-xs font-black text-gray-400 uppercase mb-2 tracking-wider">Contraseña</label>
            <input
              type="password"
              required
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 focus:outline-none font-medium transition-all"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-4 rounded-xl text-xs font-bold border border-red-100">
              {error}
            </div>
          )}

          <button
            disabled={loading}
            className="w-full bg-gray-900 text-white py-4 rounded-2xl font-black hover:bg-gray-800 transition-all shadow-lg disabled:opacity-50 uppercase tracking-widest text-sm"
          >
            {loading ? 'Validando...' : 'Entrar al Panel'}
          </button>
        </form>
      </div>
    </div>
  )
}
