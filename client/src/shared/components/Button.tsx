import React from 'react'
import cn from 'classnames'

import s from './Button.scss'
import Refresh from 'shared/icons/Refresh'

interface ButtonProps extends React.HTMLProps<HTMLButtonElement> {
  loading?: boolean
  type?: 'button' | 'submit' | 'reset'
}

function Button({ loading, className, children, ...rest }: ButtonProps) {
  return (
    <button {...rest} className={cn(s.component, className, { [s.componentLoading]: loading })}>
      {children}
      {loading && <Refresh animated className={s.loadingIndicator} />}
    </button>
  )
}

export default Button
