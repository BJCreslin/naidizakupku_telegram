import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { Login } from './pages/Auth/Login'
import { Dashboard } from './pages/Dashboard/Dashboard'
import { UsersList } from './pages/Users/UsersList'
import { UserDetails } from './pages/Users/UserDetails'
import { CodesList } from './pages/Codes/CodesList'
import { VerificationSessionsList } from './pages/Verification/SessionsList'
import { AuthRequestsList } from './pages/AuthRequests/AuthRequestsList'
import { MetricsDashboard } from './pages/Metrics/MetricsDashboard'
import { KafkaStatus } from './pages/Kafka/KafkaStatus'
import { LogsViewer } from './pages/Logs/LogsViewer'
import { Settings } from './pages/Settings/Settings'
import { ProtectedRoute } from './components/layout/ProtectedRoute'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="users" element={<UsersList />} />
          <Route path="users/:id" element={<UserDetails />} />
          <Route path="codes" element={<CodesList />} />
          <Route path="verification" element={<VerificationSessionsList />} />
          <Route path="auth-requests" element={<AuthRequestsList />} />
          <Route path="metrics" element={<MetricsDashboard />} />
          <Route path="kafka" element={<KafkaStatus />} />
          <Route path="logs" element={<LogsViewer />} />
          <Route path="settings" element={<Settings />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

