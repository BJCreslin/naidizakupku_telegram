import { Layout, Dropdown, Avatar, Button } from 'antd'
import { UserOutlined, LogoutOutlined } from '@ant-design/icons'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../../api/auth'

const { Header: AntHeader } = Layout

export const Header = () => {
  const { user, clearAuth } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      clearAuth()
      navigate('/login')
    }
  }

  const menuItems = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Выход',
      onClick: handleLogout,
    },
  ]

  return (
    <AntHeader
      style={{
        padding: '0 24px',
        background: '#fff',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <div style={{ fontSize: 18, fontWeight: 'bold' }}>Admin Panel</div>
      <Dropdown menu={{ items: menuItems }} placement="bottomRight">
        <Button type="text" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Avatar icon={<UserOutlined />} />
          <span>{user?.username}</span>
        </Button>
      </Dropdown>
    </AntHeader>
  )
}

