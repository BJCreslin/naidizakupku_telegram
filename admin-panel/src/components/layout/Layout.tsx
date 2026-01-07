import { Outlet } from 'react-router-dom'
import { Layout as AntLayout } from 'antd'
import { Sidebar } from './Sidebar'
import { Header } from './Header'
import { useUIStore } from '../../store/uiStore'

const { Content } = AntLayout

export const Layout = () => {
  const { sidebarCollapsed } = useUIStore()

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sidebar />
      <AntLayout style={{ marginLeft: sidebarCollapsed ? 80 : 200 }}>
        <Header />
        <Content style={{ margin: '24px 16px', padding: 24, background: '#fff' }}>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

