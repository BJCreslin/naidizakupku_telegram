import { useState, useMemo, useCallback } from 'react'
import { Card, Space, Select, Button, Popconfirm } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { DataTable } from '../../components/common/DataTable'
import { SearchBar } from '../../components/common/SearchBar'
import { FilterPanel } from '../../components/common/FilterPanel'
import { StatusBadge } from '../../components/common/StatusBadge'
import { useUsers, useActivateUser, useDeactivateUser } from '../../hooks/useUsers'
import { User } from '../../types/user'
import { formatDateTime } from '../../utils/formatters'
import { showSuccess, showError } from '../../utils/notifications'

export const UsersList = () => {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [search, setSearch] = useState<string>()
  const [active, setActive] = useState<boolean | undefined>()
  const navigate = useNavigate()
  
  const { data, isLoading, refetch } = useUsers(page, size, search, active)
  const activateUser = useActivateUser()
  const deactivateUser = useDeactivateUser()

  const handlePageChange = useCallback((newPage: number, newSize: number) => {
    setPage(newPage)
    setSize(newSize)
  }, [])

  const handleSearch = useCallback((value: string) => {
    setSearch(value || undefined)
    setPage(0)
  }, [])

  const handleFilterReset = useCallback(() => {
    setSearch(undefined)
    setActive(undefined)
    setPage(0)
  }, [])

  const handleActivate = useCallback(async (id: number) => {
    try {
      await activateUser.mutateAsync(id)
      showSuccess({ title: 'Пользователь активирован' })
    } catch (error) {
      showError({ title: 'Ошибка', message: 'Не удалось активировать пользователя' })
    }
  }, [activateUser])

  const handleDeactivate = useCallback(async (id: number) => {
    try {
      await deactivateUser.mutateAsync(id)
      showSuccess({ title: 'Пользователь деактивирован' })
    } catch (error) {
      showError({ title: 'Ошибка', message: 'Не удалось деактивировать пользователя' })
    }
  }, [deactivateUser])

  const handleActiveChange = useCallback((value: boolean | undefined) => {
    setActive(value)
    setPage(0)
  }, [])

  const columns = useMemo(() => [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Telegram ID',
      dataIndex: 'telegramId',
      key: 'telegramId',
      width: 120,
    },
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: 'Имя',
      key: 'name',
      render: (_: any, record: User) => 
        `${record.firstName || ''} ${record.lastName || ''}`.trim() || '-',
    },
    {
      title: 'Статус',
      dataIndex: 'active',
      key: 'active',
      width: 100,
      render: (active: boolean) => (
        <StatusBadge status={active ? 'ACTIVE' : 'INACTIVE'} />
      ),
    },
    {
      title: 'Создан',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => formatDateTime(date),
    },
    {
      title: 'Действия',
      key: 'actions',
      width: 200,
      render: (_: any, record: User) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/users/${record.id}`)}>
            Детали
          </Button>
          {record.active ? (
            <Popconfirm
              title="Деактивировать пользователя?"
              description="Пользователь не сможет использовать систему после деактивации."
              onConfirm={() => handleDeactivate(record.id)}
              okText="Да"
              cancelText="Нет"
              okButtonProps={{ danger: true }}
            >
              <Button 
                type="link" 
                danger 
                loading={deactivateUser.isPending}
              >
                Деактивировать
              </Button>
            </Popconfirm>
          ) : (
            <Button 
              type="link" 
              onClick={() => handleActivate(record.id)}
              loading={activateUser.isPending}
            >
              Активировать
            </Button>
          )}
        </Space>
      ),
    },
  ], [navigate, handleActivate, handleDeactivate, activateUser.isPending, deactivateUser.isPending])

  return (
    <div>
      <h1>Пользователи</h1>
      
      <FilterPanel onReset={handleFilterReset}>
        <SearchBar
          placeholder="Поиск по username, имени или Telegram ID"
          value={search}
          onChange={setSearch}
          onSearch={handleSearch}
          style={{ width: 300 }}
        />
        <Select
          placeholder="Статус"
          allowClear
          style={{ width: 150 }}
          value={active}
          onChange={handleActiveChange}
        >
          <Select.Option value={true}>Активные</Select.Option>
          <Select.Option value={false}>Неактивные</Select.Option>
        </Select>
        <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
          Обновить
        </Button>
      </FilterPanel>

      <Card>
        {data && (
          <DataTable
            data={data}
            columns={columns}
            loading={isLoading}
            onPageChange={handlePageChange}
            virtualized={true}
            virtualizedHeight={600}
          />
        )}
      </Card>
    </div>
  )
}
