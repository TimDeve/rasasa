import React from 'react'

interface FeedIconProps extends React.HTMLProps<SVGSVGElement> {
  color?: string
}

function FeedIcon({ color = '#000', ...rest }: FeedIconProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="5" y="16" width="3" height="3" fill={color} />
      <path d="M5 10.5H9.5L13.5 14.5V19" stroke={color} />
      <path d="M5 4.5H13.5L19.5 10.5V19" stroke={color} />
    </svg>
  )
}

export default FeedIcon
