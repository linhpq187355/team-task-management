import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { workspaceApi } from '../api/resources'
import Modal from '../components/Modal'
import { RoleBadge } from '../components/Badges'

export default function WorkspaceListPage() {
  const [workspaces, setWorkspaces] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showCreate, setShowCreate] = useState(false)

  function load() {
    setLoading(true)
    workspaceApi
      .list()
      .then(setWorkspaces)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <div className="page">
      <div className="page-header">
        <h2>Workspaces</h2>
        <button onClick={() => setShowCreate(true)}>+ New workspace</button>
      </div>
      {error && <div className="alert-error">{error}</div>}
      {loading ? (
        <p className="muted">Loading…</p>
      ) : (
        <div className="card-grid">
          {workspaces.map((ws) => (
            <Link key={ws.id} to={`/workspaces/${ws.id}`} className="card card-link">
              <div className="card-title-row">
                <h3>{ws.name}</h3>
                <RoleBadge role={ws.myRole} />
              </div>
              <p className="muted">{ws.description || 'No description'}</p>
            </Link>
          ))}
          {workspaces.length === 0 && <p className="muted">No workspaces yet.</p>}
        </div>
      )}

      {showCreate && (
        <CreateWorkspaceModal
          onClose={() => setShowCreate(false)}
          onCreated={() => {
            setShowCreate(false)
            load()
          }}
        />
      )}
    </div>
  )
}

function CreateWorkspaceModal({ onClose, onCreated }) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await workspaceApi.create({ name, description })
      onCreated()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="New workspace" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert-error">{error}</div>}
        <label>
          Name
          <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={150} />
        </label>
        <label>
          Description
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
        <button type="submit" disabled={saving}>
          {saving ? 'Creating…' : 'Create'}
        </button>
      </form>
    </Modal>
  )
}
