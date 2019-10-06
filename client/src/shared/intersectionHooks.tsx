import React, { useState, useRef, useEffect, useCallback, useContext } from 'react'

export function useElementHasExitedTopScreen(callback: () => void) {
  const [node, setNode] = useState<Element | null>(null)
  const [wasTriggered, setWasTriggered] = useState<boolean>(false)

  const ref = useCallback(node => {
    if (node !== null) {
      setNode(node)
    }
  }, [])

  useEffect(() => {
    let observer: IntersectionObserver | null = null
    if (!wasTriggered && node !== null) {
      observer = new IntersectionObserver(val => {
        if ((val[0].boundingClientRect as DOMRect).y < 0) {
          setWasTriggered(true)
          callback()
        }
      })

      observer.observe(node)
    }

    return () => {
      observer && observer.disconnect()
    }
  }, [node, callback, wasTriggered])

  return ref
}
