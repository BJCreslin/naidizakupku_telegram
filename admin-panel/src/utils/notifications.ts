import { notification } from 'antd'
import { NotificationPlacement } from 'antd/es/notification/interface'

interface NotificationOptions {
  title: string
  message?: string
  duration?: number
  placement?: NotificationPlacement
}

export const showSuccess = (options: NotificationOptions) => {
  notification.success({
    message: options.title,
    description: options.message,
    duration: options.duration || 3,
    placement: options.placement || 'topRight',
  })
}

export const showError = (options: NotificationOptions) => {
  notification.error({
    message: options.title,
    description: options.message,
    duration: options.duration || 5,
    placement: options.placement || 'topRight',
  })
}

export const showWarning = (options: NotificationOptions) => {
  notification.warning({
    message: options.title,
    description: options.message,
    duration: options.duration || 4,
    placement: options.placement || 'topRight',
  })
}

export const showInfo = (options: NotificationOptions) => {
  notification.info({
    message: options.title,
    description: options.message,
    duration: options.duration || 3,
    placement: options.placement || 'topRight',
  })
}

