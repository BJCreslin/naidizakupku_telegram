import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Button, Space, Spin } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useUser, useActivateUser, useDeactivateUser } from '../../hooks/useUsers'
import { StatusBadge } from '../../components/common/StatusBadge'
import { formatDateTime } from '../../utils/formatters'

export const UserDetails = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const userId = id ? parseInt(id) : 0
  
  const { data: user, isLoading } = useUser(userId)
  const activateUser = useActivateUser()
  const deactivateUser = useDeactivateUser()

  const handleActivate = async () => {
    await activateUser.mutateAsync(userId)
  }

  const handleDeactivate = async () => {
    await deactivateUser.mutateAsync(userId)
  }

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!user) {
    return <div>Пользователь не найден</div>
  }

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/users')}>
          Назад к списку
        </Button>
      </Space>

      <Card title={`Пользователь #${user.id}`}>
        <Descriptions bordered column={2}>
          <Descriptions.Item label="ID">{user.id}</Descriptions.Item>
          <Descriptions.Item label="Telegram ID">{user.telegramId}</Descriptions.Item>
          <Descriptions.Item label="Username">{user.username || '-'}</Descriptions.Item>
          <Descriptions.Item label="Имя">{user.firstName || '-'}</Descriptions.Item>
          <Descriptions.Item label="Фамилия">{user.lastName || '-'}</Descriptions.Item>
          <Descriptions.Item label="Статус">
            <StatusBadge status={user.active ? 'ACTIVE' : 'INACTIVE'} />
          </Descriptions.Item>
          <Descriptions.Item label="Дата создания">
            {formatDateTime(user.createdAt)}
          </Descriptions.Item>
          <Descriptions.Item label="Последнее обновление">
            {formatDateTime(user.updatedAt)}
          </Descriptions.Item>
        </Descriptions>

        <Space style={{ marginTop: 16 }}>
          {user.active ? (
            <Button 
              danger 
              onClick={handleDeactivate}
              loading={deactivateUser.isPending}
            >
              Деактивировать
            </Button>
          ) : (
            <Button 
              type="primary"
              onClick={handleActivate}
              loading={activateUser.isPending}
            >
              Активировать
            </Button>
          )}
        </Space>
      </Card>
    </div>
  )
}
