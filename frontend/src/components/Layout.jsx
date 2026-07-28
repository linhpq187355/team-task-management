import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import brandIcon from '../assets/icons/image.png'

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
          <img src={brandIcon} alt="Team Task Management Icon" className="brand-mark" style={{ width: '30px', height: '30px', borderRadius: '10px' }} />
          <span className="brand-name" style={{margin: 'auto'}}>Team task management</span>
        </NavLink>
        <nav className="nav-links">
          <NavLink to="/workspaces" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            Workspaces
          </NavLink>
          <NavLink to="/profile" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            {user?.fullName || 'profile'}
          </NavLink>
          <button className="link-btn nav-logout" onClick={handleLogout}>
            Log out
          </button>
        </nav>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
