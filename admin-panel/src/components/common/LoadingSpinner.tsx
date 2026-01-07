import { Spin } from 'antd'
import { memo } from 'react'

interface LoadingSpinnerProps {
  size?: 'small' | 'default' | 'large'
  tip?: string
  fullScreen?: boolean
  minHeight?: number | string
}

export const LoadingSpinner = memo(({ 
  size = 'large', 
  tip,
  fullScreen = false,
  minHeight = 400
}: LoadingSpinnerProps) => {
  const style: React.CSSProperties = fullScreen
    ? {
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'rgba(255, 255, 255, 0.8)',
        zIndex: 9999,
      }
    : {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: typeof minHeight === 'number' ? `${minHeight}px` : minHeight,
        padding: 50,
      }

  return (
    <div style={style}>
      <Spin size={size} tip={tip} />
    </div>
  )
})

LoadingSpinner.displayName = 'LoadingSpinner'

