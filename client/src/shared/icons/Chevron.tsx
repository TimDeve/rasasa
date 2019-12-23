import React from 'react'

type Direction = 'up' | 'down' | 'flat'

interface ChevronProps extends React.HTMLProps<SVGSVGElement> {
  color?: string
  direction?: Direction
}

function Chevron({ color, direction, ...rest }: ChevronProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path d={generatePath(direction)} stroke={color} />
    </svg>
  )
}

function generatePath(direction?: Direction): string {
  switch (direction) {
    case 'down':
      return 'M4 7L12 17L20 7'
    case 'flat':
      return 'M4 12L12 12L20 12'
    default:
      return 'M4 17L12 7L20 17'
  }
}

Chevron.defaultProps = {
  color: '#000',
  direction: 'down',
}

export default Chevron
