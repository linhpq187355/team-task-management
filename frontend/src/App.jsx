import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProfilePage from './pages/ProfilePage'
import WorkspaceListPage from './pages/WorkspaceListPage'
import WorkspaceDetailPage from './pages/WorkspaceDetailPage'
import ProjectDetailPage from './pages/ProjectDetailPage'
import TaskBoardPage from './pages/TaskBoardPage'
import TaskDetailPage from './pages/TaskDetailPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<Navigate to="/workspaces" replace />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/workspaces" element={<WorkspaceListPage />} />
              <Route path="/workspaces/:workspaceId" element={<WorkspaceDetailPage />} />
              <Route path="/projects/:projectId" element={<ProjectDetailPage />} />
              <Route path="/projects/:projectId/board" element={<TaskBoardPage />} />
              <Route path="/tasks/:taskId" element={<TaskDetailPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/workspaces" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
