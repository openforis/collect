import React from 'react'
import { Input } from 'reactstrap'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import FieldValidationTooltip from './FieldValidationTooltip'

export default class TextField extends AbstractField {
  constructor() {
    super()

    this.fieldId = `text-field-${new Date().getTime()}`

    this.onChange = this.onChange.bind(this)
  }

  extractValueFromProps() {
    const attr = this.getSingleAttribute()
    return { value: attr.fields[0].value }
  }

  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.value } })
  }

  render() {
    const { dirty, value: valueState, errors, warnings } = this.state
    const { value } = valueState || {}
    const text = value || ''

    return (
      <div>
        <>
          <Input
            id={this.fieldId}
            invalid={Boolean(errors || warnings)}
            className={warnings ? 'warning' : ''}
            value={text}
            onChange={this.onChange}
          />
          <FieldValidationTooltip target={this.fieldId} errors={errors} warnings={warnings} />
          {dirty && <FieldLoadingSpinner />}
        </>
      </div>
    )
  }
}
