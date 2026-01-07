import { Layout, Menu } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  DashboardOutlined,
  UserOutlined,
  KeyOutlined,
  SafetyOutlined,
  FileTextOutlined,
  BarChartOutlined,
  DatabaseOutlined,
  FileSearchOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { useUIStore } from '../../store/uiStore'

const { Sider } = Layout

const menuItems = [
  {
    key: '/dashboard',
    icon: <DashboardOutlined />,
    label: 'Dashboard',
  },
  {
    key: '/users',
    icon: <UserOutlined />,
    label: 'Пользователи',
  },
  {
    key: '/codes',
    icon: <KeyOutlined />,
    label: 'Коды',
  },
  {
    key: '/verification',
    icon: <SafetyOutlined />,
    label: 'Верификация',
  },
  {
    key: '/auth-requests',
    icon: <FileTextOutlined />,
    label: 'Запросы авторизации',
  },
  {
    key: '/metrics',
    icon: <BarChartOutlined />,
    label: 'Метрики',
  },
  {
    key: '/kafka',
    icon: <DatabaseOutlined />,
    label: 'Kafka',
  },
  {
    key: '/logs',
    icon: <FileSearchOutlined />,
    label: 'Логи',
  },
  {
    key: '/settings',
    icon: <SettingOutlined />,
    label: 'Настройки',
  },
]

export const Sidebar = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { sidebarCollapsed } = useUIStore()

  return (
    <Sider
      collapsible
      collapsed={sidebarCollapsed}
      onCollapse={(collapsed) => useUIStore.getState().setSidebarCollapsed(collapsed)}
      width={200}
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
      }}
    >
      <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.3)' }} />
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[location.pathname]}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
      />
    </Sider>
  )
}

