import React from 'react'
import { Input } from 'reactstrap'

import { TextAttributeDefinition } from 'model/Survey'
import { TextFieldDefinition } from 'model/ui/TextFieldDefinition'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import AbstractField from './AbstractField'
import * as FieldsSizes from './FieldsSizes'

const tranformFunctions = {
  [TextFieldDefinition.TextTranform.NONE]: (value) => value,
  [TextFieldDefinition.TextTranform.UPPERCASE]: (value) => value.toUpperCase(),
  [TextFieldDefinition.TextTranform.LOWERCASE]: (value) => value.toLowerCase(),
}

export default class TextField extends AbstractField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    const { fieldDef } = this.props
    const { textTransform = TextFieldDefinition.TextTranform.NONE } = fieldDef
    const value = event.target.value
    const valueTranformed = tranformFunctions[textTransform](value)

    this.updateValue({ value: { value: valueTranformed } })
  }

  render() {
    const { fieldDef, inTable } = this.props
    const { dirty, value: valueState } = this.state
    const { value } = valueState || {}
    const text = value || ''
    const { attributeDefinition } = fieldDef
    const { textType } = attributeDefinition

    const inputFieldType = textType === TextAttributeDefinition.TextTypes.MEMO ? 'textarea' : 'text'

    return (
      <>
        <Input
          value={text}
          type={inputFieldType}
          onChange={this.onChange}
          style={{ width: FieldsSizes.getWidth({ fieldDef, inTable }) }}
        />
        {dirty && <LoadingSpinnerSmall />}
      </>
    )
  }
}
