import { useState, Dispatch, SetStateAction } from 'react'

function useToggle(defaultValue: boolean): [boolean, () => void, Dispatch<SetStateAction<boolean>>] {
  const [state, setState] = useState<boolean>(defaultValue)

  function toggleState() {
    setState(!state)
  }

  return [state, toggleState, setState]
}

export default useToggle
