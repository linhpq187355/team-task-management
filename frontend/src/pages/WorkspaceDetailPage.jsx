import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { projectApi, workspaceApi } from '../api/resources'
import Modal from '../components/Modal'
import { RoleBadge } from '../components/Badges'

export default function WorkspaceDetailPage() {
  const { workspaceId } = useParams()
  const navigate = useNavigate()
  const [workspace, setWorkspace] = useState(null)
  const [projects, setProjects] = useState([])
  const [members, setMembers] = useState([])
  const [error, setError] = useState('')
  const [showCreateProject, setShowCreateProject] = useState(false)
  const [showAddMember, setShowAddMember] = useState(false)

  function load() {
    setError('')
    Promise.all([
      workspaceApi.get(workspaceId),
      projectApi.listByWorkspace(workspaceId),
      workspaceApi.members(workspaceId),
    ])
      .then(([ws, projs, mems]) => {
        setWorkspace(ws)
        setProjects(projs)
        setMembers(mems)
      })
      .catch((err) => setError(err.message))
  }

  useEffect(load, [workspaceId])

  const isOwner = workspace?.myRole === 'OWNER'

  async function handleDeleteWorkspace() {
    if (!confirm('Delete this workspace? This can be undone by an admin only.')) return
    try {
      await workspaceApi.remove(workspaceId)
      navigate('/workspaces')
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleRemoveMember(userId) {
    if (!confirm('Remove this member from the workspace?')) return
    try {
      await workspaceApi.removeMember(workspaceId, userId)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleRoleChange(userId, role) {
    try {
      await workspaceApi.updateMemberRole(workspaceId, userId, role)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!workspace) return <div className="page">{error ? <div className="alert-error">{error}</div> : 'Loading…'}</div>

  return (
    <div className="page">
      {error && <div className="alert-error">{error}</div>}
      <div className="page-header">
        <div>
          <h2>{workspace.name}</h2>
          <p className="muted">{workspace.description}</p>
        </div>
        {isOwner && (
          <button className="danger-btn" onClick={handleDeleteWorkspace}>
            Delete workspace
          </button>
        )}
      </div>

      <section>
        <div className="page-header">
          <h3>Projects</h3>
          {isOwner && <button onClick={() => setShowCreateProject(true)}>+ New project</button>}
        </div>
        <div className="card-grid">
          {projects.map((p) => (
            <Link key={p.id} to={`/projects/${p.id}`} className="card card-link">
              <div className="card-title-row">
                <h3>{p.name}</h3>
                {p.myRole && <RoleBadge role={p.myRole} />}
              </div>
              <p className="muted">{p.description || 'No description'}</p>
              <p className="muted small">
                {p.startDate || '—'} → {p.endDate || '—'}
              </p>
            </Link>
          ))}
          {projects.length === 0 && <p className="muted">No projects yet.</p>}
        </div>
      </section>

      <section>
        <div className="page-header">
          <h3>Members</h3>
          {isOwner && <button onClick={() => setShowAddMember(true)}>+ Add member</button>}
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              {isOwner && <th></th>}
            </tr>
          </thead>
          <tbody>
            {members.map((m) => (
              <tr key={m.id}>
                <td>{m.user.fullName}</td>
                <td>{m.user.email}</td>
                <td>
                  {isOwner ? (
                    <select value={m.role} onChange={(e) => handleRoleChange(m.user.id, e.target.value)}>
                      <option value="OWNER">OWNER</option>
                      <option value="MEMBER">MEMBER</option>
                    </select>
                  ) : (
                    <RoleBadge role={m.role} />
                  )}
                </td>
                {isOwner && (
                  <td>
                    <button className="link-btn danger-text" onClick={() => handleRemoveMember(m.user.id)}>
                      Remove
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {showCreateProject && (
        <CreateProjectModal
          workspaceId={workspaceId}
          onClose={() => setShowCreateProject(false)}
          onCreated={() => {
            setShowCreateProject(false)
            load()
          }}
        />
      )}
      {showAddMember && (
        <AddMemberModal
          workspaceId={workspaceId}
          onClose={() => setShowAddMember(false)}
          onAdded={() => {
            setShowAddMember(false)
            load()
          }}
        />
      )}
    </div>
  )
}

function CreateProjectModal({ workspaceId, onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', description: '', startDate: '', endDate: '' })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await projectApi.create(workspaceId, {
        ...form,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
      })
      onCreated()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="New project" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert-error">{error}</div>}
        <label>
          Name
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
            maxLength={150}
          />
        </label>
        <label>
          Description
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </label>
        <label>
          Start date
          <input
            type="date"
            value={form.startDate}
            onChange={(e) => setForm({ ...form, startDate: e.target.value })}
          />
        </label>
        <label>
          End date
          <input
            type="date"
            value={form.endDate}
            onChange={(e) => setForm({ ...form, endDate: e.target.value })}
          />
        </label>
        <button type="submit" disabled={saving}>
          {saving ? 'Creating…' : 'Create'}
        </button>
      </form>
    </Modal>
  )
}

function AddMemberModal({ workspaceId, onClose, onAdded }) {
  const [email, setEmail] = useState('')
  const [role, setRole] = useState('MEMBER')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await workspaceApi.addMember(workspaceId, email, role)
      onAdded()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="Add member" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert-error">{error}</div>}
        <label>
          Email (must be an existing user)
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Role
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="MEMBER">MEMBER</option>
            <option value="OWNER">OWNER</option>
          </select>
        </label>
        <button type="submit" disabled={saving}>
          {saving ? 'Adding…' : 'Add'}
        </button>
      </form>
    </Modal>
  )
}
