import React from 'react'
import { Checkbox } from '@material-ui/core'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import FieldValidationTooltip from './FieldValidationTooltip'

export default class BooleanField extends AbstractField {
  constructor() {
    super()

    this.fieldId = `boolean-field-${new Date().getTime()}`
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
        <Checkbox
          id={this.fieldId}
          color={Boolean(errors) || Boolean(warnings) ? 'secondary' : 'primary'}
          checked={checked}
          onChange={this.onChange}
        />
        {dirty && <FieldLoadingSpinner />}
        <FieldValidationTooltip target={this.fieldId} errors={errors} warnings={warnings} />
      </div>
    )
  }
}
