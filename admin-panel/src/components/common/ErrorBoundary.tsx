import { Component, ReactNode, ErrorInfo } from 'react'
import { Result, Button, Card } from 'antd'
import { ReloadOutlined, HomeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
  errorInfo: ErrorInfo | null
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    }
  }

  static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error,
      errorInfo: null,
    }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo)
    this.setState({
      error,
      errorInfo,
    })
  }

  handleReset = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    })
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback
      }

      return (
        <Card style={{ margin: '24px' }}>
          <Result
            status="error"
            title="Произошла ошибка"
            subTitle={
              this.state.error?.message || 
              'Что-то пошло не так. Пожалуйста, попробуйте обновить страницу.'
            }
            extra={[
              <ErrorBoundaryActions 
                key="actions"
                onReset={this.handleReset}
              />,
            ]}
          >
            {import.meta.env.DEV && this.state.errorInfo && (
              <div style={{ 
                marginTop: 20, 
                padding: 16, 
                background: '#f5f5f5', 
                borderRadius: 4,
                maxHeight: 400,
                overflow: 'auto'
              }}>
                <pre style={{ margin: 0, fontSize: 12 }}>
                  {this.state.error?.stack}
                  {'\n\n'}
                  {this.state.errorInfo.componentStack}
                </pre>
              </div>
            )}
          </Result>
        </Card>
      )
    }

    return this.props.children
  }
}

// Компонент для действий в Error Boundary (нужен для использования хуков)
function ErrorBoundaryActions({ onReset }: { onReset: () => void }) {
  const navigate = useNavigate()

  return (
    <>
      <Button type="primary" icon={<ReloadOutlined />} onClick={onReset}>
        Попробовать снова
      </Button>
      <Button icon={<HomeOutlined />} onClick={() => navigate('/dashboard')}>
        На главную
      </Button>
    </>
  )
}

