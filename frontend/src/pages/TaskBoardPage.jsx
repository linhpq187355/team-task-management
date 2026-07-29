import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { projectApi, taskApi } from '../api/resources'
import { PriorityBadge } from '../components/Badges'
import Modal from '../components/Modal'

const COLUMNS = [
  { status: 'TODO', label: 'To Do' },
  { status: 'IN_PROGRESS', label: 'In Progress' },
  { status: 'REVIEW', label: 'Review' },
  { status: 'DONE', label: 'Done' },
  { status: 'CANCELLED', label: 'Cancelled' },
]

export default function TaskBoardPage() {
  const { projectId } = useParams()
  const [project, setProject] = useState(null)
  const [tasks, setTasks] = useState([])
  const [members, setMembers] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({ priority: '', assigneeId: '', keyword: '' })
  const [showCreate, setShowCreate] = useState(false)

  function load() {
    setLoading(true)
    setError('')
    Promise.all([
      projectApi.get(projectId),
      taskApi.listByProject(projectId, { size: 200 }),
      projectApi.members(projectId),
    ])
      .then(([p, page, mems]) => {
        setProject(p)
        setTasks(page.content)
        setMembers(mems)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [projectId])

  const filtered = useMemo(() => {
    return tasks.filter((t) => {
      if (filters.priority && t.priority !== filters.priority) return false
      if (filters.assigneeId && String(t.assignee?.id) !== filters.assigneeId) return false
      if (filters.keyword && !t.title.toLowerCase().includes(filters.keyword.toLowerCase())) return false
      return true
    })
  }, [tasks, filters])

  const canManage =
    project?.myRole === 'PROJECT_MANAGER' || project?.myRoles?.includes('PROJECT_MANAGER')
  const developers = members.filter((m) => m.role === 'DEVELOPER')

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <Link to={`/projects/${projectId}`} className="crumb-back">
            ← Back to project
          </Link>
          <h2>Task board</h2>
        </div>
        {canManage && <button onClick={() => setShowCreate(true)}>+ New task</button>}
      </div>

      {error && <div className="alert-error">{error}</div>}

      <div className="filter-row">
        <input
          placeholder="Search title…"
          value={filters.keyword}
          onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
        />
        <select
          value={filters.priority}
          onChange={(e) => setFilters({ ...filters, priority: e.target.value })}
        >
          <option value="">All priorities</option>
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
          <option value="URGENT">URGENT</option>
        </select>
        <select
          value={filters.assigneeId}
          onChange={(e) => setFilters({ ...filters, assigneeId: e.target.value })}
        >
          <option value="">All assignees</option>
          {members.map((m) => (
            <option key={m.userId} value={m.userId}>
              {m.fullName}
            </option>
          ))}
        </select>
      </div>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : (
        <div className="board">
          {COLUMNS.map((col) => (
            <div key={col.status} className="board-column">
              <h4>
                {col.label} <span className="muted">({filtered.filter((t) => t.status === col.status).length})</span>
              </h4>
              <div className="board-column-body">
                {filtered
                  .filter((t) => t.status === col.status)
                  .map((t) => (
                    <Link key={t.id} to={`/tasks/${t.id}`} className="task-card">
                      <div className="task-card-title">{t.title}</div>
                      <div className="task-card-meta">
                        <PriorityBadge priority={t.priority} />
                        {t.dueDate && <span className="muted small">{t.dueDate}</span>}
                      </div>
                      {t.assignee && <div className="muted small">{t.assignee.fullName}</div>}
                    </Link>
                  ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {showCreate && (
        <CreateTaskModal
          projectId={projectId}
          developers={developers}
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

function CreateTaskModal({ projectId, developers, onClose, onCreated }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    dueDate: '',
    assigneeId: '',
  })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await taskApi.create(projectId, {
        ...form,
        dueDate: form.dueDate || null,
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
      })
      onCreated()
    } catch (err) {
      setError(err.errors?.join(', ') || err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="New task" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert-error">{error}</div>}
        <label>
          Title
          <input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
            maxLength={255}
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
          Priority
          <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="URGENT">URGENT</option>
          </select>
        </label>
        <label>
          Due date
          <input
            type="date"
            value={form.dueDate}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
          />
        </label>
        <label>
          Assignee
          <select
            value={form.assigneeId}
            onChange={(e) => setForm({ ...form, assigneeId: e.target.value })}
          >
            <option value="">Unassigned</option>
            {developers.map((d) => (
              <option key={d.userId} value={d.userId}>
                {d.fullName}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={saving}>
          {saving ? 'Creating…' : 'Create'}
        </button>
      </form>
    </Modal>
  )
}
