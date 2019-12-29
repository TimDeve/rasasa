import React from 'react'

import s from './ChevronIcon.scss'

type Direction = 'up' | 'down' | 'flat'

interface ChevronProps extends React.HTMLProps<SVGSVGElement> {
  color?: string
  direction?: Direction
}

function Chevron({ color = '#000', direction = 'down', ...rest }: ChevronProps) {
  const [className, pathDraw] = generatePath(direction)

  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path className={`${s.path} ${className}`} d={pathDraw} stroke={color} />
    </svg>
  )
}

function generatePath(direction?: Direction): [string, string] {
  switch (direction) {
    case 'up':
      return [s.up, 'M4 17L12 7L20 17']
    case 'flat':
      return [s.flat, 'M4 12L12 12L20 12']
    default:
      return [s.down, 'M4 7L12 17L20 7']
  }
}

export default Chevron
