import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { projectApi, workspaceApi } from '../api/resources'
import Modal from '../components/Modal'
import { RoleBadge } from '../components/Badges'

export default function ProjectDetailPage() {
  const { projectId } = useParams()
  const navigate = useNavigate()
  const [project, setProject] = useState(null)
  const [members, setMembers] = useState([])
  const [workspaceMembers, setWorkspaceMembers] = useState([])
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(false)
  const [showAddMember, setShowAddMember] = useState(false)
  const [confirmAction, setConfirmAction] = useState(null) // { title, message, onConfirm }

  function load() {
    setError('')
    projectApi
      .get(projectId)
      .then((p) => {
        setProject(p)
        return Promise.all([projectApi.members(projectId), workspaceApi.members(p.workspaceId)])
      })
      .then(([mems, wsMembers]) => {
        setMembers(mems)
        setWorkspaceMembers(wsMembers)
      })
      .catch((err) => setError(err.message))
  }

  useEffect(load, [projectId])

  const canManage =
    project?.myRole === 'PROJECT_MANAGER' || project?.myRoles?.includes('PROJECT_MANAGER')

  async function handleDelete() {
    setConfirmAction({
      title: 'Delete project',
      message: 'Are you sure you want to delete this project?',
      onConfirm: async () => {
        try {
          await projectApi.remove(projectId)
          navigate(`/workspaces/${project.workspaceId}`)
        } catch (err) {
          setError(err.message)
        }
      },
    })
  }

  async function handleRemoveMember(userId) {
    setConfirmAction({
      title: 'Remove member',
      message: 'Are you sure you want to remove this member from the project?',
      onConfirm: async () => {
        try {
          await projectApi.removeMember(projectId, userId)
          load()
        } catch (err) {
          setError(err.message)
        }
      },
    })
  }

  async function handleRoleChange(userId, role) {
    try {
      await projectApi.updateMemberRole(projectId, userId, role)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!project) return <div className="page">{error ? <div className="alert-error">{error}</div> : 'Loading…'}</div>

  const availableToAdd = workspaceMembers.filter(
    (wm) => !members.some((pm) => pm.userId === wm.user.id),
  )

  return (
    <div className="page">
      {error && <div className="alert-error">{error}</div>}
      <div className="page-header">
        <div>
          <Link to={`/workspaces/${project.workspaceId}`} className="crumb-back">
            ← Back to workspace
          </Link>
          <h2>{project.name}</h2>
          <p className="muted">{project.description}</p>
          <p className="muted small">
            {project.startDate || '—'} → {project.endDate || '—'}
          </p>
        </div>
        <div className="btn-row">
          <Link to={`/projects/${projectId}/board`}>
            <button>Open task board</button>
          </Link>
          {canManage && (
            <button className="secondary-btn" onClick={() => setEditing(true)}>
              Edit
            </button>
          )}
          {canManage && (
            <button className="danger-btn" onClick={handleDelete}>
              Delete
            </button>
          )}
        </div>
      </div>

      <section>
        <div className="page-header">
          <h3>Members</h3>
          {canManage && <button onClick={() => setShowAddMember(true)}>+ Add member</button>}
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              {canManage && <th></th>}
            </tr>
          </thead>
          <tbody>
            {members.map((m) => (
              <tr key={m.id}>
                <td>{m.fullName}</td>
                <td>{m.email}</td>
                <td>
                  {canManage ? (
                    <select value={m.role} onChange={(e) => handleRoleChange(m.userId, e.target.value)}>
                      <option value="PROJECT_MANAGER">PROJECT_MANAGER</option>
                      <option value="DEVELOPER">DEVELOPER</option>
                    </select>
                  ) : (
                    <RoleBadge role={m.role} />
                  )}
                </td>
                {canManage && (
                  <td>
                    <button className="link-btn danger-text" onClick={() => handleRemoveMember(m.userId)}>
                      Remove
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {editing && (
        <EditProjectModal
          project={project}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false)
            load()
          }}
        />
      )}
      {showAddMember && (
        <AddProjectMemberModal
          projectId={projectId}
          candidates={availableToAdd}
          onClose={() => setShowAddMember(false)}
          onAdded={() => {
            setShowAddMember(false)
            load()
          }}
        />
      )}
      {confirmAction && (
        <Modal title={confirmAction.title} onClose={() => setConfirmAction(null)}>
          <div>
            <p>{confirmAction.message}</p>
            <div className="btn-row" style={{ justifyContent: 'flex-end' }}>
              <button
                className="secondary-btn"
                onClick={() => setConfirmAction(null)}
              >
                Cancel
              </button>
              <button
                className="danger-btn"
                onClick={() => {
                  confirmAction.onConfirm()
                  setConfirmAction(null)
                }}
              >
                Confirm
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}

function EditProjectModal({ project, onClose, onSaved }) {
  const [form, setForm] = useState({
    name: project.name,
    description: project.description || '',
    startDate: project.startDate || '',
    endDate: project.endDate || '',
  })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await projectApi.update(project.id, {
        ...form,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
      })
      onSaved()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="Edit project" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert-error">{error}</div>}
        <label>
          Name
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
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
          {saving ? 'Saving…' : 'Save'}
        </button>
      </form>
    </Modal>
  )
}

function AddProjectMemberModal({ projectId, candidates, onClose, onAdded }) {
  const [userId, setUserId] = useState(candidates[0]?.user.id || '')
  const [role, setRole] = useState('DEVELOPER')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!userId) {
      setError('No workspace members available to add')
      return
    }
    setSaving(true)
    setError('')
    try {
      await projectApi.addMember(projectId, Number(userId), role)
      onAdded()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="Add project member" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert-error">{error}</div>}
        <label>
          Workspace member
          <select value={userId} onChange={(e) => setUserId(e.target.value)}>
            {candidates.length === 0 && <option value="">No eligible members</option>}
            {candidates.map((wm) => (
              <option key={wm.user.id} value={wm.user.id}>
                {wm.user.fullName} ({wm.user.email})
              </option>
            ))}
          </select>
        </label>
        <label>
          Role
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="DEVELOPER">DEVELOPER</option>
            <option value="PROJECT_MANAGER">PROJECT_MANAGER</option>
          </select>
        </label>
        <button type="submit" disabled={saving}>
          {saving ? 'Adding…' : 'Add'}
        </button>
      </form>
    </Modal>
  )
}
