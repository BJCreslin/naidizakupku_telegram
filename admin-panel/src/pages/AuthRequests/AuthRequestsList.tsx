import { useState, useMemo, useCallback } from 'react'
import { Card, Space, Input, Button } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { DataTable } from '../../components/common/DataTable'
import { FilterPanel } from '../../components/common/FilterPanel'
import { authRequestsApi } from '../../api/authRequests'
import { useQuery } from '@tanstack/react-query'
import { AuthRequest } from '../../api/authRequests'
import { PagedResponse } from '../../types/api'
import { formatDateTime } from '../../utils/formatters'

export const AuthRequestsList = () => {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [userId, setUserId] = useState<number | undefined>()
  const [traceId, setTraceId] = useState<string>()
  const navigate = useNavigate()
  
  const { data, isLoading, refetch } = useQuery<PagedResponse<AuthRequest>>({
    queryKey: ['auth-requests', page, size, userId, traceId],
    queryFn: () => authRequestsApi.getAuthRequests(page, size, userId, traceId),
  })

  const handlePageChange = useCallback((newPage: number, newSize: number) => {
    setPage(newPage)
    setSize(newSize)
  }, [])

  const handleFilterReset = useCallback(() => {
    setUserId(undefined)
    setTraceId(undefined)
    setPage(0)
  }, [])

  const handleUserIdChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value ? parseInt(e.target.value) : undefined
    setUserId(value)
    setPage(0)
  }, [])

  const handleTraceIdChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setTraceId(e.target.value || undefined)
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
      title: 'Trace ID',
      dataIndex: 'traceId',
      key: 'traceId',
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
      render: (code: string | null) => code ? <code>{code}</code> : '-',
    },
    {
      title: 'Создан',
      dataIndex: 'requestedAt',
      key: 'requestedAt',
      width: 180,
      render: (date: string) => formatDateTime(date),
    },
    {
      title: 'Действия',
      key: 'actions',
      width: 100,
      render: (_: any, record: AuthRequest) => (
        <Button 
          type="link" 
          onClick={() => navigate(`/auth-requests/${record.traceId}`)}
        >
          Детали
        </Button>
      ),
    },
  ], [navigate])

  return (
    <div>
      <h1>Запросы авторизации</h1>
      
      <FilterPanel onReset={handleFilterReset}>
        <Input
          type="number"
          placeholder="Telegram User ID"
          value={userId}
          onChange={handleUserIdChange}
          style={{ width: 200 }}
        />
        <Input
          placeholder="Trace ID (UUID)"
          value={traceId}
          onChange={handleTraceIdChange}
          style={{ width: 350 }}
        />
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
