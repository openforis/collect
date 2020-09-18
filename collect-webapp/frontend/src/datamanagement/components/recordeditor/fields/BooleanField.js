import React from 'react'
import { Checkbox } from '@material-ui/core'

import AbstractField from './AbstractField'
import FieldValidationFeedback from './FieldValidationFeedback'
import FieldLoadingSpinner from './FieldLoadingSpinner'

export default class BooleanField extends AbstractField {
  constructor() {
    super()

    this.onChange = this.onChange.bind(this)
  }

  extractValueFromProps() {
    const attr = this.getSingleAttribute()
    return { value: attr.fields[0].value }
  }
  
  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.checked }, debounced: false })
  }

  render() {
    const { dirty, value: valueState = {}, errors, warnings } = this.state
    const { value } = valueState || {}
    const checked = value || false
    return (
      <div>
        <Checkbox color="primary" checked={checked} onChange={this.onChange} />
        {dirty && <FieldLoadingSpinner />}
        <FieldValidationFeedback errors={errors} warnings={warnings} />
      </div>
    )
  }
}
