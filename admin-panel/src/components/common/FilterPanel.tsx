import { memo } from 'react'
import { Card, Space, Button } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { ReactNode } from 'react'

interface FilterPanelProps {
  children: ReactNode
  onReset?: () => void
  title?: string
}

export const FilterPanel = memo(({ children, onReset, title = 'Фильтры' }: FilterPanelProps) => {
  return (
    <Card
      title={title}
      size="small"
      extra={
        onReset && (
          <Button icon={<ReloadOutlined />} onClick={onReset} size="small">
            Сбросить
          </Button>
        )
      }
      style={{ marginBottom: 16 }}
    >
      <Space wrap>{children}</Space>
    </Card>
  )
})

FilterPanel.displayName = 'FilterPanel'

