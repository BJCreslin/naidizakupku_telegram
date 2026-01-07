import { useState, useMemo, useCallback } from 'react'
import { Card, Space, Select, Button, Input, Popconfirm } from 'antd'
import { ReloadOutlined, DeleteOutlined } from '@ant-design/icons'
import { DataTable } from '../../components/common/DataTable'
import { FilterPanel } from '../../components/common/FilterPanel'
import { StatusBadge } from '../../components/common/StatusBadge'
import { useCodes, useDeleteCode } from '../../hooks/useCodes'
import { Code } from '../../types/code'
import { formatDateTime } from '../../utils/formatters'

export const CodesList = () => {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [userId, setUserId] = useState<number | undefined>()
  const [active, setActive] = useState<boolean | undefined>()
  const [expired, setExpired] = useState<boolean | undefined>()
  
  const { data, isLoading, refetch } = useCodes(page, size, userId, active, expired)
  const deleteCode = useDeleteCode()

  const handlePageChange = useCallback((newPage: number, newSize: number) => {
    setPage(newPage)
    setSize(newSize)
  }, [])

  const handleFilterReset = useCallback(() => {
    setUserId(undefined)
    setActive(undefined)
    setExpired(undefined)
    setPage(0)
  }, [])

  const handleDelete = useCallback(async (id: number) => {
    await deleteCode.mutateAsync(id)
  }, [deleteCode])

  const handleUserIdChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value ? parseInt(e.target.value) : undefined
    setUserId(value)
    setPage(0)
  }, [])

  const handleActiveChange = useCallback((value: string | undefined) => {
    setActive(value === 'active' ? true : value === 'inactive' ? false : undefined)
    setPage(0)
  }, [])

  const handleExpiredChange = useCallback((value: boolean | undefined) => {
    setExpired(value)
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
      title: 'Код',
      dataIndex: 'code',
      key: 'code',
      width: 120,
      render: (code: string) => <code>{code}</code>,
    },
    {
      title: 'Telegram User ID',
      dataIndex: 'telegramUserId',
      key: 'telegramUserId',
      width: 150,
    },
    {
      title: 'Статус',
      key: 'status',
      width: 120,
      render: (_: any, record: Code) => (
        <StatusBadge status={record.isActive ? 'ACTIVE' : 'EXPIRED'} />
      ),
    },
    {
      title: 'Истекает',
      dataIndex: 'expiresAt',
      key: 'expiresAt',
      width: 180,
      render: (date: string) => formatDateTime(date),
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
      width: 100,
      render: (_: any, record: Code) => (
        <Popconfirm
          title="Удалить код?"
          description="Это действие нельзя отменить"
          onConfirm={() => handleDelete(record.id)}
          okText="Да"
          cancelText="Нет"
        >
          <Button 
            type="link" 
            danger 
            icon={<DeleteOutlined />}
            loading={deleteCode.isPending}
          >
            Удалить
          </Button>
        </Popconfirm>
      ),
    },
  ], [handleDelete, deleteCode.isPending])

  return (
    <div>
      <h1>Коды</h1>
      
      <FilterPanel onReset={handleFilterReset}>
        <Input
          type="number"
          placeholder="Telegram User ID"
          value={userId}
          onChange={handleUserIdChange}
          style={{ width: 200 }}
        />
        <Select
          placeholder="Статус"
          allowClear
          style={{ width: 150 }}
          value={active !== undefined ? (active ? 'active' : 'inactive') : undefined}
          onChange={handleActiveChange}
        >
          <Select.Option value="active">Активные</Select.Option>
          <Select.Option value="inactive">Неактивные</Select.Option>
        </Select>
        <Select
          placeholder="Истекшие"
          allowClear
          style={{ width: 150 }}
          value={expired}
          onChange={handleExpiredChange}
        >
          <Select.Option value={true}>Да</Select.Option>
          <Select.Option value={false}>Нет</Select.Option>
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
