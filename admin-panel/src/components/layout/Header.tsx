import { Layout, Dropdown, Avatar, Button, Popconfirm } from 'antd'
import { UserOutlined, LogoutOutlined, MoonOutlined, SunOutlined } from '@ant-design/icons'
import { useAuthStore } from '../../store/authStore'
import { useUIStore } from '../../store/uiStore'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../../api/auth'
import { showSuccess, showError } from '../../utils/notifications'

const { Header: AntHeader } = Layout

export const Header = () => {
  const { user, clearAuth } = useAuthStore()
  const { theme, setTheme } = useUIStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await authApi.logout()
      showSuccess({ title: 'Выход выполнен' })
    } catch (error) {
      showError({ title: 'Ошибка', message: 'Не удалось выйти из системы' })
    } finally {
      clearAuth()
      navigate('/login')
    }
  }

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
    showSuccess({ 
      title: `Тема изменена на ${newTheme === 'dark' ? 'темную' : 'светлую'}`,
      duration: 2
    })
  }

  const menuItems = [
    {
      key: 'theme',
      icon: theme === 'dark' ? <SunOutlined /> : <MoonOutlined />,
      label: theme === 'dark' ? 'Светлая тема' : 'Темная тема',
      onClick: toggleTheme,
    },
    {
      type: 'divider' as const,
    },
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
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <div style={{ fontSize: 18, fontWeight: 'bold' }}>Admin Panel</div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <Button 
          type="text" 
          icon={theme === 'dark' ? <SunOutlined /> : <MoonOutlined />}
          onClick={toggleTheme}
          title={theme === 'dark' ? 'Переключить на светлую тему' : 'Переключить на темную тему'}
        />
        <Dropdown menu={{ items: menuItems }} placement="bottomRight">
          <Button type="text" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Avatar icon={<UserOutlined />} />
            <span>{user?.username}</span>
          </Button>
        </Dropdown>
      </div>
    </AntHeader>
  )
}

