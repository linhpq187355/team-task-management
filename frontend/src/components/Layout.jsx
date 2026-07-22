import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <header className="navbar">
        <NavLink to="/workspaces" className="brand">
          <span className="brand-mark">ttm/</span>
          <span className="brand-name">team task management</span>
        </NavLink>
        <nav className="nav-links">
          <NavLink to="/workspaces" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            workspaces
          </NavLink>
          <NavLink to="/profile" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            {user?.fullName || 'profile'}
          </NavLink>
          <button className="link-btn nav-logout" onClick={handleLogout}>
            log out
          </button>
        </nav>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
