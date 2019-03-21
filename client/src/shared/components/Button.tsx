import React from 'react'
import cn from 'classnames'

import s from './Button.scss'

function Button(props: React.HTMLProps<HTMLButtonElement>) {
  return <button {...props} className={cn(s.component, props.className)} />
}

export default Button
