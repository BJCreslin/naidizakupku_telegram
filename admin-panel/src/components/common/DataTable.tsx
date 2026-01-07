import { memo, useCallback, useMemo } from 'react'
import { Table, TableProps, Pagination, Space } from 'antd'
import { PagedResponse } from '../../types/api'

interface DataTableProps<T> extends Omit<TableProps<T>, 'pagination'> {
  data: PagedResponse<T>
  onPageChange?: (page: number, pageSize: number) => void
  loading?: boolean
  virtualized?: boolean
  virtualizedHeight?: number
}

function DataTableComponent<T extends { id?: number | string }>({
  data,
  onPageChange,
  loading = false,
  virtualized = false,
  virtualizedHeight = 600,
  ...tableProps
}: DataTableProps<T>) {
  const handlePageChange = useCallback((page: number, pageSize: number) => {
    onPageChange?.(page - 1, pageSize) // Ant Design использует 1-based индексацию
  }, [onPageChange])

  const rowKey = useCallback((record: T) => record.id?.toString() || Math.random().toString(), [])

  const paginationConfig = useMemo(() => ({
    current: data.page + 1,
    pageSize: data.size,
    total: data.totalElements,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number, range: [number, number]) => `${range[0]}-${range[1]} из ${total}`,
    onChange: handlePageChange,
    onShowSizeChange: handlePageChange,
    pageSizeOptions: ['10', '20', '50', '100'],
  }), [data.page, data.size, data.totalElements, handlePageChange])

  // Включаем виртуализацию если включена опция и данных больше 50
  const shouldVirtualize = virtualized && data.content.length > 50
  const scrollConfig = shouldVirtualize ? { y: virtualizedHeight } : undefined

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      <Table<T>
        {...tableProps}
        dataSource={data.content}
        loading={loading}
        pagination={false}
        rowKey={rowKey}
        scroll={scrollConfig}
      />
      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Pagination {...paginationConfig} />
      </div>
    </Space>
  )
}

export const DataTable = memo(DataTableComponent) as typeof DataTableComponent
