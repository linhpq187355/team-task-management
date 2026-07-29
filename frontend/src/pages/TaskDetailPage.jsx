import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { projectApi, taskApi } from '../api/resources'
import { useAuth } from '../context/AuthContext'
import { PipelineTrack, PriorityBadge } from '../components/Badges'
import Modal from '../components/Modal'

const FINAL_STATES = ['DONE', 'CANCELLED']

export default function TaskDetailPage() {
  const { taskId } = useParams()
  const { user } = useAuth()
  const [task, setTask] = useState(null)
  const [project, setProject] = useState(null)
  const [developers, setDevelopers] = useState([])
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [editing, setEditing] = useState(false)
  const [busy, setBusy] = useState(false)

  function load() {
    setError('')
    taskApi
      .get(taskId)
      .then((t) => {
        setTask(t)
        return Promise.all([projectApi.get(t.projectId), projectApi.members(t.projectId)])
      })
      .then(([p, mems]) => {
        setProject(p)
        setDevelopers(mems.filter((m) => m.role === 'DEVELOPER'))
      })
      .catch((err) => setError(err.message))
  }

  useEffect(load, [taskId])

  if (!task) return <div className="page">{error ? <div className="alert-error">{error}</div> : 'Loading…'}</div>

  const canManage =
    project?.myRole === 'PROJECT_MANAGER' || project?.myRoles?.includes('PROJECT_MANAGER')
  const isAssignee = task.assignee?.id === user?.id
  const isFinal = FINAL_STATES.includes(task.status)

  async function runAction(fn) {
    setBusy(true)
    setActionError('')
    try {
      const updated = await fn()
      setTask(updated)
    } catch (err) {
      setActionError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page-narrow">
      <Link to={`/projects/${task.projectId}/board`} className="crumb-back">
        ← Back to task board
      </Link>

      <div className="page-header">
        <h2>{task.title}</h2>
      </div>

      <PipelineTrack status={task.status} />

      {error && <div className="alert-error">{error}</div>}
      {actionError && <div className="alert-error">{actionError}</div>}

      <div className="task-meta-row">
        <PriorityBadge priority={task.priority} />
        {task.dueDate && <span className="meta-item">due <b>{task.dueDate}</b></span>}
        <span className="meta-item">assignee <b>{task.assignee?.fullName || 'unassigned'}</b></span>
        <span className="meta-item">opened by <b>{task.createdBy?.fullName}</b></span>
      </div>

      <p className="task-description">
        {task.description || <span className="muted">No description was written for this task.</span>}
      </p>

      <div className="btn-row">
        {task.status === 'TODO' && isAssignee && (
          <button disabled={busy} onClick={() => runAction(() => taskApi.start(task.id))}>
            Start
          </button>
        )}
        {task.status === 'IN_PROGRESS' && isAssignee && (
          <button disabled={busy} onClick={() => runAction(() => taskApi.submitReview(task.id))}>
            Submit for review
          </button>
        )}
        {task.status === 'REVIEW' && canManage && (
          <>
            <button disabled={busy} onClick={() => runAction(() => taskApi.approve(task.id))}>
              Approve
            </button>
            <button
              className="secondary-btn"
              disabled={busy}
              onClick={() => runAction(() => taskApi.requestChanges(task.id))}
            >
              Request changes
            </button>
          </>
        )}
        {['TODO', 'IN_PROGRESS', 'REVIEW'].includes(task.status) && canManage && (
          <button
            className="danger-btn"
            disabled={busy}
            onClick={() => {
              if (confirm('Cancel this task?')) runAction(() => taskApi.cancel(task.id))
            }}
          >
            Cancel
          </button>
        )}
        {canManage && !isFinal && (
          <button className="secondary-btn" onClick={() => setEditing(true)}>
            Edit
          </button>
        )}
      </div>

      {editing && (
        <EditTaskModal
          task={task}
          developers={developers}
          onClose={() => setEditing(false)}
          onSaved={(updated) => {
            setEditing(false)
            setTask(updated)
          }}
        />
      )}
    </div>
  )
}

function EditTaskModal({ task, developers, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: task.title,
    description: task.description || '',
    priority: task.priority,
    dueDate: task.dueDate || '',
    assigneeId: task.assignee?.id || '',
  })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const updated = await taskApi.update(task.id, {
        ...form,
        dueDate: form.dueDate || null,
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
      })
      onSaved(updated)
    } catch (err) {
      setError(err.errors?.join(', ') || err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal title="Edit task" onClose={onClose}>
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
          <select value={form.assigneeId} onChange={(e) => setForm({ ...form, assigneeId: e.target.value })}>
            <option value="">Unassigned</option>
            {developers.map((d) => (
              <option key={d.userId} value={d.userId}>
                {d.fullName}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={saving}>
          {saving ? 'Saving…' : 'Save'}
        </button>
      </form>
    </Modal>
  )
}
