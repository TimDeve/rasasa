import React from 'react'

interface BookProps extends React.HTMLProps<SVGSVGElement> {
  color?: string
}

function Book({ color, ...rest }: BookProps) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" {...rest}>
      <path d="M12 21L4 18V4L12 6M12 21L20 18V4L12 6M12 21V6" stroke={color} />
    </svg>
  )
}

Book.defaultProps = {
  color: '#000',
}

export default Book
