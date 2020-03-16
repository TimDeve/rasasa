import React from 'react'

interface ListIconProps extends React.SVGProps<SVGSVGElement> {
  color?: string
}

function ListIcon({ color = '#000', ...rest }: ListIconProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <line x1="4" y1="5.5" x2="20" y2="5.5" stroke={color} />
      <path d="M4 11.5H16" stroke={color} />
      <path d="M4 17.5H12" stroke={color} />
    </svg>
  )
}

export default ListIcon
