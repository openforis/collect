import React from 'react'
import classNames from 'classnames'
import { Checkbox, TextField } from '@material-ui/core'

import AbstractField from './AbstractField'
import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import { BooleanAttributeDefinition } from 'model/Survey'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

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
    const { fieldDef } = this.props
    const { dirty, value: valueState = {} } = this.state
    const { attributeDefinition } = fieldDef
    const { calculated, layoutType } = attributeDefinition
    const { value: fieldValue } = valueState || {}
    const checked = fieldValue || false

    return (
      <div>
        {layoutType === BooleanAttributeDefinition.LayoutTypes.CHECKBOX ? (
          <Checkbox
            color="primary"
            className={classNames({ readOnly: calculated })}
            checked={checked}
            onChange={(event) => {
              if (calculated) {
                event.preventDefault()
                event.stopPropagation()
              } else {
                this.onChange(checked ? null : true)
              }
            }}
          />
        ) : (
          <TextField
            className={classNames({ readOnly: calculated })}
            variant="outlined"
            disabled={calculated}
            inputProps={{
              maxLength: 1,
            }}
            style={{ width: '55px' }}
            onChange={(event) => this.onChangeText(event.target.value)}
            value={valueToText(fieldValue)}
          />
        )}
        {dirty && <LoadingSpinnerSmall />}
      </div>
    )
  }
}
