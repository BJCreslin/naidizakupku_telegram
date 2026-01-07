import { Suspense, lazy } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Spin, App as AntApp } from 'antd'
import { Layout } from './components/layout/Layout'
import { Login } from './pages/Auth/Login'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { ErrorBoundary } from './components/common/ErrorBoundary'

// Lazy loading для страниц
const Dashboard = lazy(() => import('./pages/Dashboard/Dashboard').then(m => ({ default: m.Dashboard })))
const UsersList = lazy(() => import('./pages/Users/UsersList').then(m => ({ default: m.UsersList })))
const UserDetails = lazy(() => import('./pages/Users/UserDetails').then(m => ({ default: m.UserDetails })))
const CodesList = lazy(() => import('./pages/Codes/CodesList').then(m => ({ default: m.CodesList })))
const VerificationSessionsList = lazy(() => import('./pages/Verification/SessionsList').then(m => ({ default: m.VerificationSessionsList })))
const AuthRequestsList = lazy(() => import('./pages/AuthRequests/AuthRequestsList').then(m => ({ default: m.AuthRequestsList })))
const MetricsDashboard = lazy(() => import('./pages/Metrics/MetricsDashboard').then(m => ({ default: m.MetricsDashboard })))
const KafkaStatus = lazy(() => import('./pages/Kafka/KafkaStatus').then(m => ({ default: m.KafkaStatus })))
const LogsViewer = lazy(() => import('./pages/Logs/LogsViewer'))
const Settings = lazy(() => import('./pages/Settings/Settings').then(m => ({ default: m.Settings })))

// Компонент загрузки
const PageLoader = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
    <Spin size="large" />
  </div>
)

export function App() {
  return (
    <ErrorBoundary>
      <AntApp>
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
          <Route 
            path="dashboard" 
            element={
              <Suspense fallback={<PageLoader />}>
                <Dashboard />
              </Suspense>
            } 
          />
          <Route 
            path="users" 
            element={
              <Suspense fallback={<PageLoader />}>
                <UsersList />
              </Suspense>
            } 
          />
          <Route 
            path="users/:id" 
            element={
              <Suspense fallback={<PageLoader />}>
                <UserDetails />
              </Suspense>
            } 
          />
          <Route 
            path="codes" 
            element={
              <Suspense fallback={<PageLoader />}>
                <CodesList />
              </Suspense>
            } 
          />
          <Route 
            path="verification" 
            element={
              <Suspense fallback={<PageLoader />}>
                <VerificationSessionsList />
              </Suspense>
            } 
          />
          <Route 
            path="auth-requests" 
            element={
              <Suspense fallback={<PageLoader />}>
                <AuthRequestsList />
              </Suspense>
            } 
          />
          <Route 
            path="metrics" 
            element={
              <Suspense fallback={<PageLoader />}>
                <MetricsDashboard />
              </Suspense>
            } 
          />
          <Route 
            path="kafka" 
            element={
              <Suspense fallback={<PageLoader />}>
                <KafkaStatus />
              </Suspense>
            } 
          />
          <Route 
            path="logs" 
            element={
              <Suspense fallback={<PageLoader />}>
                <LogsViewer />
              </Suspense>
            } 
          />
          <Route 
            path="settings" 
            element={
              <Suspense fallback={<PageLoader />}>
                <Settings />
              </Suspense>
            } 
          />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AntApp>
    </ErrorBoundary>
  )
}

export default App

