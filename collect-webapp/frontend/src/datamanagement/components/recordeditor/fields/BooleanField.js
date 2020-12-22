import React from 'react'
import classNames from 'classnames'
import { Checkbox, TextField } from '@material-ui/core'

import { BooleanAttributeDefinition } from 'model/Survey'

import L from 'utils/Labels'
import Strings from 'utils/Strings'

import AbstractField from './AbstractField'
import DirtyFieldSpinner from './DirtyFieldSpinner'

const TRUE_KEY = 'dataManagement.dataEntry.attribute.boolean.textValue.yes'
const FALSE_KEY = 'dataManagement.dataEntry.attribute.boolean.textValue.no'

const valueToText = (value) => {
  if (value === true) return L.l(TRUE_KEY)
  if (value === false) return L.l(FALSE_KEY)
  return ''
}

const textToValue = (text) => {
  if (Strings.equalsIgnoreCase(text, L.l(TRUE_KEY))) return true
  if (Strings.equalsIgnoreCase(text, L.l(FALSE_KEY))) return false
  return null
}

export default class BooleanField extends AbstractField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(fieldValue) {
    this.updateValue({ value: { value: fieldValue }, debounced: false })
  }

  onChangeText(textValue) {
    this.onChange(textToValue(textValue))
  }

  render() {
    const { fieldDef, parentEntity } = this.props
    const { dirty, value: valueState = {} } = this.state
    const { record } = parentEntity
    const { attributeDefinition } = fieldDef
    const { calculated, layoutType } = attributeDefinition
    const { value: fieldValue } = valueState || {}
    const checked = fieldValue || false
    const readOnly = record.readOnly || calculated

    return (
      <div>
        {layoutType === BooleanAttributeDefinition.LayoutTypes.CHECKBOX ? (
          <Checkbox
            color="primary"
            className={classNames({ readOnly })}
            checked={checked}
            onChange={(event) => {
              if (readOnly) {
                event.preventDefault()
                event.stopPropagation()
              } else {
                this.onChange(!checked)
              }
            }}
          />
        ) : (
          <TextField
            className={classNames({ readOnly })}
            variant="outlined"
            disabled={readOnly}
            inputProps={{
              maxLength: 1,
            }}
            style={{ width: '55px' }}
            onChange={(event) => this.onChangeText(event.target.value)}
            value={valueToText(fieldValue)}
          />
        )}
        {dirty && <DirtyFieldSpinner />}
      </div>
    )
  }
}
