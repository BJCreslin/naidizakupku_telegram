import { memo, useMemo } from 'react'
import { Tag } from 'antd'
import { ReactNode } from 'react'

interface StatusBadgeProps {
  status: string
  children?: ReactNode
}

const statusColors: Record<string, string> = {
  ACTIVE: 'green',
  INACTIVE: 'red',
  PENDING: 'orange',
  CONFIRMED: 'green',
  REVOKED: 'red',
  EXPIRED: 'red',
  INFO: 'blue',
  WARN: 'orange',
  ERROR: 'red',
  DEBUG: 'default',
}

export const StatusBadge = memo(({ status, children }: StatusBadgeProps) => {
  const color = useMemo(() => statusColors[status.toUpperCase()] || 'default', [status])
  
  return (
    <Tag color={color}>
      {children || status}
    </Tag>
  )
})

StatusBadge.displayName = 'StatusBadge'
