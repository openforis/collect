import React from 'react'
import MuiTextField from '@material-ui/core/TextField'

import { TextAttributeDefinition } from 'model/Survey'

import Objects from 'utils/Objects'

import AbstractField from './AbstractField'
import * as FieldsSizes from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

const transformFunctions = {
  [TextAttributeDefinition.TextTransform.NONE]: (value) => value,
  [TextAttributeDefinition.TextTransform.UPPERCASE]: (value) => value.toLocaleUpperCase(),
  [TextAttributeDefinition.TextTransform.LOWERCASE]: (value) => value.toLocaleLowerCase(),
}

export default class TextField extends AbstractField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    const { fieldDef } = this.props
    const { attributeDefinition } = fieldDef
    const { textTransform = TextAttributeDefinition.TextTransform.NONE } = attributeDefinition

    const inputFieldValue = event.target.value
    const valueTransformed = transformFunctions[textTransform](inputFieldValue.trimLeft())

    const attribute = this.getAttribute()
    const valuePrev = Objects.getPath(['value', 'value'], '')(attribute)
    const valueNew = { value: valueTransformed }

    // check if value has changed
    if (valueTransformed.trim() === valuePrev) {
      // value not changed: update UI but do not send update to server side
      this.setState({ value: valueNew })
    } else {
      // value changed: updated UI and server side
      this.updateValue({ value: valueNew })
    }
  }

  render() {
    const { fieldDef, inTable, parentEntity } = this.props
    const { dirty, value: valueState } = this.state
    const { record } = parentEntity
    const { value } = valueState || {}
    const text = value || ''
    const { attributeDefinition } = fieldDef
    const { textType, calculated } = attributeDefinition
    const readOnly = record.readOnly || calculated

    const showAsTextArea = textType === TextAttributeDefinition.TextTypes.MEMO && !inTable
    const inputFieldType = showAsTextArea ? 'textarea' : 'text'

    return (
      <>
        <MuiTextField
          value={text}
          type={inputFieldType}
          onChange={this.onChange}
          variant="outlined"
          multiline={showAsTextArea}
          rows={showAsTextArea ? 3 : 1}
          disabled={readOnly}
          style={{ width: FieldsSizes.getWidth({ fieldDef, inTable }) }}
        />
        {dirty && <DirtyFieldSpinner />}
      </>
    )
  }
}
