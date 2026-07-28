import { createContext, useContext, useState, useCallback } from 'react'
import { authApi, userApi } from '../api/resources'
import { saveSession, clearSession, getStoredUser } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser)

  const login = useCallback(async (email, password) => {
    const data = await authApi.login(email, password)
    saveSession(data)
    setUser(data.user)
    return data.user
  }, [])

  const register = useCallback(
    (email, password, fullName) => authApi.register(email, password, fullName),
    [],
  )

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    clearSession()
    setUser(null)
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken)
      } catch {
        
      }
    }
  }, [])

  const refreshProfile = useCallback(async () => {
    const profile = await userApi.me()
    const updated = { ...user, ...profile }
    localStorage.setItem('user', JSON.stringify(updated))
    setUser(updated)
    return updated
  }, [user])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, refreshProfile }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
