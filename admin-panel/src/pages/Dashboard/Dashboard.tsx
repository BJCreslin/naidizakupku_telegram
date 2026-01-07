import { Row, Col, Card, Statistic, Spin } from 'antd'
import { UserOutlined, KeyOutlined, SafetyOutlined, MessageOutlined } from '@ant-design/icons'
import { useDashboardMetrics } from '../../hooks/useMetrics'
import { MetricsChart } from '../../components/charts/MetricsChart'

export const Dashboard = () => {
  const { data: metrics, isLoading } = useDashboardMetrics()

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!metrics) {
    return <div>Нет данных</div>
  }

  // Подготовка данных для графика (пример)
  const chartData = [
    { name: 'Коды', generated: metrics.codes.generated, verified: metrics.codes.verified },
    { name: 'Верификация', requests: metrics.verification.requests, confirmed: metrics.verification.confirmed },
  ]

  return (
    <div>
      <h1>Dashboard</h1>
      
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Пользователи"
              value={0}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Сгенерировано кодов"
              value={metrics.codes.generated}
              prefix={<KeyOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Активных сессий"
              value={metrics.verification.activeSessions}
              prefix={<SafetyOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Сообщений Telegram"
              value={metrics.telegram.messagesSent}
              prefix={<MessageOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Метрики кодов">
            <Row gutter={16}>
              <Col span={12}>
                <Statistic title="Проверено" value={metrics.codes.verified} />
              </Col>
              <Col span={12}>
                <Statistic title="Просрочено" value={metrics.codes.expired} />
              </Col>
            </Row>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Метрики верификации">
            <Row gutter={16}>
              <Col span={8}>
                <Statistic title="Запросов" value={metrics.verification.requests} />
              </Col>
              <Col span={8}>
                <Statistic title="Подтверждено" value={metrics.verification.confirmed} />
              </Col>
              <Col span={8}>
                <Statistic title="Отозвано" value={metrics.verification.revoked} />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24}>
          <Card title="График метрик">
            <MetricsChart
              data={chartData}
              dataKeys={['generated', 'verified', 'requests', 'confirmed']}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
