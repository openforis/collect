import React from 'react'
import { Checkbox } from '@material-ui/core'

import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'

export default class BooleanField extends AbstractSingleAttributeField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.checked }, debounced: false })
  }

  render() {
    const { dirty, value: valueState = {} } = this.state
    const { value } = valueState || {}
    const checked = value || false
    return (
      <div>
        <Checkbox checked={checked} onChange={this.onChange} />
        {dirty && <LoadingSpinnerSmall />}
      </div>
    )
  }
}
