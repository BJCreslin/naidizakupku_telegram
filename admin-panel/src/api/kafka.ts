import { apiClient } from './client'

export interface KafkaStatus {
  topics: TopicInfo[]
  consumerGroups: ConsumerGroupInfo[]
  isAvailable: boolean
}

export interface TopicInfo {
  name: string
  partitions: number
  replicationFactor: number
  size: number
  messageCount: number
}

export interface ConsumerGroupInfo {
  groupId: string
  activeConsumers: number
  topicsCount: number
  state: string
}

export const kafkaApi = {
  getStatus: async (): Promise<KafkaStatus> => {
    const response = await apiClient.get<KafkaStatus>('/api/admin/kafka/topics')
    return response.data
  },

  getConsumerGroups: async (): Promise<ConsumerGroupInfo[]> => {
    const response = await apiClient.get<ConsumerGroupInfo[]>('/api/admin/kafka/consumer-groups')
    return response.data
  },
}

