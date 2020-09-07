import React from 'react'
import { Input } from 'reactstrap'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'

export default class TextField extends AbstractField {
  constructor(props) {
    super(props)

    this.onChange = this.onChange.bind(this)

    this.state = {
      value: { value: '' },
      dirty: false,
    }
  }

  extractValueFromProps() {
    const attr = this.getSingleAttribute()
    return { value: attr.fields[0].value }
  }

  extractValueFromAttributeUpdateEvent(event) {
    return { value: event.text }
  }

  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.value } })
  }

  render() {
    const { value: valueState = {}, dirty } = this.state
    const { value } = valueState
    const text = value || ''

    return (
      <React.Fragment>
        <Input value={text} onChange={this.onChange} />
        {dirty && <FieldLoadingSpinner />}
      </React.Fragment>
    )
  }
}
