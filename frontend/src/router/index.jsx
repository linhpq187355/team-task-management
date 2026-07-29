import { createBrowserRouter, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ProfilePage from '../pages/ProfilePage';
import WorkspaceListPage from '../pages/WorkspaceListPage';
import WorkspaceDetailPage from '../pages/WorkspaceDetailPage';
import ProjectDetailPage from '../pages/ProjectDetailPage';
import TaskBoardPage from '../pages/TaskBoardPage';
import TaskDetailPage from '../pages/TaskDetailPage';
import ProtectedRoute from '../components/ProtectedRoute';
import Layout from '../components/Layout';

export const router = createBrowserRouter([
    {
        path: '/login',
        element: <LoginPage />
    },
    {
        path: '/register',
        element: <RegisterPage />
    },
    {
        element: <ProtectedRoute />,
        children: [
            {
                element: <Layout />,
                path: '/',
                children: [
                    {
                        index: true,
                        element: <Navigate to="/workspaces" replace />
                    },
                    {
                        path: 'profile',
                        element: <ProfilePage />
                    },
                    {
                        path: 'workspaces',
                        element: <WorkspaceListPage />
                    },
                    {
                        path: 'workspaces/:workspaceId',
                        element: <WorkspaceDetailPage />
                    },
                    {
                        path: 'projects/:projectId',
                        element: <ProjectDetailPage />
                    },
                    {
                        path: 'projects/:projectId/board',
                        element: <TaskBoardPage />
                    },
                    {
                        path: 'tasks/:taskId',
                        element: <TaskDetailPage />
                    }
                ]
            }
        ]
    },
    {
        path: '*',
        element: <Navigate to="/workspaces" replace />
    }
]);
