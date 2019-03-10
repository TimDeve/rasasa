import React, { useState, useEffect } from 'react'

async function loadName(setName: Function) {
  const res = await fetch('/api/v0/name/world')

  if (!res.ok) return setName('Failed')

  const json = await res.json()

  setName(json.name)
}

export function Hello() {
  const [name, setName] = useState('Loading...')

  useEffect(() => {
    loadName(setName)
  })

  return <>Hello, {name}!</>
}
