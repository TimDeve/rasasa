import React from 'react'

interface ChevronProps extends React.HTMLProps<SVGSVGElement> {
  color?: string,
    direction?: "up" | "down"
}

function Chevron({ color,direction, ...rest }: ChevronProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path d={direction === "down" ? "M4 7L12 17L20 7" : 'M4 17L12 7L20 17'} stroke={color} />
    </svg>
  )
}

Chevron.defaultProps = {
  color: '#000',
 direction: "down"
}

export default Chevron
