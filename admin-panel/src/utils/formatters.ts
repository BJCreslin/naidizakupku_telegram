import dayjs from 'dayjs'
import 'dayjs/locale/ru'
import relativeTime from 'dayjs/plugin/relativeTime'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(relativeTime)
dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.locale('ru')

export const formatDate = (date: string | Date, format: string = 'DD.MM.YYYY HH:mm'): string => {
  return dayjs(date).format(format)
}

export const formatRelativeTime = (date: string | Date): string => {
  return dayjs(date).fromNow()
}

export const formatDateTime = (date: string | Date): string => {
  return dayjs(date).format('DD.MM.YYYY HH:mm:ss')
}

export const formatNumber = (value: number): string => {
  return new Intl.NumberFormat('ru-RU').format(value)
}

