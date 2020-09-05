import React from 'react'
import { Input } from 'reactstrap'

import AbstractField from './AbstractField'

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

  onChange(event) {
    this.onAttributeUpdate({ value: { value: event.target.value } })
  }

  render() {
    const { value: valueState } = this.state
    const { value } = valueState

    return <Input value={value} onChange={this.onChange} />
  }
}
