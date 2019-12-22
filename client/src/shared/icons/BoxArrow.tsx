import React from 'react'

interface BoxArrowProps extends React.HTMLProps<SVGSVGElement> {
  color?: string
}

function BoxArrow({ color, ...rest }: BoxArrowProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path d="M11 14L20 5M20 5H14M20 4.5V11" stroke={color} />
      <path d="M9 5H4V20H20V15" stroke={color} />
    </svg>
  )
}

BoxArrow.defaultProps = {
  color: '#000',
}

export default BoxArrow
