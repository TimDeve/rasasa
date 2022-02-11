import React, { useState, useEffect } from 'react'

import { getInputValuesFromFormEvent } from 'shared/helpers'
import Title from 'shared/components/Title'
import TextInput from 'shared/components/TextInput'
import Button from 'shared/components/Button'
import s from './LoginPage.scss'

async function login(loginData: object) {
  const res = await fetch(`/api/v0/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(loginData),
  })

  if (res.ok) {
    // Very hacky/lazy way of ensuring all feeds/lists are loaded properly
    location.pathname = '/' // Reloads the page...
  }
}

function LoginPage() {
  return (
    <div className={s.component}>
      <Title>Login</Title>

      <div>
        <form
          onSubmit={(e: React.ChangeEvent<HTMLFormElement>) => {
            const inputs = getInputValuesFromFormEvent(e)
            login(inputs)
          }}
        >
          <TextInput name="username" placeholder="Username" />
          <br />
          <TextInput type="password" name="password" placeholder="Password" />
          <br />
          <Button>Login</Button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage
