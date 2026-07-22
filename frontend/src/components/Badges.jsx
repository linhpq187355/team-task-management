const STATUS_LABEL = {
  TODO: 'To do',
  IN_PROGRESS: 'In progress',
  REVIEW: 'Review',
  DONE: 'Done',
  CANCELLED: 'Cancelled',
}

const PIPELINE = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE']
const PRIORITY_RANK = { LOW: 1, MEDIUM: 2, HIGH: 3, URGENT: 4 }

export function StatusBadge({ status }) {
  return (
    <span className={`status-chip status-${status}`}>
      <span className="status-chip-dot" />
      {STATUS_LABEL[status] || status}
    </span>
  )
}

export function PriorityBadge({ priority }) {
  const rank = PRIORITY_RANK[priority] || 0
  return (
    <span className={`priority-meter priority-${priority}`} title={priority}>
      <span className="priority-bars">
        {[1, 2, 3, 4].map((n) => (
          <span key={n} className={n <= rank ? 'bar-filled' : 'bar-empty'} />
        ))}
      </span>
      {priority}
    </span>
  )
}

const LEAD_ROLES = new Set(['OWNER', 'PROJECT_MANAGER'])

export function RoleBadge({ role }) {
  return (
    <span className="role-tag" data-tier={LEAD_ROLES.has(role) ? 'lead' : 'member'}>
      {role.replace('_', ' ')}
    </span>
  )
}

/** Signature element: the task lifecycle as a literal track. Order = real information here —
    it mirrors the state machine the backend enforces, not decorative numbering. */
export function PipelineTrack({ status }) {
  if (status === 'CANCELLED') {
    return (
      <div className="pipeline-track pipeline-cancelled">
        <span className="status-chip-dot" />
        Cancelled — this task exited the pipeline
      </div>
    )
  }

  const currentIndex = PIPELINE.indexOf(status)

  return (
    <div className="pipeline-track">
      {PIPELINE.map((step, i) => (
        <div className="pipeline-step" key={step} data-state={i < currentIndex ? 'done' : i === currentIndex ? 'current' : 'pending'}>
          <span className="pipeline-node" />
          <span className="pipeline-label">{STATUS_LABEL[step]}</span>
          {i < PIPELINE.length - 1 && <span className="pipeline-rail" data-filled={i < currentIndex} />}
        </div>
      ))}
    </div>
  )
}
