import React from 'react'
import cn from 'classnames'

import s from './TextInput.scss'

function TextInput(props: React.HTMLProps<HTMLInputElement>) {
  return <input type="text" {...props} className={cn(s.component, props.className)} />
}

export default TextInput
