import { useState } from 'react'
import { Card, Select, Input, Button } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { DataTable } from '../../components/common/DataTable'
import { FilterPanel } from '../../components/common/FilterPanel'
import { StatusBadge } from '../../components/common/StatusBadge'
import { logsApi, LogEntry } from '../../api/logs'
import { useQuery } from '@tanstack/react-query'
import { PagedResponse } from '../../types/api'
import { formatDateTime } from '../../utils/formatters'

export const LogsViewer = () => {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(50)
  const [level, setLevel] = useState<string>()
  const [traceId, setTraceId] = useState<string>()
  const [correlationId, setCorrelationId] = useState<string>()
  
  const { data, isLoading, refetch } = useQuery<PagedResponse<LogEntry>>({
    queryKey: ['logs', page, size, level, traceId, correlationId],
    queryFn: () => logsApi.getLogs(page, size, level, traceId, correlationId),
  })

  const handlePageChange = (newPage: number, newSize: number) => {
    setPage(newPage)
    setSize(newSize)
  }

  const handleFilterReset = () => {
    setLevel(undefined)
    setTraceId(undefined)
    setCorrelationId(undefined)
    setPage(0)
  }

  const columns = [
    {
      title: 'Время',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (date: string) => formatDateTime(date),
    },
    {
      title: 'Уровень',
      dataIndex: 'level',
      key: 'level',
      width: 100,
      render: (level: string) => <StatusBadge status={level} />,
    },
    {
      title: 'Логгер',
      dataIndex: 'logger',
      key: 'logger',
      width: 250,
      render: (logger: string) => (
        <code style={{ fontSize: '11px' }}>{logger}</code>
      ),
    },
    {
      title: 'Сообщение',
      dataIndex: 'message',
      key: 'message',
      ellipsis: true,
    },
    {
      title: 'Trace ID',
      dataIndex: 'traceId',
      key: 'traceId',
      width: 200,
      render: (traceId: string | undefined) => 
        traceId ? <code style={{ fontSize: '11px' }}>{traceId}</code> : '-',
    },
    {
      title: 'Correlation ID',
      dataIndex: 'correlationId',
      key: 'correlationId',
      width: 200,
      render: (correlationId: string | undefined) => 
        correlationId ? <code style={{ fontSize: '11px' }}>{correlationId}</code> : '-',
    },
  ]

  return (
    <div>
      <h1>Логи</h1>
      
      <FilterPanel onReset={handleFilterReset}>
        <Select
          placeholder="Уровень"
          allowClear
          style={{ width: 150 }}
          value={level}
          onChange={(value) => {
            setLevel(value)
            setPage(0)
          }}
        >
          <Select.Option value="INFO">INFO</Select.Option>
          <Select.Option value="WARN">WARN</Select.Option>
          <Select.Option value="ERROR">ERROR</Select.Option>
          <Select.Option value="DEBUG">DEBUG</Select.Option>
        </Select>
        <Input
          placeholder="Trace ID"
          value={traceId}
          onChange={(e) => {
            setTraceId(e.target.value || undefined)
            setPage(0)
          }}
          style={{ width: 300 }}
        />
        <Input
          placeholder="Correlation ID"
          value={correlationId}
          onChange={(e) => {
            setCorrelationId(e.target.value || undefined)
            setPage(0)
          }}
          style={{ width: 300 }}
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
          />
        )}
      </Card>
    </div>
  )
}
