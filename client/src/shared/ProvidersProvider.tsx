import React, { ComponentClass, FunctionComponent } from 'react'

interface ProviderProps {
  children?: JSX.Element
  context?: {}
}

type ProviderT = ComponentClass<ProviderProps> | FunctionComponent<ProviderProps>

interface ProvidersProviderProps {
  providers: ProviderT[]
  children: JSX.Element
}

function ProvidersProvider({ providers = [], children: propsChildren }: ProvidersProviderProps) {
  return providers.reduceRight((children, Provider) => <Provider>{children}</Provider>, propsChildren)
}

export default ProvidersProvider
