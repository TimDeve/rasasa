declare module 'react-scrolllock' {
  import React from 'react'

  export interface ScrollLockProps {
    children?: React.ReactNode
    accountForScrollbars?: boolean
    isActive?: boolean
  }

  const ScrollLock: React.ComponentClass<ScrollLockProps>

  export interface TouchScrollableProps {
    children?: React.ReactNode
  }

  export const TouchScrollable: React.ComponentClass<{}>

  export default ScrollLock
}
