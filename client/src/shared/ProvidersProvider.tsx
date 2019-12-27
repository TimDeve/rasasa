import React, { ComponentClass, Fragment, ReactNode, FunctionComponent } from 'react'

interface ProviderProps {
  children?: JSX.Element
  context?: {}
}

type ProviderT = ComponentClass<ProviderProps> | FunctionComponent<ProviderProps>

function ProvidersProvider({ providers = [], children }: { providers: ProviderT[]; children: JSX.Element }) {
  return providers.reduceRight(
    (acc: JSX.Element, Provider: ProviderT) => <Provider>{acc}</Provider>,
    children
  )
}

export default ProvidersProvider
