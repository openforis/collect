import React from 'react'
import { Checkbox } from '@material-ui/core'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'

export default class BooleanField extends AbstractField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.checked }, debounced: false })
  }

  render() {
    const { dirty, value: valueState = {} } = this.state
    const { value } = valueState || {}
    const checked = value || false
    return (
      <div>
        <Checkbox checked={checked} onChange={this.onChange} />
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
