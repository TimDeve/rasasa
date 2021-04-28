import React, { useState, useEffect, ReactNode } from 'react'

export enum LoggedInState {
  LoggedIn = 'LoggedIn',
  LoggedOut = 'LoggedOut',
  Offline = 'Offline',
}

export const LoggedInContext = React.createContext<LoggedInState>(LoggedInState.LoggedIn)

export function LoggedInProvider({ children }: { children?: ReactNode }) {
  const [loggedInState, setLoggedInState] = useState<LoggedInState>(LoggedInState.LoggedIn)

  async function fetchPing() {
    try {
      const res = await fetch('/api/v0/authenticated')

      switch (res.status) {
        case 204:
          setLoggedInState(LoggedInState.LoggedIn)
          break
        case 401:
          setLoggedInState(LoggedInState.LoggedOut)
          break
        default:
          setLoggedInState(LoggedInState.Offline)
          break
      }
    } catch (e) {
      setLoggedInState(LoggedInState.Offline)
    }
  }

  useEffect(() => {
    fetchPing()

    const timer = setInterval(async => {
      fetchPing()
    }, 5000)

    return () => clearInterval(timer)
  }, [])

  return <LoggedInContext.Provider value={loggedInState}>{children}</LoggedInContext.Provider>
}
