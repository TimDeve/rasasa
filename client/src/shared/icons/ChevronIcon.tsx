import React from 'react'

import s from './ChevronIcon.scss'

type Direction = 'up' | 'down' | 'flat'

interface ChevronProps extends React.HTMLProps<SVGSVGElement> {
  color?: string
  direction?: Direction
}

function Chevron({ color = '#000', direction = 'down', ...rest }: ChevronProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path className={`${s.path} ${generatePath(direction)}`} stroke={color} />
    </svg>
  )
}

function generatePath(direction?: Direction): string {
  switch (direction) {
    case 'up':
      return s.up
    case 'flat':
      return s.flat
    default:
      return s.down
  }
}

export default Chevron
