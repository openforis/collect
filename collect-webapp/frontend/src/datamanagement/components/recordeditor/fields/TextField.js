import React from 'react'
import MuiTextField from '@material-ui/core/TextField'

import { TextAttributeDefinition } from 'model/Survey'
import { TextFieldDefinition } from 'model/ui/TextFieldDefinition'
import Objects from 'utils/Objects'
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

    const inputFieldValue = event.target.value
    const valueTranformed = tranformFunctions[textTransform](inputFieldValue.trimLeft())

    const attribute = this.getAttribute()
    const valuePrev = Objects.getPath(['value', 'value'], '')(attribute)
    const valueNew = { value: valueTranformed }

    // check if value has changed
    if (valueTranformed.trim() === valuePrev) {
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
        {dirty && <LoadingSpinnerSmall />}
      </>
    )
  }
}
