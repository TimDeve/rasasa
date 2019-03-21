function isHTMLInputElement(el: Element): el is HTMLInputElement {
  return el.tagName === 'INPUT'
}

interface InputValues {
  [key: string]: string
}

export function getInputValuesFromFormEvent(
  e: React.ChangeEvent<HTMLFormElement>,
  preventDefault = true
): InputValues {
  if (preventDefault) {
    e.preventDefault()
  }

  return Array.from(e.target.elements).reduce(
    (acc, e: Element) => {
      if (isHTMLInputElement(e)) {
        acc[e.name] = e.value
      }
      return acc
    },
    {} as InputValues
  )
}
