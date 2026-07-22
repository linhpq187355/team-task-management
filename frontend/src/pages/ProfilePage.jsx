import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../api/resources'

export default function ProfilePage() {
  const { user, refreshProfile } = useAuth()
  const [fullName, setFullName] = useState(user?.fullName || '')
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '')
  const [profileMsg, setProfileMsg] = useState('')
  const [profileError, setProfileError] = useState('')

  const [pwForm, setPwForm] = useState({ oldPassword: '', newPassword: '', rePassword: '' })
  const [pwMsg, setPwMsg] = useState('')
  const [pwError, setPwError] = useState('')

  async function handleProfileSubmit(e) {
    e.preventDefault()
    setProfileError('')
    setProfileMsg('')
    try {
      await userApi.updateProfile(fullName, avatarUrl)
      await refreshProfile()
      setProfileMsg('Profile updated')
    } catch (err) {
      setProfileError(err.message)
    }
  }

  async function handlePasswordSubmit(e) {
    e.preventDefault()
    setPwError('')
    setPwMsg('')
    try {
      await userApi.changePassword(pwForm.oldPassword, pwForm.newPassword, pwForm.rePassword)
      setPwMsg('Password updated')
      setPwForm({ oldPassword: '', newPassword: '', rePassword: '' })
    } catch (err) {
      setPwError(err.errors?.join(', ') || err.message)
    }
  }

  return (
    <div className="page-narrow">
      <h2>Profile</h2>
      <p className="muted">{user?.email}</p>

      <section className="card">
        <h3>Basic info</h3>
        <form onSubmit={handleProfileSubmit}>
          {profileError && <div className="alert-error">{profileError}</div>}
          {profileMsg && <div className="alert-success">{profileMsg}</div>}
          <label>
            Full name
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </label>
          <label>
            Avatar URL
            <input value={avatarUrl} onChange={(e) => setAvatarUrl(e.target.value)} />
          </label>
          <button type="submit">Save</button>
        </form>
      </section>

      <section className="card">
        <h3>Change password</h3>
        <form onSubmit={handlePasswordSubmit}>
          {pwError && <div className="alert-error">{pwError}</div>}
          {pwMsg && <div className="alert-success">{pwMsg}</div>}
          <label>
            Current password
            <input
              type="password"
              value={pwForm.oldPassword}
              onChange={(e) => setPwForm({ ...pwForm, oldPassword: e.target.value })}
              required
            />
          </label>
          <label>
            New password
            <input
              type="password"
              value={pwForm.newPassword}
              onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })}
              required
            />
          </label>
          <label>
            Confirm new password
            <input
              type="password"
              value={pwForm.rePassword}
              onChange={(e) => setPwForm({ ...pwForm, rePassword: e.target.value })}
              required
            />
          </label>
          <button type="submit">Update password</button>
        </form>
      </section>
    </div>
  )
}
