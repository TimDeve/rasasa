import React from 'react'

interface ConnectionIconProps extends React.SVGProps<SVGSVGElement> {
  color?: string
}

function ConnectionIcon({ color = '#000', ...rest }: ConnectionIconProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path d="M4 11.5H8.5M8.5 11.5V6.5H14.5M8.5 11.5V16.5H14.5" stroke={color} />
      <path d="M20 11.5H12.5" stroke={color} />
      <rect x="12" y="10" width="3" height="3" fill={color} />
    </svg>
  )
}

export default ConnectionIcon
