import React from 'react'
import cn from 'classnames'

import s from './RefreshIcon.scss'

interface RefreshProps extends React.SVGProps<SVGSVGElement> {
  color?: string
  animated?: boolean
}

function Refresh({ color = '#000', animated, ...rest }: RefreshProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path
        className={cn({ [s.animatedPath]: animated })}
        d="M16 20C11 20 4 20 4 20V4H20V18L16 15"
        stroke={color}
        strokeMiterlimit="10"
      />
    </svg>
  )
}

export default Refresh
