import { Input } from 'antd'
import { SearchOutlined } from '@ant-design/icons'

interface SearchBarProps {
  placeholder?: string
  value?: string
  onChange?: (value: string) => void
  onSearch?: (value: string) => void
  style?: React.CSSProperties
}

export const SearchBar = ({ 
  placeholder = 'Поиск...', 
  value, 
  onChange, 
  onSearch,
  style 
}: SearchBarProps) => {
  return (
    <Input
      placeholder={placeholder}
      prefix={<SearchOutlined />}
      value={value}
      onChange={(e) => onChange?.(e.target.value)}
      onPressEnter={(e) => onSearch?.(e.currentTarget.value)}
      allowClear
      style={style}
    />
  )
}
