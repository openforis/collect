import React from 'react'
import classNames from 'classnames'
import { Checkbox } from '@material-ui/core'

import AbstractField from './AbstractField'
import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'

export default class BooleanField extends AbstractField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    this.updateValue({ value: { value: event.target.checked }, debounced: false })
  }

  render() {
    const { fieldDef } = this.props
    const { dirty, value: valueState = {} } = this.state
    const { attributeDefinition } = fieldDef
    const { calculated } = attributeDefinition
    const { value } = valueState || {}
    const checked = value || false

    return (
      <div>
        <Checkbox
          color="primary"
          className={classNames({ readOnly: calculated })}
          checked={checked}
          onChange={(event) => {
            if (calculated) {
              event.preventDefault()
              event.stopPropagation()
            } else {
              this.onChange(event)
            }
          }}
        />
        {dirty && <LoadingSpinnerSmall />}
      </div>
    )
  }
}
