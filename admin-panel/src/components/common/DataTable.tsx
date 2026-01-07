import { Table, TableProps, Pagination, Space } from 'antd'
import { PagedResponse } from '../../types/api'

interface DataTableProps<T> extends Omit<TableProps<T>, 'pagination'> {
  data: PagedResponse<T>
  onPageChange?: (page: number, pageSize: number) => void
  loading?: boolean
}

export function DataTable<T extends { id?: number | string }>({
  data,
  onPageChange,
  loading = false,
  ...tableProps
}: DataTableProps<T>) {
  const handlePageChange = (page: number, pageSize: number) => {
    onPageChange?.(page - 1, pageSize) // Ant Design использует 1-based индексацию
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      <Table<T>
        {...tableProps}
        dataSource={data.content}
        loading={loading}
        pagination={false}
        rowKey={(record) => record.id?.toString() || Math.random().toString()}
      />
      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Pagination
          current={data.page + 1}
          pageSize={data.size}
          total={data.totalElements}
          showSizeChanger
          showQuickJumper
          showTotal={(total, range) => `${range[0]}-${range[1]} из ${total}`}
          onChange={handlePageChange}
          onShowSizeChange={handlePageChange}
          pageSizeOptions={['10', '20', '50', '100']}
        />
      </div>
    </Space>
  )
}
