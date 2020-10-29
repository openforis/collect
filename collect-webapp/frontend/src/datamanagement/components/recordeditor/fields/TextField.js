import React from 'react'
import { Input } from 'reactstrap'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'

export default class TextField extends AbstractField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.value } })
  }

  render() {
    const { fieldDef } = this.props
    const { dirty, value: valueState } = this.state
    const { value } = valueState || {}
    const text = value || ''
    const { attributeDefinition } = fieldDef
    const { textType } = attributeDefinition

    return (
      <div>
        <Input value={text} type={textType === 'MEMO' ? 'textarea' : 'text'} onChange={this.onChange} />
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
