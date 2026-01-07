import { useState, useMemo, useCallback } from 'react'
import { Card, Select, Button, Input } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { DataTable } from '../../components/common/DataTable'
import { FilterPanel } from '../../components/common/FilterPanel'
import { StatusBadge } from '../../components/common/StatusBadge'
import { useVerificationSessions } from '../../hooks/useVerification'
import { VerificationSession } from '../../types/verification'
import { formatDateTime } from '../../utils/formatters'

export const VerificationSessionsList = () => {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [status, setStatus] = useState<string>()
  const [userId, setUserId] = useState<number | undefined>()
  const navigate = useNavigate()
  
  const { data, isLoading, refetch } = useVerificationSessions(page, size, status, userId)

  const handlePageChange = useCallback((newPage: number, newSize: number) => {
    setPage(newPage)
    setSize(newSize)
  }, [])

  const handleFilterReset = useCallback(() => {
    setStatus(undefined)
    setUserId(undefined)
    setPage(0)
  }, [])

  const handleUserIdChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value ? parseInt(e.target.value) : undefined
    setUserId(value)
    setPage(0)
  }, [])

  const handleStatusChange = useCallback((value: string | undefined) => {
    setStatus(value)
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
      title: 'Correlation ID',
      dataIndex: 'correlationId',
      key: 'correlationId',
      width: 300,
      render: (id: string) => (
        <code style={{ fontSize: '12px' }}>{id}</code>
      ),
    },
    {
      title: 'Telegram User ID',
      dataIndex: 'telegramUserId',
      key: 'telegramUserId',
      width: 150,
    },
    {
      title: 'Код',
      dataIndex: 'code',
      key: 'code',
      width: 100,
      render: (code: string) => <code>{code}</code>,
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => <StatusBadge status={status} />,
    },
    {
      title: 'Создана',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => formatDateTime(date),
    },
    {
      title: 'Обновлена',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      render: (date: string) => formatDateTime(date),
    },
    {
      title: 'Действия',
      key: 'actions',
      width: 100,
      render: (_: any, record: VerificationSession) => (
        <Button 
          type="link" 
          onClick={() => navigate(`/verification/${record.correlationId}`)}
        >
          Детали
        </Button>
      ),
    },
  ], [navigate])

  return (
    <div>
      <h1>Сессии верификации</h1>
      
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
          value={status}
          onChange={handleStatusChange}
        >
          <Select.Option value="PENDING">PENDING</Select.Option>
          <Select.Option value="CONFIRMED">CONFIRMED</Select.Option>
          <Select.Option value="REVOKED">REVOKED</Select.Option>
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
