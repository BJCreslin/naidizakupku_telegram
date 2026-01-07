import { memo, useCallback } from 'react'
import { Input } from 'antd'
import { SearchOutlined } from '@ant-design/icons'

interface SearchBarProps {
  placeholder?: string
  value?: string
  onChange?: (value: string) => void
  onSearch?: (value: string) => void
  style?: React.CSSProperties
}

export const SearchBar = memo(({ 
  placeholder = 'Поиск...', 
  value, 
  onChange, 
  onSearch,
  style 
}: SearchBarProps) => {
  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    onChange?.(e.target.value)
  }, [onChange])

  const handlePressEnter = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
    onSearch?.(e.currentTarget.value)
  }, [onSearch])

  return (
    <Input
      placeholder={placeholder}
      prefix={<SearchOutlined />}
      value={value}
      onChange={handleChange}
      onPressEnter={handlePressEnter}
      allowClear
      style={style}
    />
  )
})

SearchBar.displayName = 'SearchBar'
