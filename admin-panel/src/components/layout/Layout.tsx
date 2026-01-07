import { Outlet } from 'react-router-dom'
import { Layout as AntLayout } from 'antd'
import { Sidebar } from './Sidebar'
import { Header } from './Header'
import { useUIStore } from '../../store/uiStore'

const { Content } = AntLayout

export const Layout = () => {
  const { sidebarCollapsed } = useUIStore()
  const sidebarWidth = sidebarCollapsed ? 80 : 200

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sidebar />
      <AntLayout style={{ marginLeft: sidebarWidth }}>
        <Header />
        <Content style={{ margin: '24px 16px', padding: 24, background: '#fff', minHeight: 280 }}>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

