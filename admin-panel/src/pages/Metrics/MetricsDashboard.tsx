import { useState } from 'react'
import { Card, Row, Col, Select, Statistic, Spin } from 'antd'
import { 
  KeyOutlined, 
  SafetyOutlined, 
  MessageOutlined, 
  DatabaseOutlined 
} from '@ant-design/icons'
import { useCodeMetrics, useVerificationMetrics, useTelegramMetrics, useKafkaMetrics } from '../../hooks/useMetrics'
import { BarChart } from '../../components/charts/BarChart'

export const MetricsDashboard = () => {
  const [period, setPeriod] = useState('24h')
  
  const { data: codeMetrics, isLoading: codesLoading } = useCodeMetrics(period)
  const { data: verificationMetrics, isLoading: verificationLoading } = useVerificationMetrics(period)
  const { data: telegramMetrics, isLoading: telegramLoading } = useTelegramMetrics(period)
  const { data: kafkaMetrics, isLoading: kafkaLoading } = useKafkaMetrics(period)

  const isLoading = codesLoading || verificationLoading || telegramLoading || kafkaLoading

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  const chartData: Array<{ [key: string]: string | number; name: string }> = [
    { 
      name: 'Коды', 
      generated: codeMetrics?.generated || 0, 
      verified: codeMetrics?.verified || 0,
      failed: codeMetrics?.verificationFailed || 0,
      requests: 0,
      confirmed: 0,
      revoked: 0,
    },
    { 
      name: 'Верификация', 
      requests: verificationMetrics?.requests || 0, 
      confirmed: verificationMetrics?.confirmed || 0,
      revoked: verificationMetrics?.revoked || 0,
      generated: 0,
      verified: 0,
      failed: 0,
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h1>Метрики</h1>
        <Select
          value={period}
          onChange={setPeriod}
          style={{ width: 150 }}
        >
          <Select.Option value="24h">Последние 24 часа</Select.Option>
          <Select.Option value="7d">Последние 7 дней</Select.Option>
          <Select.Option value="30d">Последние 30 дней</Select.Option>
        </Select>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Сгенерировано кодов"
              value={codeMetrics?.generated || 0}
              prefix={<KeyOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Проверено кодов"
              value={codeMetrics?.verified || 0}
              prefix={<KeyOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Активных сессий"
              value={verificationMetrics?.activeSessions || 0}
              prefix={<SafetyOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Сообщений Telegram"
              value={telegramMetrics?.messagesSent || 0}
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
              <Col span={8}>
                <Statistic title="Сгенерировано" value={codeMetrics?.generated || 0} />
              </Col>
              <Col span={8}>
                <Statistic title="Проверено" value={codeMetrics?.verified || 0} />
              </Col>
              <Col span={8}>
                <Statistic title="Ошибок" value={codeMetrics?.verificationFailed || 0} />
              </Col>
            </Row>
            <div style={{ marginTop: 16 }}>
              <Statistic 
                title="Среднее время генерации" 
                value={codeMetrics?.avgGenerationTime || 0} 
                suffix="мс"
                precision={2}
              />
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Метрики верификации">
            <Row gutter={16}>
              <Col span={8}>
                <Statistic title="Запросов" value={verificationMetrics?.requests || 0} />
              </Col>
              <Col span={8}>
                <Statistic title="Подтверждено" value={verificationMetrics?.confirmed || 0} />
              </Col>
              <Col span={8}>
                <Statistic title="Отозвано" value={verificationMetrics?.revoked || 0} />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="Метрики Telegram">
            <Row gutter={16}>
              <Col span={12}>
                <Statistic title="Отправлено" value={telegramMetrics?.messagesSent || 0} />
              </Col>
              <Col span={12}>
                <Statistic title="Ошибок" value={telegramMetrics?.messagesFailed || 0} />
              </Col>
            </Row>
            <div style={{ marginTop: 16 }}>
              <Statistic 
                title="Успешность" 
                value={telegramMetrics?.successRate || 0} 
                suffix="%"
                precision={2}
              />
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Метрики Kafka">
            <Row gutter={16}>
              <Col span={12}>
                <Statistic 
                  title="Отправлено" 
                  value={kafkaMetrics?.messagesSent || 0}
                  prefix={<DatabaseOutlined />}
                />
              </Col>
              <Col span={12}>
                <Statistic 
                  title="Получено" 
                  value={kafkaMetrics?.messagesReceived || 0}
                  prefix={<DatabaseOutlined />}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24}>
          <Card title="График метрик">
            <BarChart
              data={chartData}
              dataKeys={['generated', 'verified', 'requests', 'confirmed']}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
