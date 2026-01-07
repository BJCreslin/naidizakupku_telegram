import { Card, Table, Tag, Space, Button, Spin } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { kafkaApi, KafkaStatus, TopicInfo, ConsumerGroupInfo } from '../../api/kafka'

export const KafkaStatus = () => {
  const { data: status, isLoading, refetch } = useQuery<KafkaStatus>({
    queryKey: ['kafka', 'status'],
    queryFn: () => kafkaApi.getStatus(),
    refetchInterval: 30000, // Обновление каждые 30 секунд
  })

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!status) {
    return <div>Нет данных</div>
  }

  const topicsColumns = [
    {
      title: 'Название',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Партиции',
      dataIndex: 'partitions',
      key: 'partitions',
      width: 100,
    },
    {
      title: 'Реплики',
      dataIndex: 'replicationFactor',
      key: 'replicationFactor',
      width: 100,
    },
    {
      title: 'Размер',
      dataIndex: 'size',
      key: 'size',
      width: 120,
      render: (size: number) => `${(size / 1024 / 1024).toFixed(2)} MB`,
    },
    {
      title: 'Сообщений',
      dataIndex: 'messageCount',
      key: 'messageCount',
      width: 120,
    },
  ]

  const consumerGroupsColumns = [
    {
      title: 'Group ID',
      dataIndex: 'groupId',
      key: 'groupId',
    },
    {
      title: 'Активных consumers',
      dataIndex: 'activeConsumers',
      key: 'activeConsumers',
      width: 150,
    },
    {
      title: 'Топиков',
      dataIndex: 'topicsCount',
      key: 'topicsCount',
      width: 100,
    },
    {
      title: 'Статус',
      dataIndex: 'state',
      key: 'state',
      width: 120,
      render: (state: string) => (
        <Tag color={state === 'STABLE' ? 'green' : 'orange'}>{state}</Tag>
      ),
    },
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <h1>Kafka Status</h1>
        <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
          Обновить
        </Button>
        <Tag color={status.isAvailable ? 'green' : 'red'}>
          {status.isAvailable ? 'Доступен' : 'Недоступен'}
        </Tag>
      </Space>

      <Card title="Топики" style={{ marginBottom: 16 }}>
        <Table
          dataSource={status.topics}
          columns={topicsColumns}
          rowKey="name"
          pagination={false}
        />
      </Card>

      <Card title="Consumer Groups">
        <Table
          dataSource={status.consumerGroups}
          columns={consumerGroupsColumns}
          rowKey="groupId"
          pagination={false}
        />
      </Card>
    </div>
  )
}
