import { Card, Form, InputNumber, Button, Spin } from 'antd'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { settingsApi } from '../../api/settings'
import { showSuccess, showError } from '../../utils/notifications'

export const Settings = () => {
  const [form] = Form.useForm()
  const queryClient = useQueryClient()

  const { data: settings, isLoading } = useQuery({
    queryKey: ['settings'],
    queryFn: () => settingsApi.getSettings(),
  })

  const updateSettings = useMutation({
    mutationFn: (values: Record<string, any>) => settingsApi.updateSettings(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['settings'] })
      showSuccess({ title: 'Настройки обновлены' })
    },
    onError: () => {
      showError({ title: 'Ошибка', message: 'Не удалось обновить настройки' })
    },
  })

  const onFinish = (values: Record<string, any>) => {
    updateSettings.mutate(values)
  }

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div>
      <h1>Настройки</h1>
      
      <Card>
        <Form
          form={form}
          layout="vertical"
          initialValues={settings}
          onFinish={onFinish}
        >
          <Form.Item
            label="Время действия кода (минуты)"
            name="codeExpirationMinutes"
          >
            <InputNumber min={1} max={1440} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            label="Время жизни сессии верификации (минуты)"
            name="verificationSessionCleanupMinutes"
          >
            <InputNumber min={1} max={1440} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={updateSettings.isPending}
            >
              Сохранить
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
