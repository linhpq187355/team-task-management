import { api } from './client'

export const authApi = {
  login: (email, password) => api.publicPost('/auth/login', { email, password }),
  register: (email, password, fullName) =>
    api.publicPost('/auth/register', { email, password, fullName }),
  logout: (refreshToken) => api.publicPost('/auth/logout', { refreshToken }),
}

export const userApi = {
  me: () => api.get('/users/me'),
  updateProfile: (fullName, avatarUrl) => api.put('/users/me', { fullName, avatarUrl }),
  changePassword: (oldPassword, newPassword, rePassword) =>
    api.put('/users/me/password', { oldPassword, newPassword, rePassword }),
}

export const workspaceApi = {
  list: () => api.get('/workspaces'),
  get: (id) => api.get(`/workspaces/${id}`),
  create: (data) => api.post('/workspaces', data),
  update: (id, data) => api.put(`/workspaces/${id}`, data),
  remove: (id) => api.del(`/workspaces/${id}`),
  members: (id) => api.get(`/workspaces/${id}/members`),
  addMember: (id, email, role) => api.post(`/workspaces/${id}/members`, { email, role }),
  updateMemberRole: (id, userId, role) =>
    api.put(`/workspaces/${id}/members/${userId}/role`, { role }),
  removeMember: (id, userId) => api.del(`/workspaces/${id}/members/${userId}`),
}

export const projectApi = {
  listByWorkspace: (workspaceId) => api.get(`/workspaces/${workspaceId}/projects`),
  get: (id) => api.get(`/projects/${id}`),
  create: (workspaceId, data) => api.post(`/workspaces/${workspaceId}/projects`, data),
  update: (id, data) => api.put(`/projects/${id}`, data),
  remove: (id) => api.del(`/projects/${id}`),
  members: (id) => api.get(`/projects/${id}/members`),
  addMember: (id, userId, role) => api.post(`/projects/${id}/members`, { userId, role }),
  updateMemberRole: (id, userId, role) =>
    api.put(`/projects/${id}/members/${userId}/role`, { role }),
  removeMember: (id, userId) => api.del(`/projects/${id}/members/${userId}`),
}

export const taskApi = {
  listByProject: (projectId, params = {}) => {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') query.set(key, value)
    })
    const qs = query.toString()
    return api.get(`/projects/${projectId}/tasks${qs ? `?${qs}` : ''}`)
  },
  get: (id) => api.get(`/tasks/${id}`),
  create: (projectId, data) => api.post(`/projects/${projectId}/tasks`, data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  start: (id) => api.patch(`/tasks/${id}/start`),
  submitReview: (id) => api.patch(`/tasks/${id}/submit-review`),
  approve: (id) => api.patch(`/tasks/${id}/approve`),
  requestChanges: (id) => api.patch(`/tasks/${id}/request-changes`),
  cancel: (id) => api.patch(`/tasks/${id}/cancel`),
}
