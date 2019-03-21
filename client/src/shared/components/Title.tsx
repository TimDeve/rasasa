import React from 'react'
import cn from 'classnames'

import s from './Title.scss'

function Title(props: React.HTMLProps<HTMLHeadingElement>) {
  return <h1 {...props} className={cn(s.component, props.className)} />
}

export default Title
