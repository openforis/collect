import React from 'react'
import { Input } from 'reactstrap'

import { TextAttributeDefinition } from 'model/Survey'
import { TextFieldDefinition } from 'model/ui/TextFieldDefinition'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import * as FieldsSizes from './FieldsSizes'

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
