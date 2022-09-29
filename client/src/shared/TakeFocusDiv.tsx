import React, { HTMLProps, useEffect, useRef } from 'react'

export default function TakeFocusDiv(props: HTMLProps<HTMLDivElement>) {
  let ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (ref.current) {
      ref.current.tabIndex = -1
      ref.current.focus()
    }
  }, [ref])

  return <div ref={ref} {...props} style={{ outline: 'none', ...props.style }} />
}
