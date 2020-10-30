import React from 'react'
import { Input } from 'reactstrap'

import { TextFieldDefinition } from 'model/ui/TextFieldDefinition'

import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import FieldLoadingSpinner from './FieldLoadingSpinner'

const tranformFunctions = {
  [TextFieldDefinition.TextTranform.NONE]: (value) => value,
  [TextFieldDefinition.TextTranform.UPPERCASE]: (value) => value.toUpperCase(),
  [TextFieldDefinition.TextTranform.LOWERCASE]: (value) => value.toLowerCase(),
}

export default class TextField extends AbstractSingleAttributeField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    const { fieldDef } = this.props
    const { textTransform = TextFieldDefinition.TextTranform.NONE } = fieldDef
    const value = event.target.value
    const valueTranformed = tranformFunctions[textTransform](value)

    this.onAttributeUpdate({ value: { value: valueTranformed } })
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
